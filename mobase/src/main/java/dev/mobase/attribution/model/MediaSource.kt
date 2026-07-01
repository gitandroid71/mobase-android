package dev.mobase.attribution.model

data class MediaSource(
    val value: String?,
    val type: Type
) {
    enum class Type(val value: String) {
        ORGANIC("Organic"),
        GOOGLE("Google"),
        META("Meta"),
        TIKTOK("TikTok"),
        UNKNOWN("Unknown")
    }

    companion object {
        val ORGANIC = MediaSource("Organic", Type.ORGANIC)
    }
}