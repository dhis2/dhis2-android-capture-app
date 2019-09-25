package org.dhis2.utils.analytics

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.annotation.NonNull
import com.google.firebase.analytics.FirebaseAnalytics
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.user.UserCredentials
import rx.Completable
import java.util.concurrent.Callable
import javax.inject.Inject

class AnalyticsHelper @Inject constructor(context: Context) {

    var d2: D2? = null
    private var analytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    fun setCurrentScreen(@NonNull activity: Activity, screenName: String, classOverride: String? = null) {
        analytics.setCurrentScreen(activity, screenName, classOverride)
    }

    fun setEvent(param: String, value: String, event: String) {
        /*if(d2 != null)
            Completable.fromCallable { d2.userModule().userCredentials.get().
                map {
                    //setBundleEvent(it.user()?.uid(), it.)
                 }}
*/


    }

    private fun setBundleEvent(user: String, server: String,
                               param: String, value: String, event: String){
        val bundle = Bundle()
        bundle.putString("user", user)
        bundle.putString("server", server)
        bundle.putString(param, value)
        analytics.logEvent(event, bundle)
    }

}