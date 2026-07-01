package dev.mobase.appsflyer.deeplink

import android.net.Uri
import com.appsflyer.deeplink.DeepLink
import dev.mobase.attribution.parser.DefaultMediaSourceParser
import dev.mobase.attribution.parser.MediaSourceParser
import dev.mobase.attribution.model.AttributionData
import dev.mobase.deeplink.DeepLinkData

internal data class AppsFlyerDeeplink(
    override val uri: Uri,
    override val queryParams: Map<String, String>,
    override val attribution: AttributionData?,
    private val mediaSourceParser: MediaSourceParser = DefaultMediaSourceParser(),
    private val deepLink: DeepLink,
) : DeepLinkData {

    constructor(
        deepLink: DeepLink,
        mediaSourceParser: MediaSourceParser = DefaultMediaSourceParser(),
    ) : this(
        uri = deepLink.getStringValue("link")?.let(Uri::parse) ?: Uri.EMPTY,
        queryParams = emptyMap(),
        attribution = deepLink.toAttribution(mediaSourceParser),
        mediaSourceParser = mediaSourceParser,
        deepLink = deepLink,
    )

    override fun get(key: String): String? {
        return deepLink.getStringValue(key)
    }
}

private fun DeepLink.toAttribution(
    mediaSourceParser: MediaSourceParser,
): AttributionData {
    return AttributionData(
        mediaSource = mediaSourceParser.parse(mediaSource),
        campaign = getStringValue("campaign"),
        deepLinkValue = getStringValue("deep_link_value"),
    )
}