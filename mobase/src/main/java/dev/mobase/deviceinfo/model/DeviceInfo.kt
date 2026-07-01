package dev.mobase.deviceinfo.model

import dev.mobase.deviceinfo.display.DisplayInfo
import dev.mobase.deviceinfo.memory.MemoryInfo
import dev.mobase.deviceinfo.storage.StorageInfo
import dev.mobase.deviceinfo.theme.DeviceTheme

data class DeviceInfo(
    val theme: DeviceTheme,
    val displayInfo: DisplayInfo?,
    val memoryInfo: MemoryInfo?,
    val storageInfo: StorageInfo?,
)