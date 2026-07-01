package dev.mobase.deviceinfo.storage

internal fun interface StorageInfoProvider {
    fun get(): StorageInfo?
}