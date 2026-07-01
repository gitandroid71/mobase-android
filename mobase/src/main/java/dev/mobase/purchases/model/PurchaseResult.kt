package dev.mobase.purchases.model

sealed class PurchaseResult {
    class Success : PurchaseResult()
    data object Cancelled : PurchaseResult()
    data object Error : PurchaseResult()
}
