package dev.mobase.android.installreferrer

internal enum class InstallReferrerError {
    NETWORK_ERROR,
    FEATURE_NOT_SUPPORTED,
    SERVICE_UNAVAILABLE,
    SERVICE_DISCONNECTED,
    TIMEOUT,
    UNKNOWN
}