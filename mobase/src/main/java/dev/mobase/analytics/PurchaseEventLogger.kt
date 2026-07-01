package dev.mobase.analytics

import dev.mobase.purchases.model.PurchaseTransaction

interface PurchaseEventLogger {
    fun trackCheckoutInitiated(productId: String)

    fun trackTrialStarted(productId: String)

    fun trackPurchaseCompleted(purchase: PurchaseTransaction)
}