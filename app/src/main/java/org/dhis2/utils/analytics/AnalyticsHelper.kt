package org.dhis2.utils.analytics

import android.annotation.SuppressLint
import android.os.Bundle
import javax.inject.Inject
import org.dhis2.utils.analytics.matomo.MatomoAnalyticsController

class AnalyticsHelper @Inject constructor(
    private val matomoAnalyticsController: MatomoAnalyticsController
) {

    @SuppressLint("CheckResult")
    fun setEvent(param: String, value: String, event: String) {
        val params = HashMap<String, String>().apply {
            put(param, value)
        }

        setEvent(event, params)
    }

    fun trackMatomoEvent(category: String, action: String, label: String) {
        matomoAnalyticsController.trackEvent(category, action, label)
    }

    fun setEvent(event: String, params: Map<String, String>) {
        val bundle = Bundle()
        logEvent(event, bundle)
    }

    fun updateMatomoSecondaryTracker(matomoUrl: String, matomoID: Int, trackerName: String) {
        matomoAnalyticsController.updateDhisImplementationTracker(
            matomoUrl,
            matomoID,
            trackerName
        )
    }

    private fun logEvent(event: String, bundle: Bundle) {
    }

    fun clearMatomoSecondaryTracker() {
        matomoAnalyticsController.clearDhisImplementation()
    }
}
