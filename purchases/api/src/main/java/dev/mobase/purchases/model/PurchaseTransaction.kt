package dev.mobase.purchases.model

data class PurchaseTransaction(
    val productId: String,
    val orderId: String?,
    val purchaseToken: String,
    val product: Product,
)