package dev.mobase.deviceinfo.memory.android

import android.app.ActivityManager
import android.content.Context
import androidx.core.content.getSystemService
import dev.mobase.deviceinfo.memory.RamPerformanceClassifier
import dev.mobase.deviceinfo.memory.MemoryInfo
import dev.mobase.deviceinfo.memory.MemoryInfoProvider
import timber.log.Timber

internal class AndroidMemoryInfoProvider(
    private val context: Context,
    private val classifier: RamPerformanceClassifier = AndroidRamPerformanceClassifier()
) : MemoryInfoProvider {
    override fun get(): MemoryInfo? {
        return try {
            val activityManager = context.getSystemService<ActivityManager>() ?: return null
            val info = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(info)

            MemoryInfo(
                totalRam = info.totalMem,
                availableRam = info.availMem,
                category = classifier.classify(info.totalMem)
            )
        } catch (e: Throwable) {
            Timber.e(e, "Failed to read memory info")
            null
        }
    }
}