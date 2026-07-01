package dev.mobase.featureflags.amplitude

import android.app.Application
import com.amplitude.experiment.Experiment
import com.amplitude.experiment.ExperimentConfig
import com.amplitude.experiment.ExperimentUser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import dev.mobase.featureflags.FeatureFlags
import dev.mobase.featureflags.model.FeatureFlag
import dev.mobase.featureflags.model.EvaluationContext
import dev.mobase.featureflags.model.Variant
import dev.mobase.featureflags.storage.DefaultFeatureFlagStorage
import dev.mobase.featureflags.storage.FeatureFlagsStorage
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

class AmplitudeFeatureFlags(
    application: Application,
    deploymentKey: String,
    featureFlags: List<FeatureFlag> = emptyList(),
    private val storage: FeatureFlagsStorage = DefaultFeatureFlagStorage(application.applicationContext),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + ioDispatcher)
) : FeatureFlags {
    private val featureFlags = featureFlags.associateBy { it.key }

    private val keyLocks = ConcurrentHashMap<String, ReentrantLock>()

    private val client = Experiment.initializeWithAmplitudeAnalytics(
        application = application,
        apiKey = deploymentKey,
        config = ExperimentConfig.builder()
            .automaticExposureTracking(false)
            .build()
    )

    override suspend fun fetch(context: EvaluationContext): Result<Unit> {
        Timber.d("Fetching feature flags")

        val storage = coroutineScope {
            async {
                try {
                    Timber.d("Loading feature flags from storage")
                    storage.load()
                    Timber.d("Feature flags loaded from storage successfully")
                } catch (e: Throwable) {
                    Timber.e(e, "Failed to load feature flags from storage")
                }
            }
        }

        val result = try {
            Timber.d("Fetching feature flags from Amplitude")

            val experimentUser = ExperimentUser.builder()
                .userId(context.userId)
                .userProperties(context.userProperties)
                .build()

            Timber.d("Fetching feature flags from Amplitude")

            withContext(ioDispatcher) {
                client.fetch(experimentUser).get(5, TimeUnit.SECONDS)
            }

            Timber.d("Feature flags fetched successfully")

            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch feature flags")
            Result.failure(e)
        }

        storage.await()

        return result
    }

    override fun get(key: String): Variant {
        Timber.d("Getting feature flag: $key")

        val featureFlag = featureFlags[key]

        val cachedVariant = storage.getVariant(key)
        if (featureFlag?.isStickyBucketed == true && cachedVariant != null) {
            return cachedVariant
        }

        val lock = keyLocks.getOrPut(key) { ReentrantLock() }
        lock.lock()

        return try {
            val remoteVariant = client.variant(key)

            val currentVariant = Variant(
                key = key,
                value = remoteVariant.value ?: cachedVariant?.value ?: featureFlag?.defaultValue,
                payload = remoteVariant.payload?.toString() ?: featureFlag?.defaultValue
            )

            if (cachedVariant == null || cachedVariant != currentVariant) {
                scope.launch {
                    storage.setVariant(currentVariant)
                }

                client.exposure(key)
            }

            currentVariant
        } finally {
            lock.unlock()
            keyLocks.remove(key)
        }
    }
}
