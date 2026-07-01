package dev.mobase.attribution.conversion

sealed interface ConversionDataResult {
    data class Success(val data: Map<String, Any?>?) : ConversionDataResult
    data class Failure(val error: String?) : ConversionDataResult
}