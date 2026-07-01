package dev.mobase.attribution.source

import dev.mobase.attribution.model.AttributionData

interface AttributionSource {
    suspend fun getAttribution(): Result<AttributionData>
}