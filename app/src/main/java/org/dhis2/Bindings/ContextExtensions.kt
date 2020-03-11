package org.dhis2.Bindings

import android.content.Context
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
