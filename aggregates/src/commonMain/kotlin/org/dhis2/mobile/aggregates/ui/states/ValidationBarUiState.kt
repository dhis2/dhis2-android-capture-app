package org.dhis2.mobile.aggregates.ui.states

internal data class ValidationBarUiState(
    val quantity: Int,
    val description: String,
    val onExpandErrors: () -> Unit,
)
