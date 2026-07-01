package dev.mobase.featureflags.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import dev.mobase.featureflags.model.Variant
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

// Prefixes and suffixes for DataStore keys
private const val VARIANT_PREFIX = "v:"
private const val VALUE_SUFFIX = "_value"
private const val PAYLOAD_SUFFIX = "_payload"

private val Context.dataStore by preferencesDataStore("feature_flags")

internal class DefaultFeatureFlagStorage(
    private val dataStore: DataStore<Preferences>
) : FeatureFlagsStorage {

    constructor(context: Context) : this(context.dataStore)

    private val mutex = Mutex()
    private val cache = ConcurrentHashMap<String, Variant>()

    override suspend fun load() {
        mutex.withLock {
            try {
                val initialData = try {
                    dataStore.data.first().toVariantMap()
                } catch (e: Exception) {
                    Timber.e(e, "Failed to read variants from disk")
                    emptyMap()
                }

                cache.clear()
                cache.putAll(initialData)
            } catch (e: Throwable) {
                Timber.e(e, "Failed to read variants from disk")
                throw e
            }
        }
    }

    override fun getVariant(key: String): Variant? = cache[key]

    override suspend fun setVariant(variant: Variant) {
        setVariants(listOf(variant))
    }

    override suspend fun setVariants(variants: List<Variant>) {
        variants.forEach { variant ->
            cache[variant.key] = variant
        }

        try {
            dataStore.edit { prefs ->
                variants.forEach { variant ->
                    prefs[valueKey(variant.key)] = variant.value ?: ""
                    prefs[payloadKey(variant.key)] = variant.payload ?: ""
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to write variants to disk")
        }
    }

    private fun Preferences.toVariantMap(): Map<String, Variant> {
        val builders = mutableMapOf<String, VariantBuilder>()

        asMap().forEach { (prefKey, value) ->
            val name = prefKey.name
            if (name.startsWith(VARIANT_PREFIX)) {
                val key = name.removePrefix(VARIANT_PREFIX)
                    .removeSuffix(VALUE_SUFFIX)
                    .removeSuffix(PAYLOAD_SUFFIX)

                val builder = builders.getOrPut(key) { VariantBuilder(key) }
                if (name.endsWith(VALUE_SUFFIX)) builder.value = value as? String
                else if (name.endsWith(PAYLOAD_SUFFIX)) builder.payload = value as? String
            }
        }

        return builders.mapValues { it.value.build() }
    }

    private class VariantBuilder(val key: String) {
        var value: String? = null
        var payload: String? = null
        fun build() = Variant(
            key = key,
            value = value?.takeIf { it.isNotEmpty() },
            payload = payload?.takeIf { it.isNotEmpty() }
        )
    }

    private fun valueKey(key: String) = stringPreferencesKey("$VARIANT_PREFIX$key$VALUE_SUFFIX")
    private fun payloadKey(key: String) = stringPreferencesKey("$VARIANT_PREFIX$key$PAYLOAD_SUFFIX")
}
