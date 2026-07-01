package dev.mobase.purchases.storage

import kotlinx.coroutines.flow.Flow
import dev.mobase.purchases.model.Entitlements

interface PurchasesStorage {
    val entitlements: Flow<Entitlements>

    suspend fun save(entitlements: Entitlements)
}
