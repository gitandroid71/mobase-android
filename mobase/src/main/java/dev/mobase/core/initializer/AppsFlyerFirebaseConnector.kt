package dev.mobase.core.initializer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import dev.mobase.appsflyer.AppsFlyer
import dev.mobase.common.Initializer
import dev.mobase.firebase.AppInstanceIdProvider
import dev.mobase.firebase.FirebaseAppInstanceIdProvider
import timber.log.Timber

internal class AppsFlyerFirebaseConnector(
    private val appsFlyer: AppsFlyer,
    private val appInstanceIdProvider: AppInstanceIdProvider = FirebaseAppInstanceIdProvider(),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) : Initializer {
    override fun initialize() {
        scope.launch {
            appInstanceIdProvider.get()
                .onSuccess { appsFlyer.setAdditionalData(mapOf("firebase_app_instance_id" to it)) }
                .onFailure { e -> Timber.e(e, "Failed to bind Firebase App Instance ID") }
        }
    }
}