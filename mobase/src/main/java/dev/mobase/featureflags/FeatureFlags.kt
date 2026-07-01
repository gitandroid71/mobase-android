package dev.mobase.featureflags

import dev.mobase.featureflags.model.EvaluationContext
import dev.mobase.featureflags.model.Variant

interface FeatureFlags {
    suspend fun fetch(context: EvaluationContext): Result<Unit>

    operator fun get(key: String): Variant
}