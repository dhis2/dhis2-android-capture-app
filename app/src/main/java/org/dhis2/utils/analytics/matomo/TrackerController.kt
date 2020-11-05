package org.dhis2.utils.analytics.matomo

import android.content.Context
import org.dhis2.BuildConfig
import org.matomo.sdk.Matomo
import org.matomo.sdk.Tracker
import org.matomo.sdk.TrackerBuilder

class TrackerController {
    companion object {
        fun generateTracker(context: Context): Tracker? {
            return when (BuildConfig.DEBUG) {
                true -> null
                false -> TrackerBuilder.createDefault(
                    BuildConfig.MATOMO_URL,
                    BuildConfig.MATOMO_ID
                ).build(Matomo.getInstance(context))
            }
        }
    }
}
