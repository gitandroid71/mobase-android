package dev.mobase.appsflyer

import dev.mobase.identity.user.UserIdProvider

internal class AppsFlyerUserIdProvider(
    private val appsFlyer: AppsFlyer
) : UserIdProvider {
    override fun provide(): String? {
        return appsFlyer.getAppsFlyerUID()
    }
}