package dev.mobase.amplitude

import android.content.Context
import com.amplitude.android.Amplitude
import dev.mobase.analytics.Analytics
import dev.mobase.analytics.AnalyticsEvent
import dev.mobase.consent.ConsentState

class AmplitudeAnalytics(
    apiKey: String,
    applicationContext: Context,
    optOut: Boolean = false,
) : Analytics {
    internal val amplitude = Amplitude(apiKey, applicationContext) {
        flushIntervalMillis = 10_000
        flushEventsOnClose = true
        this.optOut = optOut
    }

    override fun setUserId(userId: String?) {
        amplitude.setUserId(userId)
    }

    override fun setConsent(consent: ConsentState) {
        amplitude.configuration.optOut = !consent.analyticsStorage
    }

    override fun track(event: String, properties: Map<String, Any?>?) {
        amplitude.track(event, properties)
    }

    override fun track(event: AnalyticsEvent) {
        track(event.type, event.properties)
    }

    override fun setUserProperties(properties: Map<String, Any?>) {
        amplitude.identify(properties)
    }
}