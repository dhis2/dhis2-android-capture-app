package org.dhis2.utils

import android.content.res.Configuration
import android.content.res.Resources

fun isLandscape(): Boolean {
    val orientation = Resources.getSystem().configuration.orientation
    return orientation == Configuration.ORIENTATION_LANDSCAPE
}

fun isPortrait(): Boolean {
    val orientation = Resources.getSystem().configuration.orientation
    return orientation == Configuration.ORIENTATION_PORTRAIT
}