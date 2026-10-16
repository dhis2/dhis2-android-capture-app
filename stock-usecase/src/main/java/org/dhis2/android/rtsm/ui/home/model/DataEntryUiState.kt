package org.dhis2.android.rtsm.ui.home.model

import org.dhis2.mobile.commons.providers.InfoBarType

data class DataEntryUiState(
    val step: DataEntryStep = DataEntryStep.START,
    val button: ButtonUiState = ButtonUiState(),
    val hasUnsavedData: Boolean = false,
    val snackBarUiState: SnackBarUiState = SnackBarUiState(),
    val loading: Boolean = false,
    val infoBar: InfoBarType? = null,
)

enum class DataEntryStep {
    START,
    LISTING,
    EDITING_LISTING,
    REVIEWING,
    EDITING_REVIEWING,
    COMPLETED,
}
