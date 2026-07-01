package dev.mobase.analytics

import dev.mobase.consent.ConsentState

interface Analytics {
    fun setUserId(userId: String?)

    fun setConsent(consent: ConsentState)

    fun track(event: AnalyticsEvent)

    fun track(event: String, properties: Map<String, Any?>? = null)

    fun setUserProperties(properties: Map<String, Any?>)
}