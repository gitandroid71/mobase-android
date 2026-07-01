package dev.mobase.featureflags.model

data class Variant(
    val key: String,
    val value: String?,
    val payload: String?,
)
