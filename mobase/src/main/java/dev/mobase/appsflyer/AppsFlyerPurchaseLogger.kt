package dev.mobase.appsflyer

import com.appsflyer.AFInAppEventType
import dev.mobase.analytics.Analytics
import dev.mobase.analytics.PurchaseEventLogger
import dev.mobase.purchases.model.PurchaseTransaction

internal class AppsFlyerPurchaseLogger(
    private val analytics: Analytics,
) : PurchaseEventLogger {
    override fun trackCheckoutInitiated(productId: String) {
        analytics.track(AFInAppEventType.INITIATED_CHECKOUT)
    }

    override fun trackTrialStarted(productId: String) {
        analytics.track(AFInAppEventType.START_TRIAL)
    }

    override fun trackPurchaseCompleted(purchase: PurchaseTransaction) {
        analytics.track(
            AFInAppEventType.PURCHASE,
            mapOf("productId" to purchase.productId)
        )
    }
}