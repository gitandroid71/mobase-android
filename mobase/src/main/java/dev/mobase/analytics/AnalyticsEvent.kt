package dev.mobase.analytics

interface AnalyticsEvent {
    val type: String
    val properties: Map<String, Any?>?
}
