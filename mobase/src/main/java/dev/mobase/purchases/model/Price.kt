package dev.mobase.purchases.model

data class Price(
    val amountMicros: Long,
    val currencyCode: String,
    val formattedAmount: String,
)