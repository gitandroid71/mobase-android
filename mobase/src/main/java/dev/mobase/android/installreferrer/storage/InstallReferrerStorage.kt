package dev.mobase.android.installreferrer.storage

internal interface InstallReferrerStorage {
    suspend fun setReferrer(referrer: String?)

    suspend fun getReferrer(): String?
}