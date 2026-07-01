package dev.mobase.attribution.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import dev.mobase.attribution.parser.MediaSourceParser
import dev.mobase.attribution.model.AttributionData

internal class DefaultAttributionStorage(
    private val dataStore: DataStore<Preferences>,
    private val mediaSourceParser: MediaSourceParser = MediaSourceParser.create()
) : AttributionStorage {
    private object Keys {
        val mediaSource = stringPreferencesKey("media_source")
        val campaign = stringPreferencesKey("campaign")
        val adGroup = stringPreferencesKey("ad_group")
        val referrerUrl = stringPreferencesKey("referrer_url")
        val deepLinkValue = stringPreferencesKey("deep_link_value")
    }

    override suspend fun set(attribution: AttributionData) {
        dataStore.edit { preferences ->
            preferences[Keys.mediaSource] = attribution.mediaSource?.value.orEmpty()
            preferences[Keys.campaign] = attribution.campaign ?: ""
            preferences[Keys.adGroup] = attribution.adGroup ?: ""
            preferences[Keys.referrerUrl] = attribution.referrerUrl ?: ""
            preferences[Keys.deepLinkValue] = attribution.deepLinkValue ?: ""
        }
    }

    override suspend fun get(): AttributionData? {
        return dataStore.data.map { preferences ->
            val mediaSource = preferences[Keys.mediaSource]
            if (mediaSource.isNullOrEmpty()) return@map null

            AttributionData(
                mediaSource = mediaSourceParser.parse(mediaSource),
                campaign = preferences[Keys.campaign]?.takeIf { it.isNotEmpty() },
                adGroup = preferences[Keys.adGroup]?.takeIf { it.isNotEmpty() },
                referrerUrl = preferences[Keys.referrerUrl]?.takeIf { it.isNotEmpty() },
                deepLinkValue = preferences[Keys.deepLinkValue]?.takeIf { it.isNotEmpty() }
            )
        }.firstOrNull()
    }

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "attribution")

        fun create(context: Context): AttributionStorage {
            return DefaultAttributionStorage(context.dataStore)
        }
    }
}
