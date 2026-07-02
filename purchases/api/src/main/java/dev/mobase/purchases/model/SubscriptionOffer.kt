package dev.mobase.purchases.model

data class SubscriptionOffer(
    val id: String,
    val price: Price,
    val period: SubscriptionPeriod,
)