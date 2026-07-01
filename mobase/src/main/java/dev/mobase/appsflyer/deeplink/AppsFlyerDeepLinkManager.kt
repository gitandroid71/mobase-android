package dev.mobase.appsflyer.deeplink

import com.appsflyer.AppsFlyerLib
import com.appsflyer.deeplink.DeepLinkResult
import dev.mobase.deeplink.DeepLinkListener
import dev.mobase.deeplink.DeepLinkManager
import timber.log.Timber

class AppsFlyerDeepLinkManager : DeepLinkManager {
    override fun setDeepLinkListener(listener: DeepLinkListener) {
        AppsFlyerLib.getInstance().subscribeForDeepLink {
            when (it.status) {
                DeepLinkResult.Status.FOUND -> {
                    Timber.d("Deep link found: ${it.deepLink}")
                    listener.onDeepLink(AppsFlyerDeeplink(it.deepLink))
                }

                DeepLinkResult.Status.NOT_FOUND -> {
                    Timber.d("Deep link not found")
                    listener.onDeepLink(null)
                }

                DeepLinkResult.Status.ERROR -> {
                    Timber.w("Deep link error: ${it.error}")

                    DeepLinkResult.Error.DEVELOPER_ERROR

                    null
                }
            }
        }
    }
}
