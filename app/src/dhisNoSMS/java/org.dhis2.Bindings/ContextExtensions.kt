package org.dhis2.Bindings

import android.content.Context
import androidx.fragment.app.Fragment
import hu.supercluster.paperwork.Paperwork
import org.dhis2.BuildConfig

fun Context.buildInfo(): String {
    val paperWork = Paperwork(this)
    return if (BuildConfig.BUILD_TYPE == "release") {
        "v${BuildConfig.VERSION_NAME}"
    } else {
        "v${BuildConfig.VERSION_NAME} : ${paperWork.get("buildTime")} : ${paperWork.get("gitSha")} "
    }
}

fun Fragment.checkSMSPermission(requestPermission: Boolean, request: Int): Boolean {
    return false
}

fun Context.showSMS(): Boolean {
    return BuildConfig.FLAVOR == "dhis"
}