package org.dhis2.mobile.commons.extensions

import androidx.activity.ComponentActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@ExperimentalMaterial3WindowSizeClassApi
@Composable
actual fun getWindowSizeClass(): WindowSizeClass {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    return calculateWindowSizeClass(activity)
}
