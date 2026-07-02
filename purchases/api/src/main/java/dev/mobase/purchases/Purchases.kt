package dev.mobase.purchases

import android.app.Activity
import kotlinx.coroutines.flow.Flow
import dev.mobase.purchases.model.Entitlements
import dev.mobase.purchases.model.Product
import dev.mobase.purchases.model.Storefront
import dev.mobase.purchases.model.PurchaseTransaction

interface Purchases {
    val entitlements: Flow<Entitlements>

    suspend fun getStorefront(): Result<Storefront>

    suspend fun getProducts(productIds: List<String>): Result<List<Product>>

    suspend fun getEntitlements(): Result<Entitlements>

    suspend fun purchase(activity: Activity, productId: String): Result<PurchaseTransaction>

    suspend fun restore(): Result<Entitlements>
}