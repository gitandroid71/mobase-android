package dev.mobase.consent

data class ConsentState(
    val analyticsStorage: Boolean,
    val adUserData: Boolean,
    val adStorage: Boolean,
    val adPersonalization: Boolean,
) {
    companion object {
        val GRANTED_ALL = ConsentState(
            analyticsStorage = true,
            adUserData = true,
            adStorage = true,
            adPersonalization = true,
        )

        val DENIED_ALL = ConsentState(
            analyticsStorage = false,
            adUserData = false,
            adStorage = false,
            adPersonalization = false,
        )
    }
}