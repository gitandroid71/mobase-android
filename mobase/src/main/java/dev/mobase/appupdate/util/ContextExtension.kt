package dev.mobase.appupdate.util

import android.content.Context
import android.os.Build
import timber.log.Timber

internal val Context.versionCode: Long?
    get() = try {
        val packageInfo = applicationContext.packageManager
            .getPackageInfo(applicationContext.packageName, 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        }
    } catch (e: Exception) {
        Timber.e(e, "An error occurred while getting app version code")
        null
    }
