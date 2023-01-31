package org.dhis2.android.rtsm.ui.home.model

import org.dhis2.android.rtsm.R

data class SnackBarUiState(
    val message: Int = R.string.transaction_completed,
    val color: Int = R.color.error,
    val icon: Int = R.drawable.ic_warning
)
