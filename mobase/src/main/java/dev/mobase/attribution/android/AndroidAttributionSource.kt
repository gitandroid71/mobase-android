package dev.mobase.attribution.android

import android.content.Context
import dev.mobase.android.installreferrer.DefaultInstallReferrerProvider
import dev.mobase.android.installreferrer.storage.InstallReferrerDataStore
import dev.mobase.android.installreferrer.InstallReferrerProvider
import dev.mobase.android.installreferrer.storage.InstallReferrerStorage
import dev.mobase.attribution.source.AttributionSource
import dev.mobase.attribution.model.AttributionData
import timber.log.Timber

internal class AndroidAttributionSource(
    private val installReferrerProvider: InstallReferrerProvider,
    private val installReferrerParser: InstallReferrerParser,
    private val installReferrerStorage: InstallReferrerStorage,
) : AttributionSource {

    override suspend fun getAttribution(): Result<AttributionData> {
        val installReferrer = installReferrerStorage.getReferrer()
        if (installReferrer != null) {
            return Result.success(installReferrerParser.parse(installReferrer))
        }

        return installReferrerProvider.getInstallReferrer().fold(
            onSuccess = {
                installReferrerStorage.setReferrer(it.installReferrer)
                Result.success(installReferrerParser.parse(it.installReferrer))
            },
            onFailure = { e ->
                Timber.e(e, "Failed to get install referrer")
                Result.failure(e)
            }
        )
    }

    companion object {
        fun create(context: Context): AttributionSource {
            return AndroidAttributionSource(
                installReferrerProvider = DefaultInstallReferrerProvider(context),
                installReferrerParser = GooglePlayInstallReferrerParser(),
                installReferrerStorage = InstallReferrerDataStore.create(context)
            )
        }
    }
}