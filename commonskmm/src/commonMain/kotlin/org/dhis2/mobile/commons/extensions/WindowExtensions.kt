package org.dhis2.mobile.commons.extensions

import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable

@Composable
expect fun getWindowSizeClass(): WindowSizeClass

@Composable
fun deviceIsInLandscapeMode(): Boolean {
    val windowSizeClass = getWindowSizeClass()
    return windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded ||
        (
            windowSizeClass.widthSizeClass == WindowWidthSizeClass.Medium &&
                windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact
        )
}
