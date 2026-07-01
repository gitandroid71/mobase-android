package dev.mobase.attribution.parser

import dev.mobase.attribution.model.MediaSource

internal class DefaultMediaSourceParser : MediaSourceParser {
    private val patterns = listOf(
        Regex("organic") to MediaSource.Type.ORGANIC,
        Regex("google") to MediaSource.Type.GOOGLE,
        Regex("facebook|\\bfb\\b|instagram|\\big\\b") to MediaSource.Type.META,
        Regex("tiktok|tiktokglobal_int|bytedanceglobal_int") to MediaSource.Type.TIKTOK,
    )

    override fun parse(value: String?): MediaSource {
        if (value.isNullOrBlank()) {
            return MediaSource.ORGANIC
        }

        val normalized = value.replace(" ", "_").lowercase()

        val type = patterns
            .firstOrNull { (regex, _) -> regex.containsMatchIn(normalized) }
            ?.second
            ?: MediaSource.Type.UNKNOWN

        return MediaSource(value, type)
    }
}