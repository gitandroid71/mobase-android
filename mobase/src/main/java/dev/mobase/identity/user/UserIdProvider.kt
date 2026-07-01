package dev.mobase.identity.user

fun interface UserIdProvider {
    fun provide(): String?
}