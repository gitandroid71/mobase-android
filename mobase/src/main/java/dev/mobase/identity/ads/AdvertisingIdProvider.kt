package dev.mobase.identity.ads

fun interface AdvertisingIdProvider {
    suspend fun get(): AdvertisingId
}