package dev.mobase.purchases.model

data class PurchasesError(
    val code: ErrorCode,
    val message: String?,
)

enum class ErrorCode {
    PURCHASE_CANCELLED,
    NETWORK_ERROR,
    SERVICE_UNAVAILABLE,
    API_UNAVAILABLE,
    UNKNOWN_ERROR
}

class PurchasesException(val error: PurchasesError) : Exception(error.message)