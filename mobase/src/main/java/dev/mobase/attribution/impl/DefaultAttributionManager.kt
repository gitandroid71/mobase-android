package dev.mobase.attribution.impl

import android.content.Context
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import dev.mobase.attribution.AttributionManager
import dev.mobase.attribution.model.AttributionData
import dev.mobase.attribution.source.AttributionSource
import dev.mobase.attribution.storage.AttributionStorage
import dev.mobase.attribution.storage.DefaultAttributionStorage

internal class DefaultAttributionManager(
    context: Context,
    private val attributionSource: AttributionSource,
    private val attributionStorage: AttributionStorage = DefaultAttributionStorage.create(context),
) : AttributionManager {
    private val mutex = Mutex()

    @Volatile
    private var memoryCache: AttributionData? = null

    override suspend fun getAttribution(): AttributionData {
        return memoryCache ?: mutex.withLock {
            memoryCache?.let { return@withLock it }

            val storedAttribution = attributionStorage.get()
            if (storedAttribution != null) {
                memoryCache = storedAttribution
                return@withLock storedAttribution
            }

            val attribution = attributionSource.getAttribution()
                .getOrDefault(AttributionData.Companion.ORGANIC)

            attributionStorage.set(attribution)
            memoryCache = attribution
            attribution
        }
    }
}