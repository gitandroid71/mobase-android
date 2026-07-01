package dev.mobase.firebase.util

import com.google.firebase.analytics.FirebaseAnalytics.ConsentStatus
import com.google.firebase.analytics.FirebaseAnalytics.ConsentType
import dev.mobase.consent.ConsentState

internal fun ConsentState.toFirebaseConsent(): Map<ConsentType, ConsentStatus> {
    fun Boolean.toConsentStatus() = if (this) ConsentStatus.GRANTED else ConsentStatus.DENIED

    return mapOf(
        ConsentType.ANALYTICS_STORAGE to analyticsStorage.toConsentStatus(),
        ConsentType.AD_USER_DATA to adUserData.toConsentStatus(),
        ConsentType.AD_PERSONALIZATION to adPersonalization.toConsentStatus(),
        ConsentType.AD_STORAGE to adStorage.toConsentStatus(),
    )
}