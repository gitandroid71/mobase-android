package dev.mobase.deviceinfo

import dev.mobase.deviceinfo.model.DeviceInfo

fun interface DeviceInfoProvider {
    fun get(): DeviceInfo
}