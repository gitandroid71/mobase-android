package dev.mobase.core.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.firstOrNull

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

internal class DefaultStorage(
    private val dataStore: DataStore<Preferences>
) : Storage {

    constructor(context: Context) : this(context.dataStore)

    override suspend fun isFirstLaunch(): Boolean {
        return dataStore.data.firstOrNull()
            ?.get(booleanPreferencesKey(KEY_IS_FIRST_INSTALL))
            ?: false
    }

    override suspend fun confirmFirstLaunch() {
        dataStore.edit {
            it[booleanPreferencesKey(KEY_IS_FIRST_INSTALL)] = true
        }
    }

    private companion object {
        private const val KEY_IS_FIRST_INSTALL = "is_first_install"
    }
}
