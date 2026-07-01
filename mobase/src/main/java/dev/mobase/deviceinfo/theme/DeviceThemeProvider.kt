package dev.mobase.deviceinfo.theme

internal fun interface DeviceThemeProvider {
    fun get(): DeviceTheme
}