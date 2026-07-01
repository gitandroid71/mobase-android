package dev.mobase.firebase

import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class FirebaseAppInstanceIdProvider : AppInstanceIdProvider {
    override suspend fun get(): Result<String> {
        return runCatching { Firebase.analytics.appInstanceId.await() }
            .onSuccess { Timber.d("Firebase app instance id: $it") }
            .onFailure { e -> Timber.e(e, "Failed to get firebase app instance id") }
    }
}