package org.dhis2.utils.timber

import android.os.Build
import android.util.Log
import timber.log.Timber

class DebugTree: Timber.DebugTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        var prior = priority;
        if (Build.MANUFACTURER == "HUAWEI" || Build.MANUFACTURER == "samsung") {
            if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
                prior = Log.ERROR
            }
        }
        super.log(prior, tag, message, t)
    }

    override fun createStackElementTag(element: StackTraceElement): String? {
        return super.createStackElementTag(element) + " - " + element.lineNumber
    }
}