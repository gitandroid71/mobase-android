package dev.mobase.purchases.google

import android.content.Context
import dev.mobase.purchases.Purchases
import dev.mobase.purchases.PurchasesFactory
import dev.mobase.purchases.google.identifiers.DefaultAccountIdentifiersProvider

class GooglePurchasesFactory(
    private val applicationContext: Context,
    private val config: PurchasesConfig
) : PurchasesFactory {
    override fun create(): Purchases {
        return GooglePlayPurchases(
            applicationContext,
            accountIdentifiersProvider = DefaultAccountIdentifiersProvider(
                userId = config.userId,
                secretKey = config.secretKey,
                iv = config.iv
            )
        )
    }
}