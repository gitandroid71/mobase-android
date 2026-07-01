package dev.mobase.appupdate

import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.Application
import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import dev.mobase.appupdate.util.exitApplication
import dev.mobase.appupdate.util.versionCode
import timber.log.Timber

internal class GooglePlayAppUpdateManager(
    private val applicationContext: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) : AppUpdateManager {
    private val appUpdateManager by lazy {
        AppUpdateManagerFactory.create(applicationContext)
    }

    private val activityLifecycleCallback =
        ActivityLifecycleCallback(applicationContext as Application)

    init {
        activityLifecycleCallback.onActivityResultCallback = ::handleUpdateResult
    }

    override fun requestUpdate(minVersion: Long) {
        val versionCode = checkNotNull(applicationContext.versionCode) {
            "Failed to get version code"
        }

        if (versionCode > minVersion) {
            Timber.d("Update not required")
            return
        }

        scope.launch {
            requestUpdate()
        }
    }

    private suspend fun requestUpdate() {
        val appUpdateInfo = try {
            appUpdateManager.appUpdateInfo.await()
        } catch (e: Exception) {
            Timber.e(e, "Failed to get app update info")
            return
        }

        when (val updateAvailability = appUpdateInfo.updateAvailability()) {
            UpdateAvailability.UPDATE_AVAILABLE,
            UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                startUpdateFlow(appUpdateInfo)
            }

            UpdateAvailability.UPDATE_NOT_AVAILABLE -> {
                Timber.d("Update not available")
            }

            else -> {
                Timber.e("Unknown update availability: $updateAvailability")
            }
        }
    }

    private fun startUpdateFlow(appUpdateInfo: AppUpdateInfo) {
        val activity = activityLifecycleCallback.currentActivity
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            Timber.w("No active activity available for update flow")
            return
        }

        val launcher = activityLifecycleCallback.activityResultLauncher
        if (launcher == null) {
            Timber.w("No ActivityResultLauncher available, cannot start update flow")
            return
        }

        try {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                launcher,
                AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE),
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to start update flow")
        }
    }

    private fun handleUpdateResult(activity: Activity, resultCode: Int) {
        when (resultCode) {
            ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> {
                Timber.w("In-app update failed")

                scope.launch {
                    delay(RETRY_DELAY_MS)
                    requestUpdate()
                }
            }

            RESULT_CANCELED -> {
                Timber.d("In-app update canceled")
                activity.exitApplication()
            }

            else -> {
                Timber.e("Unknown result code: $resultCode")
            }
        }
    }

    private companion object {
        const val RETRY_DELAY_MS = 2000L
    }
}