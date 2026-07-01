package dev.mobase.core.purchases

import android.app.Activity
import dev.mobase.analytics.PurchaseEventLogger
import dev.mobase.purchases.Purchases
import dev.mobase.purchases.model.PurchaseTransaction

internal class Purchases(
    private val delegate: Purchases,
    private val eventLogger: PurchaseEventLogger
) : Purchases by delegate {
    override suspend fun purchase(
        activity: Activity,
        productId: String
    ): Result<PurchaseTransaction> {
        eventLogger.trackCheckoutInitiated(productId)
        return delegate.purchase(activity, productId).onSuccess { purchase ->
            if (purchase.product.subscriptionInfo?.trialPeriod != null) {
                eventLogger.trackTrialStarted(productId)
            } else {
                eventLogger.trackPurchaseCompleted(purchase)
            }
        }
    }
}