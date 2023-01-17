package org.dhis2.ui.buttons

import androidx.compose.ui.graphics.Color

data class FAButtonUiModel(
    val text: Int,
    val icon: Int,
    val contentColor: Color,
    val containerColor: Color,
    val expanded: Boolean = true,
    val enabled: Boolean = true
)
