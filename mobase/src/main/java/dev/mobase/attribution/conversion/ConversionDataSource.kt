package dev.mobase.attribution.conversion

import kotlinx.coroutines.flow.StateFlow

interface ConversionDataSource {
    val conversionData: StateFlow<ConversionDataResult?>
}