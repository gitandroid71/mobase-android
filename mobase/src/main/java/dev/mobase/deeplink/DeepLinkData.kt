package dev.mobase.deeplink

import android.net.Uri
import dev.mobase.attribution.model.AttributionData

interface DeepLinkData {
    val uri: Uri

    val queryParams: Map<String, String>

    val attribution: AttributionData?

    operator fun get(key: String): String?
}