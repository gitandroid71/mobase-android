package dev.mobase.purchases.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import dev.mobase.purchases.model.Entitlements

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "purchases")

internal class DefaultPurchasesStorage(
    private val dataStore: DataStore<Preferences>
) : PurchasesStorage {

    constructor(context: Context) : this(context.dataStore)

    private val productIdsKey = stringSetPreferencesKey("product_ids")
    private val activeSubscriptionIdsKey = stringSetPreferencesKey("active_subscription_ids")

    override val entitlements: Flow<Entitlements> = dataStore.data.map { prefs ->
        Entitlements(
            purchasedProductIds = prefs[productIdsKey] ?: emptySet(),
            activeSubscriptionIds = prefs[activeSubscriptionIdsKey] ?: emptySet(),
        )
    }

    override suspend fun save(entitlements: Entitlements) {
        dataStore.edit { prefs ->
            prefs[productIdsKey] = entitlements.purchasedProductIds
            prefs[activeSubscriptionIdsKey] = entitlements.activeSubscriptionIds
        }
    }
}
