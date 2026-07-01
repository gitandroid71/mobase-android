package dev.mobase.core

import dev.mobase.analytics.Analytics

internal class DefaultIdentityManager(
    private val analytics: List<Analytics>
) : IdentityManager {
    override fun setUserId(userId: String?) {
        analytics.forEach { it.setUserId(userId) }
    }
}