package dev.mobase.identity.appset

import android.content.Context
import com.google.android.gms.appset.AppSet
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber

internal class AndroidAppSetIdProvider(
    private val applicationContext: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AppSetIdProvider {
    override suspend fun get(): String? = withContext(ioDispatcher) {
        runCatching { AppSet.getClient(applicationContext).appSetIdInfo.await() }
            .fold(
                onSuccess = { info -> info.id },
                onFailure = { e ->
                    Timber.e(e, "Failed to get app set id")
                    null
                }
            )
    }
}