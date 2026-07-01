package dev.mobase.deviceinfo.storage

import android.content.Context
import android.os.StatFs
import dev.mobase.deviceinfo.storage.StorageInfo
import timber.log.Timber

internal class AndroidStorageInfoProvider(
    private val context: Context,
) : StorageInfoProvider {
    override fun get(): StorageInfo? {
        return try {
            val stat = StatFs(context.filesDir.absolutePath)
            val blockSize = stat.blockSizeLong
            val available = stat.availableBlocksLong * blockSize
            val total = stat.blockCountLong * blockSize

            if (available >= 0 && total > 0) {
                StorageInfo(total, available)
            } else {
                Timber.w("Storage info invalid: available: $available, total: $total")
                null
            }
        } catch (e: Throwable) {
            Timber.e(e, "Failed to read storage info")
            null
        }
    }
}