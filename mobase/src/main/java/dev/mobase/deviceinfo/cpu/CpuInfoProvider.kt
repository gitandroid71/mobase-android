package dev.mobase.deviceinfo.cpu

interface CpuInfoProvider {
    fun getCpuInfo(): CpuInfo
}