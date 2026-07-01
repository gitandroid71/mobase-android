package dev.mobase.featureflags.model

interface FeatureFlag {
    val key: String
    val defaultValue: String?
    val defaultPayload: String?
    val isStickyBucketed: Boolean

    companion object {
        fun create(
            key: String,
            defaultValue: String?,
            defaultPayload: String? = null,
            isStickyBucketed: Boolean = false
        ) = object : FeatureFlag {
            override val key = key
            override val defaultValue = defaultValue
            override val defaultPayload = defaultPayload
            override val isStickyBucketed = isStickyBucketed
        }
    }
}