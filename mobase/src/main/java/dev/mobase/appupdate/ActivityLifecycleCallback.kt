package dev.mobase.appupdate

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import java.lang.ref.WeakReference

internal class ActivityLifecycleCallback(
    application: Application
) : Application.ActivityLifecycleCallbacks {
    private var currentActivityReference: WeakReference<Activity>? = null
    private var resultLauncher: ActivityResultLauncher<IntentSenderRequest>? = null

    var onActivityResultCallback: ((Activity, Int) -> Unit)? = null

    val currentActivity: Activity?
        get() = currentActivityReference?.get()

    val activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>?
        get() = resultLauncher

    init {
        application.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity is ComponentActivity) {
            resultLauncher?.unregister()
            resultLauncher = activity.registerForActivityResult(
                ActivityResultContracts.StartIntentSenderForResult()
            ) { result ->
                onActivityResultCallback?.invoke(activity, result.resultCode)
            }
        }
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivityReference = WeakReference(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        if (currentActivityReference?.get() == activity) {
            currentActivityReference = null
        }
    }

    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivityReference?.get() == activity) {
            currentActivityReference = null
        }
        resultLauncher?.unregister()
        resultLauncher = null
    }
}