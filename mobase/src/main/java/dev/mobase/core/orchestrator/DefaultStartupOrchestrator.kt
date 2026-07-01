package dev.mobase.core.orchestrator

import dev.mobase.BuildConfig
import dev.mobase.analytics.Analytics
import dev.mobase.appupdate.AppUpdateManager
import dev.mobase.attribution.AttributionManager
import dev.mobase.attribution.model.AttributionData
import dev.mobase.common.coroutines.util.getOrNull
import dev.mobase.core.StartupData
import dev.mobase.core.analytics.AnalyticsEvents.ATTRIBUTION
import dev.mobase.core.analytics.AnalyticsEvents.ATTRIBUTION_STARTED
import dev.mobase.core.analytics.AnalyticsEvents.FRAMEWORK_FINISHED
import dev.mobase.core.analytics.AnalyticsProperties.ACTIVE_SUBS
import dev.mobase.core.analytics.AnalyticsProperties.AD
import dev.mobase.core.analytics.AnalyticsProperties.ADVERTISING_ID
import dev.mobase.core.analytics.AnalyticsProperties.AD_GROUP
import dev.mobase.core.analytics.AnalyticsProperties.ANDROID_FRAMEWORK_VERSION
import dev.mobase.core.analytics.AnalyticsProperties.APPSFLYER_SDK_VERSION
import dev.mobase.core.analytics.AnalyticsProperties.APP_SET_ID
import dev.mobase.core.analytics.AnalyticsProperties.ATTRIBUTION_SOURCE
import dev.mobase.core.analytics.AnalyticsProperties.CAMPAIGN
import dev.mobase.core.analytics.AnalyticsProperties.DEEP_LINK_VALUE
import dev.mobase.core.analytics.AnalyticsProperties.IS_LIMIT_AD_TRACKING_ENABLED
import dev.mobase.core.analytics.AnalyticsProperties.MEDIA_SOURCE_TYPE
import dev.mobase.core.analytics.AnalyticsProperties.NETWORK
import dev.mobase.core.analytics.AnalyticsProperties.PURCHASED_PRODUCT_IDS
import dev.mobase.core.analytics.AnalyticsProperties.STORE_COUNTRY
import dev.mobase.core.analytics.AnalyticsProperties.USER_ID
import dev.mobase.core.featureflags.FeatureFlags.MIN_SUPPORTED_APP_VERSION
import dev.mobase.core.usecase.AppLaunchTask
import dev.mobase.featureflags.FeatureFlags
import dev.mobase.featureflags.model.EvaluationContext
import dev.mobase.identity.ads.AdvertisingIdProvider
import dev.mobase.identity.appset.AppSetIdProvider
import dev.mobase.identity.user.UserIdProvider
import dev.mobase.purchases.Purchases
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber

internal class DefaultStartupOrchestrator(
    private val userIdProvider: UserIdProvider,
    private val analytics: Analytics,
    private val appLaunchTask: AppLaunchTask,
    private val purchases: Purchases,
    private val featureFlags: FeatureFlags,
    private val attributionManager: AttributionManager,
    private val updateManager: AppUpdateManager,
    private val advertisingIdProvider: AdvertisingIdProvider,
    private val appSetIdProvider: AppSetIdProvider,
) : StartupOrchestrator {
    override suspend fun start(): StartupData = coroutineScope {
        Timber.d("Startup orchestrator started")

        val userId = userIdProvider.provide()
        Timber.d("User ID resolved: $userId")

        val startupProperties = mapOf(
            USER_ID to userId,
            ANDROID_FRAMEWORK_VERSION to BuildConfig.VERSION,
            APPSFLYER_SDK_VERSION to BuildConfig.APPSFLYER_SDK_VERSION
        )

        analytics.setUserProperties(startupProperties)

        launch {
            Timber.d("App launch task started")
            appLaunchTask.execute()
            Timber.d("App launch task completed")
        }

        val appSetIdJob = async {
            Timber.d("Fetching app set ID")
            val result = withTimeoutOrNull(TIMEOUT_MS) {
                appSetIdProvider.get()
            }
            if (result == null) {
                Timber.w("App set ID fetch timed out")
            } else {
                Timber.d("App set ID fetched: $result")
            }
            result
        }

        val advertisingIdJob = async {
            Timber.d("Fetching advertising ID")

            val result = withTimeoutOrNull(TIMEOUT_MS) {
                advertisingIdProvider.get()
            }

            if (result == null) {
                Timber.w("Advertising ID fetch timed out")
            } else {
                Timber.d("Advertising ID fetched: ${result.id}, LAT=${result.isLimitAdTrackingEnabled}")
            }
            result
        }

        val storeCountryJob = async {
            Timber.d("Fetching store country")
            val result = withTimeoutOrNull(TIMEOUT_MS) {
                purchases.getStorefront().getOrNull()?.countryCode
            }

            if (result == null) {
                Timber.w("Store country fetch timed out or unavailable")
            } else {
                Timber.d("Store country: $result")
            }
            result
        }

        val entitlementsJob = async {
            Timber.d("Fetching entitlements")

            val result = withTimeoutOrNull(TIMEOUT_MS) {
                purchases.getEntitlements().getOrNull()
            }

            if (result == null) {
                Timber.w("Entitlements fetch timed out or unavailable")
            } else {
                Timber.d("Entitlements fetched")
            }

            result
        }

        val attributionJob = async {
            Timber.d("Attribution started")

            analytics.track(
                event = ATTRIBUTION_STARTED,
                properties = startupProperties
            )

            val attribution = withTimeoutOrNull(ATTRIBUTION_TIMEOUT_MS) {
                attributionManager.getAttribution()
            } ?: run {
                Timber.w("Attribution fetch timed out, falling back to Organic")
                AttributionData.ORGANIC
            }

            Timber.d(
                "Attribution resolved: source=%s, network=%s, campaign=%s",
                attribution.attributionSource,
                attribution.mediaSource.value,
                attribution.campaign
            )

            val attributionProperties = mapOf(
                USER_ID to userId,
                APP_SET_ID to appSetIdJob.await(),
                ADVERTISING_ID to advertisingIdJob.await()?.id,
                IS_LIMIT_AD_TRACKING_ENABLED to advertisingIdJob.await()?.isLimitAdTrackingEnabled,
                NETWORK to attribution.mediaSource.value,
                MEDIA_SOURCE_TYPE to attribution.mediaSource.type.value,
                CAMPAIGN to attribution.campaign,
                AD_GROUP to attribution.adGroup,
                AD to attribution.adSet,
                DEEP_LINK_VALUE to attribution.deepLinkValue,
                ATTRIBUTION_SOURCE to attribution.attributionSource,
                STORE_COUNTRY to storeCountryJob.await()
            )

            analytics.setUserProperties(attributionProperties)
            analytics.track(event = ATTRIBUTION, properties = attributionProperties)

            Timber.d("Attribution tracked")

            attribution to attributionProperties
        }

        val featureFlagsJob = async {
            Timber.d("Fetching feature flags")
            val attributionProperties = attributionJob.await().second
            val storeCountry = storeCountryJob.await()

            val userProperties = attributionProperties + mapOf(
                STORE_COUNTRY to storeCountry,
                ANDROID_FRAMEWORK_VERSION to BuildConfig.VERSION,
            )

            featureFlags.fetch(
                context = EvaluationContext(
                    userId = userId,
                    userProperties = userProperties
                )
            )

            Timber.d("Feature flags fetched")
        }

        val startupData = try {
            withTimeout(STARTUP_TIMEOUT_MS) {
                val attribution = attributionJob.await().first
                featureFlagsJob.await()

                val startupData = StartupData(
                    userId = userId,
                    entitlements = entitlementsJob.await(),
                    storeCountry = storeCountryJob.await(),
                    attribution = attribution
                )
                Timber.d("Startup completed successfully")
                startupData
            }
        } catch (e: Throwable) {
            if (e is TimeoutCancellationException) {
                Timber.w(
                    e,
                    "Startup timed out after ${STARTUP_TIMEOUT_MS}ms, returning partial data"
                )
            } else {
                Timber.e(e, "Startup failed with unexpected error, returning partial data")
            }

            StartupData(
                userId = userId,
                entitlements = entitlementsJob.getOrNull(),
                storeCountry = storeCountryJob.getOrNull(),
                attribution = attributionJob.getOrNull()?.first ?: AttributionData.ORGANIC,
            )
        } finally {
            launch {
                val minVersion = featureFlags[MIN_SUPPORTED_APP_VERSION].value?.toLong() ?: 0L
                Timber.d("Requesting app update check, minVersion=$minVersion")
                updateManager.requestUpdate(minVersion = minVersion)
            }
        }

        val userProperties = attributionJob.getOrNull()?.second.orEmpty()

        analytics.track(
            event = FRAMEWORK_FINISHED,
            properties = mapOf(
                USER_ID to userId,
                STORE_COUNTRY to startupData.storeCountry,
                ACTIVE_SUBS to startupData.entitlements?.activeSubscriptionIds,
                PURCHASED_PRODUCT_IDS to startupData.entitlements?.purchasedProductIds,
            ) + userProperties
        )

        Timber.d("Framework finished event tracked")

        startupData
    }

    private companion object {
        const val STARTUP_TIMEOUT_MS = 6000L
        const val ATTRIBUTION_TIMEOUT_MS = STARTUP_TIMEOUT_MS - 1000L
        const val TIMEOUT_MS = ATTRIBUTION_TIMEOUT_MS - 1000L
    }
}