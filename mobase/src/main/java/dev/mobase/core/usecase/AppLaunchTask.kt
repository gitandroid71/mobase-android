package dev.mobase.core.usecase

import android.icu.util.Calendar
import dev.mobase.analytics.Analytics
import dev.mobase.core.analytics.AnalyticsEvents.FIRST_LAUNCH
import dev.mobase.core.analytics.AnalyticsProperties.COHORT_DAY
import dev.mobase.core.analytics.AnalyticsProperties.COHORT_MONTH
import dev.mobase.core.analytics.AnalyticsProperties.COHORT_WEEK
import dev.mobase.core.analytics.AnalyticsProperties.COHORT_YEAR
import dev.mobase.core.analytics.AnalyticsPropertiesMapper
import dev.mobase.core.analytics.DeviceInfoAnalyticsPropertiesMapper
import dev.mobase.core.storage.Storage
import dev.mobase.deviceinfo.DeviceInfoProvider
import dev.mobase.deviceinfo.model.DeviceInfo

internal class AppLaunchTask(
    private val storage: Storage,
    private val deviceInfoProvider: DeviceInfoProvider,
    private val analytics: Analytics,
    private val propertiesMapper: AnalyticsPropertiesMapper<DeviceInfo> = DeviceInfoAnalyticsPropertiesMapper
) {
    suspend fun execute(): Result {
        if (!storage.isFirstLaunch()) {
            return Result(isFirstLaunch = false)
        }

        val deviceInfo = deviceInfoProvider.get()
        val deviceProperties = propertiesMapper.map(deviceInfo)
        val cohortProperties = createCohortProperties()

        val allProperties = deviceProperties + cohortProperties
        analytics.setUserProperties(allProperties)
        analytics.track(FIRST_LAUNCH, allProperties)

        storage.confirmFirstLaunch()

        return Result(isFirstLaunch = true)
    }

    private fun createCohortProperties(): Map<String, Any> {
        val calendar = Calendar.getInstance()
        return mapOf(
            COHORT_DAY to calendar[Calendar.DAY_OF_YEAR],
            COHORT_WEEK to calendar[Calendar.WEEK_OF_YEAR],
            COHORT_MONTH to calendar[Calendar.MONTH] + 1,
            COHORT_YEAR to calendar[Calendar.YEAR]
        )
    }

    data class Result(
        val isFirstLaunch: Boolean
    )
}