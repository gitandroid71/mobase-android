package dev.mobase.attribution.android

import dev.mobase.attribution.model.AttributionData

internal interface InstallReferrerParser {
    fun parse(referrer: String?): AttributionData

    companion object {
        fun create(): InstallReferrerParser {
            return GooglePlayInstallReferrerParser()
        }
    }
}