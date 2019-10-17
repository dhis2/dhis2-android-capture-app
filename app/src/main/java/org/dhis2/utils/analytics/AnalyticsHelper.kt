package org.dhis2.utils.analytics

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.annotation.NonNull
import com.google.firebase.analytics.FirebaseAnalytics
import org.hisp.dhis.android.core.D2
import javax.inject.Inject

class AnalyticsHelper @Inject constructor(context: Context) {

    private var _d2: D2? = null
    private val d2: D2?
        get() = _d2

    fun setD2(d2: D2) {
        _d2 = d2
    }

    private var analytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    fun setCurrentScreen(@NonNull activity: Activity, screenName: String, classOverride: String? = null) {
        analytics.setCurrentScreen(activity, screenName, classOverride)
    }

    @SuppressLint("CheckResult")
    fun setEvent(param: String, value: String, event: String) {
        if (_d2 != null && d2!!.userModule().isLogged.blockingGet()) {
            val user = d2!!.userModule().userCredentials().blockingGet()
            val info = d2!!.systemInfoModule().systemInfo().blockingGet()
            setBundleEvent(param, value, event, user.username(), info.contextPath())
        } else setBundleEvent(param, value, event)
    }

    fun setEvent(event: String, params: Map<String, String>) {
        val bundle = Bundle()
        if (_d2 != null) {
           bundle.apply {
                putString("user", d2!!.userModule().userCredentials().blockingGet().username())
                putString("server", d2!!.systemInfoModule().systemInfo().blockingGet().contextPath())
            }
        }
        params.entries.forEach { bundle.putString(it.key, it.value) }
        logEvent(event, bundle)
    }

    /**
     * Mask to use from Java
     * */
    private fun setBundleEvent(param: String, value: String, event: String) {
        setBundleEvent(param, value, event, "", "")
    }

    private fun setBundleEvent(param: String, value: String, event: String,
                               user: String? = "", server: String? = "") {
        val bundle = Bundle()
        bundle.apply {
            if (!user.isNullOrEmpty() && !server.isNullOrEmpty()) {
                putString("user", user)
                putString("server", server)
            }
            putString(param, value)
        }

        logEvent(event, bundle)
    }

    private fun logEvent(event: String, bundle: Bundle) {
        analytics.logEvent(event, bundle)
    }

}