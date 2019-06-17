package org.dhis2.utils.timber

import android.util.Log
import com.crashlytics.android.Crashlytics
import timber.log.Timber

class ReleaseTree: Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (isLoggable(tag, priority))
            Crashlytics.log(priority, tag, message)
    }

    override fun isLoggable(tag: String?, priority: Int): Boolean {
        return priority != Log.VERBOSE && priority != Log.DEBUG && priority != Log.INFO
    }
}