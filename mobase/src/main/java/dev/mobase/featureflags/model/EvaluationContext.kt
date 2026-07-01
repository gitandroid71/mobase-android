package dev.mobase.featureflags.model

data class EvaluationContext(
    val userId: String?,
    val userProperties: Map<String, Any?>?
)