package dev.mobase.deviceinfo.cpu

import dev.mobase.deviceinfo.model.DevicePerformanceCategory

internal interface CpuPerformanceClassifier {
    fun classify(
        cores: Int?,
        abi: String?,
        chipset: String?,
        board: String?,
    ): DevicePerformanceCategory
}