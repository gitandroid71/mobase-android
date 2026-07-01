package dev.mobase.core.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import dev.mobase.analytics.Analytics
import dev.mobase.core.analytics.AnalyticsProperties
import dev.mobase.purchases.Purchases

internal class EntitlementsEventObserver(
    private val purchases: Purchases,
    private val analytics: Analytics,
) : EventObserver {
    override fun launch(scope: CoroutineScope) {
        purchases.entitlements
            .distinctUntilChanged()
            .onEach { entitlements ->
                analytics.setUserProperties(
                    mapOf(
                        AnalyticsProperties.ACTIVE_SUBS to entitlements.activeSubscriptionIds,
                        AnalyticsProperties.PURCHASED_PRODUCT_IDS to entitlements.purchasedProductIds
                    )
                )
            }
            .launchIn(scope)
    }
}