package dev.mobase.deviceinfo.memory

import dev.mobase.deviceinfo.model.DevicePerformanceCategory

internal interface RamPerformanceClassifier {
    fun classify(totalRamBytes: Long): DevicePerformanceCategory
}