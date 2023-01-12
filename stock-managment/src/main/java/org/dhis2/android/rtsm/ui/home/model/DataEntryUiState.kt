package org.dhis2.android.rtsm.ui.home.model

data class DataEntryUiState(
    val step: DataEntryStep = DataEntryStep.EDITING,
    val button: ButtonUiState = ButtonUiState()
)

enum class DataEntryStep {
    LISTING,
    EDITING,
    REVIEWING,
    COMPLETED
}
