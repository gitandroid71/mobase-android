package dev.mobase.purchases.google.util

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult

internal val BillingResult.isSuccess: Boolean
    get() = responseCode == BillingClient.BillingResponseCode.OK

internal val BillingResult.isFailure: Boolean
    get() = responseCode != BillingClient.BillingResponseCode.OK