package dev.mobase.appsflyer

import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import dev.mobase.attribution.source.AttributionSource
import dev.mobase.attribution.conversion.ConversionDataResult
import dev.mobase.attribution.conversion.ConversionDataSource
import dev.mobase.attribution.parser.MediaSourceParser
import dev.mobase.attribution.model.AttributionData
import timber.log.Timber

internal class AppsFlyerAttributionSource(
    private val conversionDataSource: ConversionDataSource,
    private val mediaSourceParser: MediaSourceParser = MediaSourceParser.create()
) : AttributionSource {
    override suspend fun getAttribution(): Result<AttributionData> {
        return runCatching {
            val conversionData = conversionDataSource.conversionData
                .filterIsInstance<ConversionDataResult.Success>()
                .firstOrNull()
                ?.data

            if (conversionData == null || conversionData.isOrganic()) {
                AttributionData.ORGANIC
            } else {
                conversionData.toAttribution()
            }
        }
            .onSuccess { attributionData -> Timber.d("Got attribution: $attributionData") }
            .onFailure { e -> Timber.e(e, "Failed to get attribution") }
    }

    private fun Map<String, Any?>?.isOrganic(): Boolean {
        if (this.isNullOrEmpty()) {
            return true
        }

        return this["af_status"]
            ?.toString()
            ?.equals("Non-organic", ignoreCase = true) == true
    }

    private fun Map<String, Any?>.toAttribution(): AttributionData {
        val mediaSource = this["media_source"]?.toString()
        val deepLinkValue = this["deep_link_value"]?.toString()
            ?: this["af_dp"]?.toString()

        return AttributionData(
            campaign = this["campaign"]?.toString(),
            mediaSource = mediaSourceParser.parse(mediaSource),
            adSet = this["adset"]?.toString(),
            adGroup = this["adgroup"]?.toString(),
            deepLinkValue = deepLinkValue,
            rawData = this,
            attributionSource = "appsflyer"
        )
    }
}