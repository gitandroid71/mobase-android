package dev.mobase.core.analytics

import dev.mobase.deviceinfo.model.DeviceInfo
import java.math.RoundingMode
import kotlin.math.roundToInt

internal object DeviceInfoAnalyticsPropertiesMapper : AnalyticsPropertiesMapper<DeviceInfo> {
    private const val BYTES_IN_GIB = 1024.0 * 1024.0 * 1024.0
    private const val WIDTH_DP = "logical_width_dp"
    private const val HEIGHT_DP = "logical_height_dp"
    private const val DENSITY_DPI = "display_density_dpi"
    private const val TOTAL_RAM = "total_ram_gb"
    private const val AVAILABLE_STORAGE = "available_storage_gb"
    private const val TOTAL_STORAGE = "total_storage_gb"

    override fun map(value: DeviceInfo): Map<String, Any> = buildMap {
        value.displayInfo
            ?.takeIf { it.density > 0f }
            ?.let { displayInfo ->
                put(WIDTH_DP, (displayInfo.widthPx / displayInfo.density).roundToInt())
                put(HEIGHT_DP, (displayInfo.heightPx / displayInfo.density).roundToInt())
                put(DENSITY_DPI, displayInfo.densityDpi)
            }

        putIfPositive(TOTAL_RAM, value.memoryInfo?.totalRam?.bytesToGib())
        putIfPositive(AVAILABLE_STORAGE, value.storageInfo?.availableSpace?.bytesToGib())
        putIfPositive(TOTAL_STORAGE, value.storageInfo?.totalSpace?.bytesToGib())
    }

    private fun Long.bytesToGib(): Double {
        return (this / BYTES_IN_GIB)
            .toBigDecimal()
            .setScale(2, RoundingMode.HALF_UP)
            .toDouble()
    }

    private fun MutableMap<String, Any>.putIfPositive(key: String, value: Double?) {
        if (value != null && value > 0) {
            put(key, value)
        }
    }
}