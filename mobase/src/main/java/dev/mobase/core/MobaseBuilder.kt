package dev.mobase.core

import android.content.Context
import dev.mobase.Mobase
import dev.mobase.analytics.Analytics
import dev.mobase.android.installreferrer.DefaultInstallReferrerProvider
import dev.mobase.android.installreferrer.storage.InstallReferrerDataStore
import dev.mobase.appsflyer.AppsFlyer
import dev.mobase.appsflyer.AppsFlyerAttributionSource
import dev.mobase.appsflyer.AppsFlyerUserIdProvider
import dev.mobase.appupdate.AppUpdateManager
import dev.mobase.appupdate.GooglePlayAppUpdateManager
import dev.mobase.attribution.AttributionManager
import dev.mobase.attribution.source.AttributionSource
import dev.mobase.attribution.source.CombinedAttributionSource
import dev.mobase.attribution.impl.DefaultAttributionManager
import dev.mobase.attribution.android.AndroidAttributionSource
import dev.mobase.attribution.android.GooglePlayInstallReferrerParser
import dev.mobase.common.Initializer
import dev.mobase.core.event.AppsFlyerConversionEventObserver
import dev.mobase.core.event.EntitlementsEventObserver
import dev.mobase.core.event.EventObserver
import dev.mobase.core.initializer.AppsFlyerFirebaseConnector
import dev.mobase.core.orchestrator.DefaultStartupOrchestrator
import dev.mobase.core.storage.DefaultStorage
import dev.mobase.core.usecase.AppLaunchTask
import dev.mobase.deviceinfo.DeviceInfoProviderFactory
import dev.mobase.featureflags.FeatureFlags
import dev.mobase.identity.ads.AndroidAdvertisingIdProvider
import dev.mobase.identity.appset.AndroidAppSetIdProvider
import dev.mobase.identity.user.UserIdProvider
import dev.mobase.purchases.Purchases

internal class MobaseBuilder(private val applicationContext: Context) : Mobase.Builder {
    private var userIdProvider: UserIdProvider = UserIdProvider { null }
    private val initializers = mutableListOf<Initializer>()
    private val eventObservers = mutableListOf<EventObserver>()

    private val attributionSources = mutableListOf<AttributionSource>()

    private var appsFlyer: AppsFlyer? = null
    private var analytics: Analytics? = null
    private var purchases: Purchases? = null
    private var featureFlags: FeatureFlags? = null
    private var updateManager: AppUpdateManager? = null

    override fun setAppsFlyer(appsFlyer: AppsFlyer) = apply {
        this.appsFlyer = appsFlyer
        this.userIdProvider = AppsFlyerUserIdProvider(appsFlyer)
        this.initializers.add(appsFlyer)
        this.initializers.add(AppsFlyerFirebaseConnector(appsFlyer))
        this.attributionSources.add(AppsFlyerAttributionSource(conversionDataSource = appsFlyer))
    }

    override fun setAnalytics(analytics: Analytics) = apply {
        this.analytics = analytics
    }

    override fun setPurchases(purchases: Purchases) = apply {
        this.purchases = purchases
    }

    override fun setFeatureFlags(featureFlags: FeatureFlags) = apply {
        this.featureFlags = featureFlags
    }

    override fun setAppUpdateManager(appUpdateManager: AppUpdateManager) = apply {
        this.updateManager = appUpdateManager
    }

    override fun build(): Mobase {
        val analytics = requireNotNull(analytics) {
            "Analytics must be set. Call setAnalytics()."
        }
        val purchases = requireNotNull(purchases) {
            "Purchases must be set. Call setPurchases()."
        }
        val featureFlags = requireNotNull(featureFlags) {
            "FeatureFlags must be set. Call setFeatureFlags()."
        }

        val updateManager = updateManager ?: GooglePlayAppUpdateManager(applicationContext)

        val storage = DefaultStorage(applicationContext)
        val attributionManager = createAttributionManager()

        // Setup identity management
        val identityManager = DefaultIdentityManager(analytics = listOf(analytics))

        initializers.add(
            Initializer { identityManager.setUserId(userIdProvider.provide()) }
        )

        // Setup observers
        appsFlyer?.let {
            eventObservers.add(AppsFlyerConversionEventObserver(it, analytics))
        }

        eventObservers.add(EntitlementsEventObserver(purchases, analytics))

        val appLaunchTask = AppLaunchTask(
            storage = storage,
            deviceInfoProvider = DeviceInfoProviderFactory.create(applicationContext),
            analytics = analytics,
        )

        val startupOrchestrator = DefaultStartupOrchestrator(
            userIdProvider = userIdProvider,
            analytics = analytics,
            purchases = purchases,
            featureFlags = featureFlags,
            attributionManager = attributionManager,
            updateManager = updateManager,
            advertisingIdProvider = AndroidAdvertisingIdProvider(applicationContext),
            appSetIdProvider = AndroidAppSetIdProvider(applicationContext),
            appLaunchTask = appLaunchTask
        )

        return MobaseApplication(
            initializers = initializers.toList(),
            eventObservers = eventObservers.toList(),
            startupOrchestrator = startupOrchestrator
        )
    }

    private fun createAttributionManager(): AttributionManager {
        attributionSources.add(
            AndroidAttributionSource(
                installReferrerProvider = DefaultInstallReferrerProvider(applicationContext),
                installReferrerParser = GooglePlayInstallReferrerParser(),
                installReferrerStorage = InstallReferrerDataStore.create(applicationContext)
            )
        )

        return DefaultAttributionManager(
            context = applicationContext,
            attributionSource = CombinedAttributionSource(attributionSources)
        )
    }
}