package dev.mobase.attribution.storage

import dev.mobase.attribution.model.AttributionData

interface AttributionStorage {
    suspend fun get(): AttributionData?
    suspend fun set(attribution: AttributionData)
}