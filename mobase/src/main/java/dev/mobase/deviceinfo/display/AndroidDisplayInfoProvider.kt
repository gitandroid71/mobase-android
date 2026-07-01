package dev.mobase.deviceinfo.display

import android.content.Context
import timber.log.Timber

internal class AndroidDisplayInfoProvider(
    private val context: Context,
) : DisplayInfoProvider {
    override fun get(): DisplayInfo? {
        val metrics = runCatching { context.resources.displayMetrics }
            .getOrElse { e ->
                Timber.e(e, "Error while reading display parameters")
                return null
            }

        if (metrics.widthPixels <= 0 || metrics.heightPixels <= 0 || metrics.density <= 0f) {
            Timber.w("Incorrect display metrics received: %s", metrics)
            return null
        }

        return DisplayInfo(
            widthPx = metrics.widthPixels,
            heightPx = metrics.heightPixels,
            density = metrics.density,
            densityDpi = metrics.densityDpi,
        )
    }
}