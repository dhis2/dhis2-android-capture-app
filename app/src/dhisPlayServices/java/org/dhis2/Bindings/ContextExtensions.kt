package org.dhis2.Bindings

import android.content.Context
import androidx.fragment.app.Fragment
import org.dhis2.BuildConfig

fun Context.buildInfo(): String {
    val paperWork = Paperwork(this)
    return if (BuildConfig.BUILD_TYPE == "release") {
        "v${BuildConfig.VERSION_NAME}"
    } else {
        "v${BuildConfig.VERSION_NAME} : ${BuildConfig.BUILD_DATE} : ${BuildConfig.GIT_SHA} "
    }
}

fun Fragment.checkSMSPermission(requestPermission: Boolean, request: Int): Boolean {
    return false
}

fun Context.showSMS(): Boolean {
    return BuildConfig.FLAVOR == "dhis" || BuildConfig.FLAVOR == "dhisPlayServices"
}
