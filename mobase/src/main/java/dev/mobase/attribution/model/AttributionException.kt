package dev.mobase.attribution.model

class AttributionException(
    val error: AttributionError,
    message: String? = null,
) : Exception(message)