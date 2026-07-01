package dev.mobase.firebase

interface AppInstanceIdProvider {
    suspend fun get(): Result<String>
}