package dev.mobase.deeplink

fun interface DeepLinkListener {
    fun onDeepLink(deepLinkData: DeepLinkData?)
}