package dev.mobase.deviceinfo.theme

import android.content.Context
import android.content.res.Configuration
import timber.log.Timber

internal class AndroidDeviceThemeProvider(
    private val context: Context,
) : DeviceThemeProvider {
    override fun get(): DeviceTheme {
        return try {
            val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            when (uiMode) {
                Configuration.UI_MODE_NIGHT_YES -> DeviceTheme.DARK
                Configuration.UI_MODE_NIGHT_NO -> DeviceTheme.LIGHT
                else -> DeviceTheme.UNKNOWN
            }
        } catch (e: Throwable) {
            Timber.e(e, "Failed to read device theme")
            DeviceTheme.UNKNOWN
        }
    }
}