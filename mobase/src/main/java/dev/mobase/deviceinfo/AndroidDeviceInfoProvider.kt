package dev.mobase.deviceinfo

import dev.mobase.deviceinfo.display.DisplayInfoProvider
import dev.mobase.deviceinfo.memory.MemoryInfoProvider
import dev.mobase.deviceinfo.model.DeviceInfo
import dev.mobase.deviceinfo.storage.StorageInfoProvider
import dev.mobase.deviceinfo.theme.DeviceThemeProvider

internal class AndroidDeviceInfoProvider(
    private val deviceThemeProvider: DeviceThemeProvider,
    private val memoryInfoProvider: MemoryInfoProvider,
    private val storageInfoProvider: StorageInfoProvider,
    private val displayInfoProvider: DisplayInfoProvider,
) : DeviceInfoProvider {
    override fun get(): DeviceInfo {
        return DeviceInfo(
            theme = deviceThemeProvider.get(),
            memoryInfo = memoryInfoProvider.get(),
            storageInfo = storageInfoProvider.get(),
            displayInfo = displayInfoProvider.get(),
        )
    }
}