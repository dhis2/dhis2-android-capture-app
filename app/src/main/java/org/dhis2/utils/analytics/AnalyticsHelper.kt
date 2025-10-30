package org.dhis2.utils.analytics

import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.mobile.commons.reporting.AnalyticActions
import javax.inject.Inject

class AnalyticsHelper
    @Inject
    constructor(
        private val matomoAnalyticsController: MatomoAnalyticsController,
    ) : AnalyticActions {
        override fun setEvent(
            param: String,
            value: String,
            event: String,
        ) {
            // TODO: Track in matomo
        }

        override fun trackMatomoEvent(
            category: String,
            action: String,
            label: String,
        ) {
            matomoAnalyticsController.trackEvent(category, action, label)
        }

        override fun updateMatomoSecondaryTracker(
            matomoUrl: String,
            matomoID: Int,
            trackerName: String,
        ) {
            matomoAnalyticsController.updateDhisImplementationTracker(
                matomoUrl,
                matomoID,
                trackerName,
            )
        }

        override fun clearMatomoSecondaryTracker() {
            matomoAnalyticsController.clearDhisImplementation()
        }
    }
