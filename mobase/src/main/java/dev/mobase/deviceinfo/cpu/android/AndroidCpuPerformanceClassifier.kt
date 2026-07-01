package dev.mobase.deviceinfo.cpu.android

import dev.mobase.deviceinfo.cpu.CpuPerformanceClassifier
import dev.mobase.deviceinfo.model.DevicePerformanceCategory

internal class AndroidCpuPerformanceClassifier : CpuPerformanceClassifier {
    private companion object {
        val FLAGSHIP_CHIPSET_INDICATORS = setOf(
            "snapdragon 8",
            "snapdragon 888",
            "snapdragon 865",
            "snapdragon 855",
            "exynos 2100",
            "exynos 2200",
            "exynos 990",
            "exynos 9820",
            "kirin 9000",
            "kirin 990",
            "dimensity 9000",
            "dimensity 1200",
            "tensor",
            "google tensor"
        )

        val HIGH_PERFORMANCE_ABI_MARKERS = setOf(
            "arm64-v8a",
            "armeabi-v7a"
        )
    }

    override fun classify(
        cores: Int?,
        abi: String?,
        chipset: String?,
        board: String?
    ): DevicePerformanceCategory {
        if (cores == null || cores <= 0) {
            return DevicePerformanceCategory.UNKNOWN
        }

        val abi = abi.orEmpty()
        val hasHighPerformanceAbi = HIGH_PERFORMANCE_ABI_MARKERS.any(abi::contains)

        val fingerprint = listOfNotNull(chipset, board)
            .joinToString(" ")
            .lowercase()

        val isFlagshipChipset = FLAGSHIP_CHIPSET_INDICATORS.any(fingerprint::contains)

        return when {
            cores < 4 -> DevicePerformanceCategory.VERY_LOW
            cores == 4 && !hasHighPerformanceAbi -> DevicePerformanceCategory.LOW
            cores in 4..6 && hasHighPerformanceAbi -> DevicePerformanceCategory.MEDIUM
            cores >= 8 && hasHighPerformanceAbi && !isFlagshipChipset -> DevicePerformanceCategory.HIGH
            cores >= 8 && isFlagshipChipset -> DevicePerformanceCategory.VERY_HIGH
            else -> DevicePerformanceCategory.MEDIUM
        }
    }
}