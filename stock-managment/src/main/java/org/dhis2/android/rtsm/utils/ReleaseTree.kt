package org.dhis2.android.rtsm.utils

import android.util.Log
import timber.log.Timber

class ReleaseTree: Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // TODO: Integrate a crash reporter if necessary
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return
        }
    }

    override fun isLoggable(tag: String?, priority: Int): Boolean {
        // Only log ERRORs, WARNings etc
        return priority != Log.VERBOSE && priority != Log.DEBUG &&
                priority != Log.INFO
    }
}