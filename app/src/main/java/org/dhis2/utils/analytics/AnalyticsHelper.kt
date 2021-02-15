package org.dhis2.utils.analytics

import android.annotation.SuppressLint
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.utils.Constants
import org.dhis2.utils.analytics.matomo.MatomoAnalyticsController
import org.hisp.dhis.android.core.D2Manager

class AnalyticsHelper @Inject constructor(
    val analytics: FirebaseAnalytics,
    private val preferencesProvider: PreferenceProvider,
    private val matomoAnalyticsController: MatomoAnalyticsController
) {

    @SuppressLint("CheckResult")
    fun setEvent(param: String, value: String, event: String) {
        val params = HashMap<String, String>().apply {
            put(param, value)
        }

        trackMatomoEvent(param, value, event)
        setEvent(event, params)
    }

    fun trackMatomoEvent(category: String, action: String, label: String) {
        val d2 = D2Manager.getD2()

        if (d2 != null && d2.userModule().blockingIsLogged()) {
            val userUid = d2.userModule().user().blockingGet()?.uid()
            matomoAnalyticsController.setUserId(userUid)
        }
        matomoAnalyticsController.trackEvent(category, action, label)
    }

    fun setEvent(event: String, params: Map<String, String>) {
        val bundle = Bundle()
        val d2 = D2Manager.getD2()

        if (d2 != null && d2.userModule().blockingIsLogged()) {
            if (preferencesProvider.contains(Constants.USER)) {
                analytics.setUserProperty(
                    USER_PROPERTY_NAME,
                    d2.userModule().userCredentials().blockingGet()?.username()
                )
            }
            analytics.setUserProperty(
                USER_PROPERTY_SERVER,
                d2.systemInfoModule().systemInfo().blockingGet()?.contextPath()
            )
            analytics.setUserId(d2.userModule().user().blockingGet()?.uid())
        }
        params.entries.forEach { bundle.putString(it.key, it.value) }
        logEvent(event, bundle)
    }

    private fun logEvent(event: String, bundle: Bundle) {
        analytics.logEvent(event, bundle)
    }
}
