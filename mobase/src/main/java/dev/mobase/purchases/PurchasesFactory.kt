package dev.mobase.purchases

import android.content.Context
import dev.mobase.purchases.google.identifiers.DefaultAccountIdentifiersProvider
import dev.mobase.purchases.google.GooglePlayPurchases

interface PurchasesFactory {
    fun create(): Purchases
}

fun Purchases(
    context: Context,
    config: PurchasesConfig
): Purchases {
    val purchases = GooglePlayPurchases(
        applicationContext = context.applicationContext,
        accountIdentifiersProvider = DefaultAccountIdentifiersProvider(
            userId = config.userId,
            secretKey = config.secretKey,
            iv = config.iv
        )
    )
    purchases.initialize()
    return purchases
}
