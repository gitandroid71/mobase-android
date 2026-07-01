package dev.mobase.deviceinfo.memory

import dev.mobase.deviceinfo.model.DevicePerformanceCategory

data class MemoryInfo(
    val totalRam: Long,
    val availableRam: Long,
    val category: DevicePerformanceCategory,
)