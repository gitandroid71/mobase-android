package dev.mobase.deviceinfo.cpu

import dev.mobase.deviceinfo.model.DevicePerformanceCategory

data class CpuInfo(
    val cores: Int?,
    val primaryAbi: String?,
    val abis: List<String>?,
    val chipset: String?,
    val board: String?,
    val performanceCategory: DevicePerformanceCategory,
)