package dev.mobase.identity.ads

import android.content.Context
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

internal class AndroidAdvertisingIdProvider(
    private val applicationContext: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AdvertisingIdProvider {
    override suspend fun get(): AdvertisingId = withContext(ioDispatcher) {
        runCatching { AdvertisingIdClient.getAdvertisingIdInfo(applicationContext) }
            .fold(
                onSuccess = { info ->
                    val isZeroedId = info.id == ZERO_OUT_ID
                    val isLimitEnabled = info.isLimitAdTrackingEnabled || isZeroedId

                    AdvertisingId(
                        id = if (isLimitEnabled) null else info.id,
                        isLimitAdTrackingEnabled = isLimitEnabled
                    )
                },
                onFailure = { throwable ->
                    Timber.e(throwable, "Failed to get advertising id")
                    AdvertisingId(id = null, isLimitAdTrackingEnabled = true)
                }
            )
    }

    private companion object {
        const val ZERO_OUT_ID = "00000000-0000-0000-0000-000000000000"
    }
}