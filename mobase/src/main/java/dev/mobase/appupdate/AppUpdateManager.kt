package dev.mobase.appupdate

interface AppUpdateManager {
    fun requestUpdate(minVersion: Long)
}