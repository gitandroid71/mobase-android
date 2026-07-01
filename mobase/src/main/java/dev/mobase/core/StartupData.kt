package dev.mobase.core

import dev.mobase.attribution.model.AttributionData
import dev.mobase.purchases.model.Entitlements

data class StartupData(
    val userId: String?,
    val entitlements: Entitlements?,
    val storeCountry: String?,
    val attribution: AttributionData,
)