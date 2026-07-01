package dev.mobase.deviceinfo.memory

internal fun interface MemoryInfoProvider {
    fun get(): MemoryInfo?
}