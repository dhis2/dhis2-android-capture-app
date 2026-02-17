package org.dhis2.tracker.ui.input.model

import org.dhis2.mobile.commons.orgunit.OrgUnitSelectorScope

sealed interface TrackerInputUiEvent {
    data class OnScanButtonClicked(
        val uid: String,
        val optionSet: String?,
        val renderType: TrackerInputType,
    ) : TrackerInputUiEvent

    data class OnOrgUnitButtonClicked(
        val uid: String,
        val label: String,
        val value: String?,
        val orgUnitSelectorScope: OrgUnitSelectorScope?,
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
