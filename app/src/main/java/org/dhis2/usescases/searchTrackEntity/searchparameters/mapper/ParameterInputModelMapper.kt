package org.dhis2.usescases.searchTrackEntity.searchparameters.mapper

import org.dhis2.tracker.search.model.SearchParameterModel
import org.dhis2.tracker.ui.input.model.TrackerInputModel
import org.dhis2.tracker.ui.input.model.TrackerInputType
import org.hisp.dhis.mobile.ui.designsystem.component.Orientation

fun SearchParameterModel.toTrackerInputModel() =
    TrackerInputModel(
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
