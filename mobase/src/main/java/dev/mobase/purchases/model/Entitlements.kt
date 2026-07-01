package dev.mobase.purchases.model

data class Entitlements(
    val purchasedProductIds: Set<String> = emptySet(),
    val activeSubscriptionIds: Set<String> = emptySet()
)
