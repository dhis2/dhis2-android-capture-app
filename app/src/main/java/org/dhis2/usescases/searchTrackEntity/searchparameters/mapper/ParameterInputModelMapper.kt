package org.dhis2.usescases.searchTrackEntity.searchparameters.mapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.Flow
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.R
import org.dhis2.tracker.search.model.SearchParameterModel
import org.dhis2.tracker.ui.input.model.TrackerInputModel
import org.dhis2.tracker.ui.input.model.TrackerInputType
import org.dhis2.tracker.ui.input.model.TrackerOptionItem
import org.dhis2.tracker.ui.input.model.TrackerOptionSetConfiguration
import org.hisp.dhis.mobile.ui.designsystem.component.Orientation

/**
 * Converts SearchParameterModel to TrackerInputModel for UI rendering.
 *
 * @param resourceManager For resolving string resources
 */
fun SearchParameterModel.toTrackerInputModel(resourceManager: ResourceManager): TrackerInputModel =
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
        optionSetConfiguration =
            when {
                inputType == TrackerInputType.YES_ONLY_CHECKBOX ||
                    inputType == TrackerInputType.YES_ONLY_SWITCH ->
                    getBooleanOptionConfiguration(resourceManager)
                else -> null
            },
        customIntentUid = customIntentUid,
        displayName = null,
        orgUnitSelectorScope = null,
        searchOperator = searchOperator,
        minCharactersToSearch = minCharactersToSearch,
    )

/**
 * Composable extension to enrich TrackerInputModel with option set data from a flow.
 * Use this in the UI layer to dynamically populate option sets.
 */
@Composable
fun TrackerInputModel.enrichWithOptionSetFlow(
    optionSetFlow: Flow<PagingData<TrackerOptionItem>>?,
    onSearch: ((String) -> Unit)?,
): TrackerInputModel {
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

private fun TrackerInputType.getOrientation(): Orientation =
    when (this) {
        TrackerInputType.HORIZONTAL_CHECKBOXES,
        TrackerInputType.HORIZONTAL_RADIOBUTTONS,
        -> Orientation.HORIZONTAL

        else -> Orientation.VERTICAL
    }

internal fun getBooleanOptionConfiguration(resourceManager: ResourceManager) =
    TrackerOptionSetConfiguration(
        options =
            listOf(
                TrackerOptionItem(
                    true.toString(),
                    resourceManager.getString(R.string.yes),
                ),
                TrackerOptionItem(
                    false.toString(),
                    resourceManager.getString(R.string.no),
                ),
            ),
    )
