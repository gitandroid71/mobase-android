package dev.mobase.attribution.model

data class AttributionData(
    val mediaSource: MediaSource,
    val campaign: String? = null,
    val adSet: String? = null,
    val adGroup: String? = null,
    val referrerUrl: String? = null,
    val deepLinkValue: String? = null,
    val rawData: Map<String, Any?>? = null,
    val attributionSource: String? = null
) {
    companion object {
        val ORGANIC = AttributionData(MediaSource.ORGANIC)
    }
}