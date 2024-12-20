package org.dhis2.mobile.aggregates.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
actual fun getScreenWidth(): Dp = LocalConfiguration.current.screenWidthDp.dp
