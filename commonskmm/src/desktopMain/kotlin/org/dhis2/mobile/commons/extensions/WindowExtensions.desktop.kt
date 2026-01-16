package org.dhis2.mobile.commons.extensions

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable

@ExperimentalMaterial3WindowSizeClassApi
@Composable
actual fun getWindowSizeClass(): WindowSizeClass = calculateWindowSizeClass()
