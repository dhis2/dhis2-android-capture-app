package org.dhis2.utils.analytics

import org.dhis2.commons.matomo.MatomoAnalyticsController
import javax.inject.Inject

class AnalyticsHelper @Inject constructor(
    private val matomoAnalyticsController: MatomoAnalyticsController,
) {

    fun setEvent(param: String, value: String, event: String) {
        // TODO: Track in matomo
    }

    fun trackMatomoEvent(category: String, action: String, label: String) {
        matomoAnalyticsController.trackEvent(category, action, label)
    }

    fun updateMatomoSecondaryTracker(matomoUrl: String, matomoID: Int, trackerName: String) {
        matomoAnalyticsController.updateDhisImplementationTracker(
            matomoUrl,
            matomoID,
            trackerName,
        )
    }

    fun clearMatomoSecondaryTracker() {
        matomoAnalyticsController.clearDhisImplementation()
    }
}
