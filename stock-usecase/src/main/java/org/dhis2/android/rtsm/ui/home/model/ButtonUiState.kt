package org.dhis2.android.rtsm.ui.home.model

import org.dhis2.android.rtsm.R

data class ButtonUiState(
    val text: Int = R.string.review,
    val icon: Int = R.drawable.proceed_icon,
    val visible: Boolean = false,
)
