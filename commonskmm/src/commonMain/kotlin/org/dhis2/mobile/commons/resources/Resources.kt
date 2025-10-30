package org.dhis2.mobile.commons.resources

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter

@Composable
expect fun getDrawableResource(name: String): Painter?
