package dev.mobase.deviceinfo

import android.content.Context
import dev.mobase.deviceinfo.display.AndroidDisplayInfoProvider
import dev.mobase.deviceinfo.memory.android.AndroidMemoryInfoProvider
import dev.mobase.deviceinfo.storage.AndroidStorageInfoProvider
import dev.mobase.deviceinfo.theme.AndroidDeviceThemeProvider

internal object DeviceInfoProviderFactory {
    fun create(context: Context): DeviceInfoProvider {
        return AndroidDeviceInfoProvider(
            deviceThemeProvider = AndroidDeviceThemeProvider(context),
            memoryInfoProvider = AndroidMemoryInfoProvider(context),
            storageInfoProvider = AndroidStorageInfoProvider(context),
            displayInfoProvider = AndroidDisplayInfoProvider(context),
        )
    }
}