package dev.mobase.appsflyer

import android.content.Context
import com.appsflyer.AppsFlyerConsent
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import dev.mobase.analytics.Analytics
import dev.mobase.analytics.AnalyticsEvent
import dev.mobase.attribution.conversion.ConversionDataResult
import dev.mobase.attribution.conversion.ConversionDataSource
import dev.mobase.common.Initializer
import dev.mobase.consent.ConsentState
import timber.log.Timber

class AppsFlyer(
    private val apiKey: String,
    private val applicationContext: Context,
) : Initializer, Analytics, ConversionDataSource {
    private val appsflyer = AppsFlyerLib.getInstance()

    private val mutableConversionData = MutableStateFlow<ConversionDataResult?>(null)
    override val conversionData = mutableConversionData.asStateFlow()

    override fun initialize() {
        Timber.d("AppsFlyer initialize")

        appsflyer.init(
            apiKey,
            object : AppsFlyerConversionListener {
                override fun onConversionDataSuccess(data: Map<String, Any?>?) {
                    Timber.d("AppsFlyer conversion data: $data")
                    mutableConversionData.value = ConversionDataResult.Success(data)
                }

                override fun onConversionDataFail(error: String?) {
                    Timber.e("AppsFlyer conversion data failed: $error")
                    mutableConversionData.value = ConversionDataResult.Failure(error)
                }

                override fun onAppOpenAttribution(data: Map<String, String?>?) {
                    Timber.d("AppsFlyer app open attribution data: $data")
                }

                override fun onAttributionFailure(error: String?) {
                    Timber.e("AppsFlyer attribution failed: $error")
                }
            },
            applicationContext
        )
    }

    fun start(context: Context) {
        Timber.d("AppsFlyer start")

        appsflyer.start(context, apiKey, object : AppsFlyerRequestListener {
            override fun onSuccess() {
                Timber.d("AppsFlyer request success")
            }

            override fun onError(code: Int, error: String) {
                Timber.e("AppsFlyer request error: $code $error")
            }
        })
    }

    fun getAppsFlyerUID(): String? {
        return appsflyer.getAppsFlyerUID(applicationContext)
    }

    override fun setUserId(userId: String?) {
        appsflyer.setCustomerUserId(userId)
    }

    override fun setConsent(consent: ConsentState) {
        appsflyer.setConsentData(
            AppsFlyerConsent(
                hasConsentForAdStorage = consent.analyticsStorage,
                hasConsentForDataUsage = consent.adUserData,
                hasConsentForAdsPersonalization = consent.adPersonalization,
                isUserSubjectToGDPR = null,
            )
        )
    }

    override fun track(event: AnalyticsEvent) {
        track(event.type, event.properties)
    }

    override fun track(event: String, properties: Map<String, Any?>?) {
        Timber.d("AppsFlyer track: $event $properties")
        appsflyer.logEvent(applicationContext, event, properties)
    }

    override fun setUserProperties(properties: Map<String, Any?>) {
        // No-op
    }

    internal fun setAdditionalData(data: Map<String, Any?>) {
        appsflyer.setAdditionalData(data)
    }
}