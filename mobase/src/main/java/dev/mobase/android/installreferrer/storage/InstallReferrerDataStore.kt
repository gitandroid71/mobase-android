package dev.mobase.android.installreferrer.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.firstOrNull

internal class InstallReferrerDataStore private constructor(
    private val dataStore: DataStore<Preferences>
) : InstallReferrerStorage {
    override suspend fun setReferrer(referrer: String?) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(KEY_REFERRER)] = referrer ?: ""
        }
    }

    override suspend fun getReferrer(): String? {
        return dataStore.data.firstOrNull()?.let { preferences ->
            preferences[stringPreferencesKey(KEY_REFERRER)]
        }
    }

    companion object {
        private const val KEY_REFERRER = "gp_install_referrer"

        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "install_referrer")

        fun create(context: Context): InstallReferrerStorage {
            return InstallReferrerDataStore(context.dataStore)
        }
    }
}