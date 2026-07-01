package dev.mobase.identity.appset

fun interface AppSetIdProvider {
    suspend fun get(): String?
}