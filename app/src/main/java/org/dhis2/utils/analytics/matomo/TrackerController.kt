package org.dhis2.utils.analytics.matomo

import org.dhis2.BuildConfig
import org.matomo.sdk.Matomo
import org.matomo.sdk.Tracker
import org.matomo.sdk.TrackerBuilder

class TrackerController {
    companion object {
        fun generateTracker(matomo: Matomo): Tracker? {
            return when (BuildConfig.DEBUG) {
                true -> null
                false -> TrackerBuilder.createDefault(
                    BuildConfig.MATOMO_URL,
                    BuildConfig.MATOMO_ID
                ).build(matomo)
            }
        }

        fun generateSecondaryTracker(
            matomo: Matomo,
            matomoUrl: String,
            siteId: Int,
            trackerName: String
        ): Tracker? {
            return when (BuildConfig.DEBUG) {
                true -> null
                false ->
                    TrackerBuilder(matomoUrl, siteId, trackerName)
                        .build(matomo)
            }
        }
    }
}
