package dev.mobase.deviceinfo.memory.android

import dev.mobase.deviceinfo.model.DevicePerformanceCategory
import dev.mobase.deviceinfo.memory.RamPerformanceClassifier

internal class AndroidRamPerformanceClassifier : RamPerformanceClassifier {
    override fun classify(totalRamBytes: Long): DevicePerformanceCategory {
        val gb = totalRamBytes / (1024.0 * 1024.0 * 1024.0)

        return when {
            gb < 2.0 -> DevicePerformanceCategory.VERY_LOW
            gb < 3.0 -> DevicePerformanceCategory.LOW
            gb < 6.0 -> DevicePerformanceCategory.MEDIUM
            gb < 12.0 -> DevicePerformanceCategory.HIGH
            else -> DevicePerformanceCategory.VERY_HIGH
        }
    }
}