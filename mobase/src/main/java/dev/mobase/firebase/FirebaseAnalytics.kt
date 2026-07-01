package dev.mobase.firebase

import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import dev.mobase.analytics.Analytics
import dev.mobase.analytics.AnalyticsEvent
import dev.mobase.consent.ConsentState
import dev.mobase.firebase.util.toBundle
import dev.mobase.firebase.util.toFirebaseConsent
import timber.log.Timber

class FirebaseAnalytics : Analytics {
    private val analytics by lazy { Firebase.analytics }

    override fun setUserId(userId: String?) {
        Timber.d("Setting user id: $userId")
        analytics.setUserId(userId)
    }

    override fun setConsent(consent: ConsentState) {
        analytics.setConsent(consent.toFirebaseConsent())
    }

    override fun track(event: AnalyticsEvent) {
        track(event.type, event.properties)
    }

    override fun track(event: String, properties: Map<String, Any?>?) {
        Timber.d("Tracking event: $event, properties: $properties")
        analytics.logEvent(event, properties?.toBundle())
    }

    override fun setUserProperties(properties: Map<String, Any?>) {
        Timber.d("Setting user properties: $properties")

        for ((key, value) in properties) {
            analytics.setUserProperty(key, value?.toString())
        }
    }
}