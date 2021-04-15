package org.dhis2.utils.analytics

import android.annotation.SuppressLint
import android.os.Bundle
import javax.inject.Inject
import org.dhis2.utils.analytics.matomo.MatomoAnalyticsController
import org.hisp.dhis.android.core.D2Manager

class AnalyticsHelper @Inject constructor(
    private val matomoAnalyticsController: MatomoAnalyticsController
) {

    @SuppressLint("CheckResult")
    fun setEvent(param: String, value: String, event: String) {
        val params = HashMap<String, String>().apply {
            put(param, value)
        }

        // trackMatomoEvent(param, value, event)
        setEvent(event, params)
    }

    fun trackMatomoEvent(category: String, action: String, label: String) {
        matomoAnalyticsController.trackEvent(category, action, label)
    }

    private fun trackUserId() {
        val d2 = D2Manager.getD2()

        if (d2 != null && d2.userModule().blockingIsLogged()) {
            val userUid = d2.userModule().user().blockingGet()?.uid()
            matomoAnalyticsController.setUserId(userUid)
        }
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
