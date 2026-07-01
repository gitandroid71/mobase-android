package dev.mobase.analytics

import dev.mobase.consent.ConsentState

class CompositeAnalytics(
    internal val analytics: List<Analytics>
) : Analytics {
    override fun setUserId(userId: String?) {
        analytics.forEach { it.setUserId(userId) }
    }

    override fun setConsent(consent: ConsentState) {
        analytics.forEach { it.setConsent(consent) }
    }

    override fun track(event: AnalyticsEvent) {
        analytics.forEach { it.track(event) }
    }

    override fun track(event: String, properties: Map<String, Any?>?) {
        analytics.forEach { it.track(event, properties) }
    }

    override fun setUserProperties(properties: Map<String, Any?>) {
        analytics.forEach { it.setUserProperties(properties) }
    }
}

operator fun Analytics.plus(other: Analytics): Analytics {
    val providers = mutableListOf<Analytics>()

    if (this is CompositeAnalytics) {
        providers.addAll(this.analytics)
    } else {
        providers.add(this)
    }

    if (other is CompositeAnalytics) {
        providers.addAll(other.analytics)
    } else {
        providers.add(other)
    }

    return CompositeAnalytics(providers)
}