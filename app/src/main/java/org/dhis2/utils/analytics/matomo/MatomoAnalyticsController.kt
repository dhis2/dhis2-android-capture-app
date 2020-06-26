package org.dhis2.utils.analytics.matomo

interface MatomoAnalyticsController {
    fun trackScreenView(screen: String, title: String)
    fun trackScreenViewWithDimension(
        screen: String,
        title: String,
        index: Int,
        dimensionValue: String
    )

    fun trackScreenViewWithDimensionsAsSeparateEvents(
        screen: String,
        title: String,
        dimensions: Map<Int, String>
    )

    fun trackScreenViewWithTwoDimensionsAsSameEvent(
        screen: String,
        title: String,
        firstIndex: Int,
        firstValue: String,
        secondIndex: Int,
        secondValue: String
    )

    fun trackEvent(category: String, action: String, label: String)
    fun trackEventWithDimension(
        category: String,
        action: String,
        label: String,
        index: Int,
        dimensionValue: String
    )

    fun trackException(exception: Throwable, description: String)
    fun setUserId(identification: String?)
}
