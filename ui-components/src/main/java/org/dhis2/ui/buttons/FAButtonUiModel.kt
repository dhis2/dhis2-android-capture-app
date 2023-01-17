package org.dhis2.ui.buttons

import androidx.compose.ui.graphics.Color

data class FAButtonUiModel(
    val text: Int,
    val textColor: Color,
    val icon: Int,
    val iconTint: Color,
    val extended: Boolean = true,
    val enabled: Boolean = true
)
