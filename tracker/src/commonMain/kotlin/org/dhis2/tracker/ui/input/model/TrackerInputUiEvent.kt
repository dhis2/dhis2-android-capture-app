package org.dhis2.tracker.ui.input.model

sealed interface TrackerInputUiEvent {
    data class OnQRButtonClicked(
        val uid: String,
    ) : TrackerInputUiEvent

    data class OnBarcodeButtonClicked(
        val uid: String,
    ) : TrackerInputUiEvent

    data class OnOrgUnitButtonClicked(
        val uid: String,
        val label: String,
        val value: String?,
    ) : TrackerInputUiEvent

    data class OnLaunchCustomIntent(
        val uid: String,
        val customIntentUid: String,
    ) : TrackerInputUiEvent

    data class OnItemClick(
        val uid: String,
    ) : TrackerInputUiEvent

    data class OnValueChange(
        val uid: String,
        val value: String?,
    ) : TrackerInputUiEvent
}
