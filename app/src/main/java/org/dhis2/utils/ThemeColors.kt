package org.dhis2.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import org.dhis2.R
import org.dhis2.commons.resources.ColorUtils

@Composable
fun getThemePrimaryColor(): Color {
    val context = LocalContext.current
    val primaryColor = remember(context) { ColorUtils().getThemePrimaryColor(context) }
    return primaryColor
}

@Composable
fun getOnToolbarColor(): Color {
    val context = LocalContext.current
    val onToolbarColor = remember(context) {
        val ta = context.obtainStyledAttributes(intArrayOf(R.attr.onToolbarColor))
        val color = Color(ta.getColor(0, android.graphics.Color.WHITE))
        ta.recycle()
        color
    }
    return onToolbarColor
}