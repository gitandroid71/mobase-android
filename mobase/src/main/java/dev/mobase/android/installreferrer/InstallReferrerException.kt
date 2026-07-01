package dev.mobase.android.installreferrer

internal class InstallReferrerException(
    val error: InstallReferrerError,
    override val message: String? = null,
    override val cause: Throwable? = null
) : Exception()