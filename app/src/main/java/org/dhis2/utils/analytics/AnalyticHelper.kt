package org.dhis2.utils.analytics

import android.content.Context

import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker

import java.util.HashMap

/**
 * QUADRAM. Created by ppajuelo on 21/01/2019.
 */
class AnalyticHelper {


    enum class TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER
        // Tracker used by all ecommerce transactions from a company.
    }

    fun sendScreen(): Map<String, String> {
        return HitBuilders.ScreenViewBuilder()
                .build()
    }

    fun sendEvent(category: String, action: String, label: String, value: Long): Map<String, String> {
        return HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .setValue(value)
                .build()
    }

    companion object {

        private var PROPERTY_ID: String? = null

        private val mTrackers = HashMap<TrackerName, Tracker>()

        fun init(propertyID: String): AnalyticHelper { //store in Application
            val helper = AnalyticHelper()
            PROPERTY_ID = propertyID
            return helper
        }

        @Synchronized
        fun getTracker(applicationContext: Context, trackerId: TrackerName): Tracker? {
            if (!mTrackers.containsKey(trackerId)) {

                val analytics = GoogleAnalytics.getInstance(applicationContext)
                val t = analytics.newTracker(PROPERTY_ID)
                mTrackers[trackerId] = t

            }
            return mTrackers[trackerId]
        }
    }

}
