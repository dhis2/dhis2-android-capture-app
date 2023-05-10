package org.dhis2.Bindings

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import org.dhis2.BuildConfig

fun Context.buildInfo(): String {
    return if (BuildConfig.BUILD_TYPE == "release") {
        "v${BuildConfig.VERSION_NAME}"
    } else {
        "v${BuildConfig.VERSION_NAME} : ${BuildConfig.BUILD_DATE} : ${BuildConfig.GIT_SHA} "
    }
}

fun Fragment.checkSMSPermission(requestPermission: Boolean, request: Int): Boolean {
    return false
}

fun Context.hasPermissions(permissions: Array<String>): Boolean {
    for (permission in permissions) {
        if (ContextCompat.checkSelfPermission(this, permission)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
    }
    return true
}

fun Context.showSMS(): Boolean {
    return BuildConfig.FLAVOR == "dhis" || BuildConfig.FLAVOR == "dhisPlayServices"
}
