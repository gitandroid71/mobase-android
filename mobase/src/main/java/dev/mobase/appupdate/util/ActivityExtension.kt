package dev.mobase.appupdate.util

import android.app.Activity
import timber.log.Timber
import kotlin.system.exitProcess

internal fun Activity.exitApplication() {
    try {
        finishAffinity()
    } catch (e: Exception) {
        Timber.e(e, "Failed to exit application")
    } finally {
        exitProcess(0)
    }
}
