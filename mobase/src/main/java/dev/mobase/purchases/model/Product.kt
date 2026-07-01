package dev.mobase.purchases.model

data class Product(
    val id: String,
    val title: String,
    val description: String,
    val price: Price,
    val type: Type,
    val subscriptionInfo: SubscriptionInfo? = null,
) {

    enum class Type {
        IN_APP, SUBSCRIPTION
    }

    data class SubscriptionInfo(
        val basePlanId: String,
        val period: SubscriptionPeriod,
        val offerToken: String? = null,
        val trialPeriod: SubscriptionPeriod? = null
    )
}