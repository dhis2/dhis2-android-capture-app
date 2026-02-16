package org.dhis2.tracker.ui.input.model

import androidx.compose.runtime.Composable
import org.dhis2.mobile.commons.orgunit.OrgUnitSelectorScope
import org.dhis2.mobile.tracker.resources.Res
import org.dhis2.mobile.tracker.resources.end_with_search_operator
import org.dhis2.mobile.tracker.resources.equal_search_operator
import org.dhis2.mobile.tracker.resources.starts_with_search_operator
import org.dhis2.tracker.search.model.SearchOperator
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.component.LegendData
import org.hisp.dhis.mobile.ui.designsystem.component.Orientation
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextData
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextState
import org.jetbrains.compose.resources.stringResource

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
    val optionSetConfiguration: TrackerOptionSetConfiguration?, // TODO (Maybe remove when migrated)
    val customIntentUid: String?,
    val displayName: String?,
    val orgUnitSelectorScope: OrgUnitSelectorScope?,
    val searchOperator: SearchOperator?,
    val minCharactersToSearch: Int?,
)

@Composable
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
        searchOperator?.let { operator ->
            operator.supportingTextString()?.let { text ->
                SupportingTextData(
                    text,
                    SupportingTextState.DEFAULT,
                )
            }
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

@Composable
private fun SearchOperator.supportingTextString() =
    when (this) {
        SearchOperator.EQ ->
            stringResource(Res.string.equal_search_operator)
        SearchOperator.SW ->
            stringResource(Res.string.starts_with_search_operator)
        SearchOperator.EW ->
            stringResource(Res.string.end_with_search_operator)
        else -> null
    }
