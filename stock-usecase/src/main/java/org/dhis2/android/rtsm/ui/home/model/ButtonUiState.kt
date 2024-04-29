package org.dhis2.android.rtsm.ui.home.model

import androidx.compose.ui.graphics.Color
import org.dhis2.android.rtsm.R

data class ButtonUiState(
    val text: Int = R.string.review,
    val icon: Int = R.drawable.proceed_icon,
    val color: Color = Color.White,
    val contentColor: Color = Color.White,
    val containerColor: Color = Color.Blue,
    val visible: Boolean = false,
)
