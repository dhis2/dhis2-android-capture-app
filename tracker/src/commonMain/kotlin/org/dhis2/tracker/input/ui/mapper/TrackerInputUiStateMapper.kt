package org.dhis2.tracker.input.ui.mapper

import org.dhis2.tracker.input.model.TrackerInputType
import org.dhis2.tracker.input.ui.state.TrackerInputUiState
import org.dhis2.tracker.search.model.SearchParameterModel
import org.hisp.dhis.mobile.ui.designsystem.component.Orientation

fun SearchParameterModel.toTrackerInputUiState() =
    TrackerInputUiState(
        uid = uid,
        label = label,
        value = null,
        focused = false,
        valueType = inputType,
        optionSet = optionSet,
        error = null,
        warning = null,
        description = null,
        mandatory = false,
        editable = true,
        legend = null,
        orientation = inputType.getOrientation(),
        optionSetConfiguration = null,
        customIntentUid = customIntentUid,
        displayName = null,
        orgUnitSelectorScope = null,
        searchOperator = searchOperator,
        minCharactersToSearch = minCharactersToSearch,
    )

private fun TrackerInputType.getOrientation(): Orientation =
    when (this) {
        TrackerInputType.HORIZONTAL_CHECKBOXES,
        TrackerInputType.HORIZONTAL_RADIOBUTTONS,
            -> Orientation.HORIZONTAL

        else -> Orientation.VERTICAL
    }