package dev.mobase.core.analytics

interface AnalyticsPropertiesMapper<T> {
    fun map(value: T): Map<String, Any>
}