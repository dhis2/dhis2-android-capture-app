package org.dhis2.utils

import android.app.Activity
import android.content.res.Configuration
import android.content.res.Resources
import android.util.DisplayMetrics

fun isLandscape(): Boolean {
    val orientation = Resources.getSystem().configuration.orientation
    return orientation == Configuration.ORIENTATION_LANDSCAPE
}

fun Activity.isLandscape(): Boolean {
    val displayMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics.heightPixels < displayMetrics.widthPixels
}

fun isPortrait(): Boolean {
    val orientation = Resources.getSystem().configuration.orientation
    return orientation == Configuration.ORIENTATION_PORTRAIT
}

fun Activity.isPortrait(): Boolean {
    val displayMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics.heightPixels >= displayMetrics.widthPixels
}
