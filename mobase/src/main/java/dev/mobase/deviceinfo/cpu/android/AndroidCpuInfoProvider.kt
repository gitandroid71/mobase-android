package dev.mobase.deviceinfo.cpu.android

import android.os.Build
import dev.mobase.deviceinfo.cpu.CpuInfo
import dev.mobase.deviceinfo.cpu.CpuInfoProvider
import dev.mobase.deviceinfo.cpu.CpuPerformanceClassifier
import timber.log.Timber

internal class AndroidCpuInfoProvider(
    private val performanceClassifier: CpuPerformanceClassifier = AndroidCpuPerformanceClassifier()
) : CpuInfoProvider {
    override fun getCpuInfo(): CpuInfo {
        val cores = getAvailableProcessors()
        val supportedAbis = getSupportedAbis()
        val primaryAbi = supportedAbis.firstOrNull()

        val chipset = Build.HARDWARE
        val board = Build.BOARD

        return CpuInfo(
            cores = getAvailableProcessors(),
            primaryAbi = supportedAbis.firstOrNull(),
            abis = supportedAbis,
            chipset = chipset,
            board = board,
            performanceCategory = performanceClassifier.classify(cores, primaryAbi, chipset, board)
        )
    }

    private fun getAvailableProcessors(): Int? {
        return runCatching { Runtime.getRuntime().availableProcessors() }
            .getOrElse { e ->
                Timber.e(e, "Failed to retrieve number of available processors")
                null
            }
            ?.takeIf { it > 0 }
            ?: run {
                Timber.e("Failed to retrieve number of available processors")
                null
            }
    }

    private fun getSupportedAbis(): List<String> {
        return runCatching { Build.SUPPORTED_ABIS.toList() }
            .getOrElse { e ->
                Timber.e(e, "Failed to retrieve supported ABIs")
                emptyList()
            }
    }
}