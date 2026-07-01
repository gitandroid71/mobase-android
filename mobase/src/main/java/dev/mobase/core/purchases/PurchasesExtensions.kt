package dev.mobase.core.purchases

import dev.mobase.analytics.plus
import dev.mobase.appsflyer.AppsFlyer
import dev.mobase.appsflyer.AppsFlyerPurchaseLogger
import dev.mobase.firebase.FirebaseAnalytics
import dev.mobase.firebase.FirebasePurchaseEventLogger
import dev.mobase.purchases.Purchases

fun Purchases.withAnalytics(
    appsFlyer: AppsFlyer,
    firebase: FirebaseAnalytics,
): Purchases {
    val appsFlyerPurchaseLogger = AppsFlyerPurchaseLogger(appsFlyer)
    val firebasePurchaseEventLogger = FirebasePurchaseEventLogger(firebase)

    return Purchases(
        delegate = this,
        eventLogger = appsFlyerPurchaseLogger + firebasePurchaseEventLogger
    )
}