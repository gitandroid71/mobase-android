package dev.mobase.featureflags.storage

import dev.mobase.featureflags.model.Variant

interface FeatureFlagsStorage {
    /** Reads all variants from DataStore into memory. Call once on app start. */
    suspend fun load()

    /** Returns the cached variant synchronously. Safe to call from any thread after [load]. */
    fun getVariant(key: String): Variant?

    /** Persists a single variant and updates the cache. */
    suspend fun setVariant(variant: Variant)

    /** Persists multiple variants and updates the cache. */
    suspend fun setVariants(variants: List<Variant>)
}
