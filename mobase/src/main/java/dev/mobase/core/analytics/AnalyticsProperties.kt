package dev.mobase.core.analytics

internal object AnalyticsProperties {
    /** User properties */
    const val USER_ID = "user_id"
    const val APP_SET_ID = "app_set_id"
    const val STORE_COUNTRY = "store_country"

    /** Cohort properties */
    const val COHORT_DAY = "cohort_day"
    const val COHORT_WEEK = "cohort_week"
    const val COHORT_MONTH = "cohort_month"
    const val COHORT_YEAR = "cohort_year"

    /** Advertising properties */
    const val ADVERTISING_ID = "advertising_id"
    const val IS_LIMIT_AD_TRACKING_ENABLED = "is_limit_ad_tracking_enabled"

    /** Attribution properties */
    const val NETWORK = "network"
    const val MEDIA_SOURCE_TYPE = "media_source_type"
    const val CAMPAIGN = "campaignName"
    const val AD_GROUP = "adGroupName"
    const val AD = "ad"
    const val DEEP_LINK_VALUE = "deep_link_value"
    const val ATTRIBUTION_SOURCE = "attribution_source"

    /** Purchases properties */
    const val ACTIVE_SUBS = "active_subscriptions"
    const val PURCHASED_PRODUCT_IDS = "all_purchased_product_ids"

    /** Device properties */
    const val ANDROID_FRAMEWORK_VERSION = "android_framework_version"
    const val APPSFLYER_SDK_VERSION = "appsflyer_sdk_version"
}