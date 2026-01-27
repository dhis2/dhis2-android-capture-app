package org.dhis2.tracker.ui.input.model

import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.component.LegendData
import org.hisp.dhis.mobile.ui.designsystem.component.Orientation
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextData
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextState

data class TrackerInputModel(
    val uid: String,
    val label: String,
    val value: String?,
    val focused: Boolean,
    val valueType: TrackerInputType,
    val optionSet: String?,
    val error: String?,
    val warning: String?,
    val description: String?,
    val mandatory: Boolean,
    val editable: Boolean,
    val legend: LegendData?,
    val orientation: Orientation,
    val optionSetConfiguration: TrackerOptionSetConfiguration? = null,
    val customIntentUid: String? = null,
    val displayName: String?,
)

fun TrackerInputModel.supportingText(): List<SupportingTextData>? =
    listOfNotNull(
        error?.let {
            SupportingTextData(
                it,
                SupportingTextState.ERROR,
            )
        },
        warning?.let {
            SupportingTextData(
                it,
                SupportingTextState.WARNING,
            )
        },
        description?.let {
            SupportingTextData(
                it,
                SupportingTextState.DEFAULT,
            )
        },
    ).ifEmpty { null }

fun TrackerInputModel.inputState(): InputShellState =
    when {
        !editable -> InputShellState.DISABLED
        error != null -> InputShellState.ERROR
        focused -> InputShellState.FOCUSED
        else -> InputShellState.UNFOCUSED
    }
