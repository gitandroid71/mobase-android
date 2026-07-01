package dev.mobase.core.storage

internal interface Storage {
    suspend fun isFirstLaunch(): Boolean

    suspend fun confirmFirstLaunch()
}