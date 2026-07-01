package dev.mobase.core.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import dev.mobase.analytics.Analytics
import dev.mobase.appsflyer.AppsFlyer
import dev.mobase.attribution.conversion.ConversionDataResult

internal class AppsFlyerConversionEventObserver(
    private val appsFlyer: AppsFlyer,
    private val analytics: Analytics,
) : EventObserver {
    override fun launch(scope: CoroutineScope) {
        appsFlyer.conversionData
            .filterNotNull()
            .distinctUntilChanged()
            .onEach {
                val appsFlyerUid = appsFlyer.getAppsFlyerUID()

                when (it) {
                    is ConversionDataResult.Success -> {
                        analytics.track(
                            event = "af_conversion_data_success",
                            properties = it.data.orEmpty()
                                .plus("appsflyer_uid" to appsFlyerUid)
                        )
                    }

                    is ConversionDataResult.Failure -> {
                        analytics.track(
                            event = "af_conversion_data_fail",
                            properties = mapOf(
                                "error" to it.error,
                                "appsflyer_uid" to appsFlyerUid
                            )
                        )
                    }
                }
            }
            .launchIn(scope)
    }
}