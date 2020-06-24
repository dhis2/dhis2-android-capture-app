package org.dhis2.utils.analytics.matomo

interface MatomoAnalyticsController {
    fun trackScreenView(screen:String, title:String)
    fun trackEvent(category:String, action:String, label:String)
}