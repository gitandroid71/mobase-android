package dev.mobase.analytics

import dev.mobase.purchases.model.PurchaseTransaction

internal class CompositePurchaseEventLogger(
    internal val delegates: List<PurchaseEventLogger>
) : PurchaseEventLogger {
    override fun trackCheckoutInitiated(productId: String) {
        delegates.forEach { it.trackCheckoutInitiated(productId) }
    }

    override fun trackTrialStarted(productId: String) {
        delegates.forEach { it.trackTrialStarted(productId) }
    }

    override fun trackPurchaseCompleted(purchase: PurchaseTransaction) {
        delegates.forEach { it.trackPurchaseCompleted(purchase) }
    }
}

operator fun PurchaseEventLogger.plus(other: PurchaseEventLogger): PurchaseEventLogger {
    val providers = mutableListOf<PurchaseEventLogger>()

    if (this is CompositePurchaseEventLogger) {
        providers.addAll(this.delegates)
    } else {
        providers.add(this)
    }

    if (other is CompositePurchaseEventLogger) {
        providers.addAll(other.delegates)
    } else {
        providers.add(other)
    }

    return CompositePurchaseEventLogger(providers)
}