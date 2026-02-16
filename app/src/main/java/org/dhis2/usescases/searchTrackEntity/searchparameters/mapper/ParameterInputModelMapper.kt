package org.dhis2.usescases.searchTrackEntity.searchparameters.mapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.paging.compose.collectAsLazyPagingItems
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.R
import org.dhis2.form.model.OptionSetConfiguration
import org.dhis2.tracker.search.model.SearchParameterModel
import org.dhis2.tracker.ui.input.model.TrackerInputModel
import org.dhis2.tracker.ui.input.model.TrackerInputType
import org.dhis2.tracker.ui.input.model.TrackerOptionItem
import org.dhis2.tracker.ui.input.model.TrackerOptionSetConfiguration
import org.hisp.dhis.mobile.ui.designsystem.component.Orientation

// TODO remove Mapper
fun SearchParameterModel.toTrackerInputModel(): TrackerInputModel {
    // TODO manage optionSet Flow configuration

    return TrackerInputModel(
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
        optionSetConfiguration = null, // TODO(Remove when refactored option set configuration)
//            when (inputType) {
//                ValueType.BOOLEAN -> getBooleanOptionConfiguration(resourceManager)
//                else -> optionSetConfiguration?.toTrackerOptionSetConfiguration(fetchOptions)
//            },
        customIntentUid = customIntentUid,
        displayName = null,
        orgUnitSelectorScope = null,
        searchOperator = searchOperator,
        minCharactersToSearch = minCharactersToSearch,
    )
}

private fun TrackerInputType.getOrientation(): Orientation =
    when (this) {
        TrackerInputType.HORIZONTAL_CHECKBOXES,
        TrackerInputType.HORIZONTAL_RADIOBUTTONS,
        -> Orientation.HORIZONTAL

        else -> Orientation.VERTICAL
    }

// TODO(Remove when refactored option set configuration)
@Composable
private fun OptionSetConfiguration.toTrackerOptionSetConfiguration(fetchOptions: () -> Unit): TrackerOptionSetConfiguration {
    val optionsData =
        optionFlow
            .collectAsLazyPagingItems()
            .also { LaunchedEffect(this) { it.refresh() } }

    val options by remember {
        derivedStateOf {
            (0 until (optionsData.itemCount)).mapNotNull { index ->
                optionsData[index]?.let { optionData ->
                    TrackerOptionItem(
                        code = optionData.option.code() ?: "",
                        displayName = optionData.option.displayName() ?: "",
                    )
                }
            }
        }
    }

    return TrackerOptionSetConfiguration(
        options = options,
        onSearch = onSearch,
        onLoadOptions = fetchOptions,
    )
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
