package dev.mobase.attribution.android

import androidx.core.net.toUri
import dev.mobase.attribution.parser.MediaSourceParser
import dev.mobase.attribution.model.AttributionData

internal class GooglePlayInstallReferrerParser(
    private val mediaSourceParser: MediaSourceParser = MediaSourceParser.create()
) : InstallReferrerParser {
    override fun parse(referrer: String?): AttributionData {
        if (referrer.isNullOrBlank()) {
            return AttributionData.ORGANIC
                .copy(attributionSource = ATTRIBUTION_SOURCE)
        }

        val uri = "?$referrer".toUri()
        val params = uri.queryParameterNames.associateWith { uri.getQueryParameter(it) }

        val mediaSource = detectMediaSource(referrer, params)

        return AttributionData(
            mediaSource = mediaSourceParser.parse(mediaSource),
            campaign = params["c"],
            adSet = params["af_adset"],
            adGroup = params["af_adgroup"],
            deepLinkValue = params["deep_link_value"],
            referrerUrl = referrer,
            attributionSource = ATTRIBUTION_SOURCE
        )
    }

    private fun detectMediaSource(raw: String, params: Map<String, String?>): String? {
        return params.googleAdsSource()
            ?: params.utmSource()
            ?: params["pid"]
            ?: raw.tiktokSource()
    }

    private fun Map<String, String?>.googleAdsSource(): String? {
        return if (listOf("gclid", "gbraid", "gad_source").all { !get(it).isNullOrBlank() }) {
            "google"
        } else {
            null
        }
    }

    private fun Map<String, String?>.utmSource(): String? {
        if (get("utm_medium")?.equals("organic", ignoreCase = true) == true) {
            return null
        }

        return get("utm_source")
    }

    private fun String.tiktokSource(): String? {
        return when {
            startsWith("tiktokglobal", ignoreCase = true) -> "tiktok_global"
            startsWith("tiktok", ignoreCase = true) -> "tiktok"
            else -> null
        }
    }

    private companion object {
        const val ATTRIBUTION_SOURCE = "gp_install_referrer"
    }
}