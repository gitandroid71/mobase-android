package dev.mobase.attribution

import dev.mobase.attribution.model.AttributionData

internal interface AttributionManager {
    suspend fun getAttribution(): AttributionData
}