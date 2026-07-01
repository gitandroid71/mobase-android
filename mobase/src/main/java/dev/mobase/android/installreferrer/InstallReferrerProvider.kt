package dev.mobase.android.installreferrer

internal interface InstallReferrerProvider {
    suspend fun getInstallReferrer(): Result<InstallReferrerDetails>
}