package dev.mobase

import android.content.Context
import dev.mobase.analytics.Analytics
import dev.mobase.appsflyer.AppsFlyer
import dev.mobase.appupdate.AppUpdateManager
import dev.mobase.featureflags.FeatureFlags
import dev.mobase.purchases.Purchases
import dev.mobase.core.AppInitializer
import dev.mobase.core.MobaseBuilder

interface Mobase : AppInitializer {

    interface Builder {
        fun setAppsFlyer(appsFlyer: AppsFlyer): Builder

        fun setAnalytics(analytics: Analytics): Builder

        fun setPurchases(purchases: Purchases): Builder

        fun setFeatureFlags(featureFlags: FeatureFlags): Builder

        fun setAppUpdateManager(appUpdateManager: AppUpdateManager): Builder

        fun build(): Mobase
    }

    companion object {
        fun builder(applicationContext: Context): Builder = MobaseBuilder(applicationContext)
    }
}