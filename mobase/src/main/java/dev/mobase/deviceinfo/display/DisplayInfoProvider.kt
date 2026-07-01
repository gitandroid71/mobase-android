package dev.mobase.deviceinfo.display

internal fun interface DisplayInfoProvider {
    fun get(): DisplayInfo?
}