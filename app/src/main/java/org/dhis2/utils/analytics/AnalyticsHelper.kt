package org.dhis2.utils.analytics

import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.mobile.commons.reporting.AnalyticActions
import org.dhis2.utils.analytics.matomo.DEFAULT_EXTERNAL_TRACKER_NAME
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
        ) {
            matomoAnalyticsController.updateDhisImplementationTracker(
                matomoUrl,
                matomoID,
                DEFAULT_EXTERNAL_TRACKER_NAME,
            )
        }

        override fun clearMatomoSecondaryTracker() {
            matomoAnalyticsController.clearDhisImplementation()
        }
    }
