package org.dhis2.utils.analytics

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.annotation.NonNull
import com.google.firebase.analytics.FirebaseAnalytics
import io.reactivex.Flowable
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.user.UserCredentials
import rx.Completable
import java.util.concurrent.Callable
import javax.inject.Inject

class AnalyticsHelper @Inject constructor(context: Context) {

    private lateinit var _d2: D2
    private val d2: D2
        get() = _d2

    fun setD2(d2: D2){
        _d2 = d2
    }

    private var analytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    fun setCurrentScreen(@NonNull activity: Activity, screenName: String, classOverride: String? = null) {
        analytics.setCurrentScreen(activity, screenName, classOverride)
    }

    @SuppressLint("CheckResult")
    fun setEvent(param: String, value: String, event: String) {
        if(d2 != null) {
            val user = d2.userModule().userCredentials.blockingGet()
            val info = d2.systemInfoModule().systemInfo.blockingGet()
            setBundleEvent(param, value, event, user.username(), info.contextPath())
            /*Flowable.just(d2.userModule().userCredentials.get()
                    .map { user -> d2.systemInfoModule().systemInfo.get()
                            .map { info ->
                                setBundleEvent(param, value, event, user.username(), info.contextPath())
                            }
                    }
            )*/
        }else setBundleEvent(param, value, event)
    }

    /**
     * Mask to use from Java
     * */
    private fun setBundleEvent(param: String, value: String, event: String){
        setBundleEvent(param, value, event, "", "")
    }

    private fun setBundleEvent(param: String, value: String, event: String,
                               user: String? = "", server: String? = ""){
        val bundle = Bundle()
        bundle.apply {
            if(!user.isNullOrEmpty() && !server.isNullOrEmpty()) {
                putString("user", user)
                putString("server", server)
            }
            putString(param, value)
        }

        analytics.logEvent(event, bundle)
    }

}