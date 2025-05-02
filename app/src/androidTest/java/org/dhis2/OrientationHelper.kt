package org.dhis2

import android.content.Context
import android.content.res.Configuration
import androidx.test.platform.app.InstrumentationRegistry

object OrientationHelper {
    fun isLandscape(): Boolean {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val orientation = context.resources.configuration.orientation
        return orientation == Configuration.ORIENTATION_LANDSCAPE
    }
}