package org.dhis2.tracker.input.model

sealed interface TrackerInputUiEvent {
    data class OnQRButtonClicked(
        val uid: String,
    ) : TrackerInputUiEvent

    data class OnBarcodeButtonClicked(
        val uid: String,
    ) : TrackerInputUiEvent

    data class OnOrgUnitButtonClicked(
        val uid: String,
    ) : TrackerInputUiEvent
}
