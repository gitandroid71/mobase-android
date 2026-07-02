package dev.mobase.purchases.google

import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingResult
import dev.mobase.purchases.model.ErrorCode
import dev.mobase.purchases.model.PurchasesError
import dev.mobase.purchases.model.PurchasesException

internal fun PurchasesException(billingResult: BillingResult): PurchasesException {
    return PurchasesException(
        billingResult.responseCode,
        billingResult.debugMessage
    )
}

internal fun PurchasesException(
    @BillingResponseCode responseCode: Int,
    debugMessage: String?,
): PurchasesException {
    val errorCode = when (responseCode) {
        BillingResponseCode.ERROR -> ErrorCode.UNKNOWN_ERROR
        BillingResponseCode.NETWORK_ERROR -> ErrorCode.NETWORK_ERROR
        BillingResponseCode.SERVICE_UNAVAILABLE -> ErrorCode.SERVICE_UNAVAILABLE
        BillingResponseCode.BILLING_UNAVAILABLE -> ErrorCode.API_UNAVAILABLE
        BillingResponseCode.USER_CANCELED -> ErrorCode.PURCHASE_CANCELLED
        else -> ErrorCode.UNKNOWN_ERROR
    }

    val error = PurchasesError(errorCode, debugMessage)
    return PurchasesException(error)
}