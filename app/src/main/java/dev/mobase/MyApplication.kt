package dev.mobase

import android.app.Application
import dev.mobase.amplitude.AmplitudeAnalytics
import dev.mobase.analytics.plus
import dev.mobase.appsflyer.AppsFlyer
import dev.mobase.core.purchases.withAnalytics
import dev.mobase.featureflags.amplitude.AmplitudeFeatureFlags
import dev.mobase.firebase.FirebaseAnalytics
import dev.mobase.purchases.google.GooglePlayPurchases
import dev.mobase.purchases.google.identifiers.AccountIdentifiersProvider
import timber.log.Timber

class MyApplication : Application() {
    lateinit var mobase: Mobase

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        val amplitudeAnalytics = AmplitudeAnalytics(
            apiKey = "YOUR_API_KEY",
            applicationContext = this,
            optOut = true,
        )

        val appsFlyer = AppsFlyer(apiKey = "YOUR_API_KEY", applicationContext = this)
        appsFlyer.start(this)

        val firebase = FirebaseAnalytics()

        val analytics = amplitudeAnalytics + firebase

        val purchases = GooglePlayPurchases(
            applicationContext = this,
            accountIdentifiersProvider = AccountIdentifiersProvider.create(
                userId = appsFlyer.getAppsFlyerUID(),
                secretKey = "YOUR_SECRET_KEY",
                iv = "YOUR_IV"
            )
        ).withAnalytics(appsFlyer, firebase)

        val featureFlags = AmplitudeFeatureFlags(
            application = this,
            deploymentKey = "YOUR_API_KEY"
        )

        mobase = Mobase.builder(applicationContext = this)
            .setAppsFlyer(appsFlyer)
            .setAnalytics(analytics)
            .setPurchases(purchases)
            .setFeatureFlags(featureFlags)
            .build()

        mobase.initialize()
    }
}