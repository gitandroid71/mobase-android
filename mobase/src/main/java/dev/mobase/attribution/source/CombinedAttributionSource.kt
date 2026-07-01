package dev.mobase.attribution.source

import dev.mobase.attribution.model.MediaSource
import dev.mobase.attribution.model.AttributionData

internal class CombinedAttributionSource(
    private val providers: List<AttributionSource>
) : AttributionSource {
    override suspend fun getAttribution(): Result<AttributionData> {
        var firstError: Throwable? = null
        var firstOrganic: AttributionData? = null

        for (provider in providers) {
            provider.getAttribution()
                .onSuccess { data ->
                    if (data.mediaSource.type != MediaSource.Type.ORGANIC) {
                        return Result.success(data)
                    }

                    if (firstOrganic == null) {
                        firstOrganic = data
                    }
                }
                .onFailure { error ->
                    if (firstError == null) {
                        firstError = error
                    }
                }
        }

        return when {
            firstOrganic != null -> Result.success(firstOrganic)
            firstError != null -> Result.failure(firstError)
            else -> Result.success(AttributionData.ORGANIC)
        }
    }
}
