package dev.mobase.firebase

import dev.mobase.analytics.Analytics
import dev.mobase.analytics.PurchaseEventLogger
import dev.mobase.purchases.model.PurchaseTransaction

internal class FirebasePurchaseEventLogger(
    val analytics: Analytics,
) : PurchaseEventLogger {
    override fun trackCheckoutInitiated(productId: String) {
        // No-op
    }

    override fun trackTrialStarted(productId: String) {
        analytics.track("trial_started")
    }

    override fun trackPurchaseCompleted(purchase: PurchaseTransaction) {
        analytics.track(
            event = "in_app_purchased",
            properties = mapOf(
                "productId" to purchase.productId,
                "purchaseId" to purchase.purchaseToken,
                "price" to purchase.product.price.amountMicros / 1_000_000,
                "currency" to purchase.product.price.currencyCode
            )
        )
    }
}