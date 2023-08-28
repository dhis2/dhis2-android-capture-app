package org.dhis2.ui.model

data class ButtonUiModel(
    val text: String,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
)
