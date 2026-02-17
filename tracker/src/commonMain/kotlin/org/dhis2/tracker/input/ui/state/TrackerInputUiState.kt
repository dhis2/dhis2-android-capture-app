package org.dhis2.tracker.input.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.Flow
import org.dhis2.mobile.commons.orgunit.OrgUnitSelectorScope
import org.dhis2.mobile.tracker.resources.Res
import org.dhis2.mobile.tracker.resources.end_with_search_operator
import org.dhis2.mobile.tracker.resources.equal_search_operator
import org.dhis2.mobile.tracker.resources.no
import org.dhis2.mobile.tracker.resources.starts_with_search_operator
import org.dhis2.mobile.tracker.resources.yes
import org.dhis2.tracker.input.model.TrackerInputType
import org.dhis2.tracker.search.model.SearchOperator
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.component.LegendData
import org.hisp.dhis.mobile.ui.designsystem.component.Orientation
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextData
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextState
import org.jetbrains.compose.resources.stringResource

data class TrackerInputUiState(
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
    val optionSetConfiguration: TrackerOptionSetConfiguration?,
    val customIntentUid: String?,
    val displayName: String?,
    val orgUnitSelectorScope: OrgUnitSelectorScope?,
    val searchOperator: SearchOperator?,
    val minCharactersToSearch: Int?,
)

@Composable
fun TrackerInputUiState.supportingText(): List<SupportingTextData>? =
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

fun TrackerInputUiState.inputState(): InputShellState =
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

@Composable
fun TrackerInputUiState.loadOptionSetConfiguration(
    getOptionSetFlow: (fieldUid: String, optionSetUid: String) -> Flow<PagingData<TrackerOptionItem>>?,
    onOptionSetSearch: (fieldUid: String, query: String) -> Unit,
): TrackerInputUiState =
    when {
        valueType == TrackerInputType.YES_ONLY_CHECKBOX ||
            valueType == TrackerInputType.YES_ONLY_SWITCH ->
            this.getBooleanOptionConfiguration()

        optionSet != null -> {
            val optionSetFlow =
                getOptionSetFlow(
                    uid,
                    optionSet,
                )
            this.loadWithOptionSetFlow(
                optionSetFlow = optionSetFlow,
                onSearch = { query ->
                    onOptionSetSearch(uid, query)
                },
            )
        }

        else -> this
    }

@Composable
private fun TrackerInputUiState.getBooleanOptionConfiguration(): TrackerInputUiState {
    val booleanConfiguration =
        TrackerOptionSetConfiguration(
            options =
                listOf(
                    TrackerOptionItem(
                        code = true.toString(),
                        displayName = stringResource(Res.string.yes),
                    ),
                    TrackerOptionItem(
                        code = false.toString(),
                        displayName = stringResource(Res.string.no),
                    ),
                ),
        )

    return copy(optionSetConfiguration = booleanConfiguration)
}

@Composable
private fun TrackerInputUiState.loadWithOptionSetFlow(
    optionSetFlow: Flow<PagingData<TrackerOptionItem>>?,
    onSearch: ((String) -> Unit)?,
): TrackerInputUiState {
    if (optionSetFlow == null) return this

    val optionsData = optionSetFlow.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        optionsData.refresh()
    }

    val options by remember {
        derivedStateOf {
            (0 until optionsData.itemCount).mapNotNull { index ->
                optionsData[index]
            }
        }
    }

    return copy(
        optionSetConfiguration =
            TrackerOptionSetConfiguration(
                options = options,
                onSearch = onSearch,
                onLoadOptions = { optionsData.refresh() },
            ),
    )
}
