package dev.mobase.identity.user

internal interface UserIdentityManager {
    fun getUserId(): String?
}