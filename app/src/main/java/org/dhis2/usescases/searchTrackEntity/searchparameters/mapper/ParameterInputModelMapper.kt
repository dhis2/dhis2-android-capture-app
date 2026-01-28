package org.dhis2.usescases.searchTrackEntity.searchparameters.mapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.paging.compose.collectAsLazyPagingItems
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.R
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.UiRenderType
import org.dhis2.tracker.ui.input.model.TrackerInputModel
import org.dhis2.tracker.ui.input.model.TrackerInputType
import org.dhis2.tracker.ui.input.model.TrackerOptionItem
import org.dhis2.tracker.ui.input.model.TrackerOptionSetConfiguration
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.mobile.ui.designsystem.component.LegendData
import org.hisp.dhis.mobile.ui.designsystem.component.Orientation

@Composable
fun FieldUiModel.toParameterInputModel(
    fetchOptions: () -> Unit,
    resourceManager: ResourceManager,
): TrackerInputModel {
    val trackerInputType =
        when {
            optionSet != null && valueType != ValueType.MULTI_TEXT -> {
                getInputTypeForOptionSetByRenderingType(renderingType)
            }

            customIntent != null -> {
                TrackerInputType.CUSTOM_INTENT
            }

            eventCategories != null -> {
                TrackerInputType.NOT_SUPPORTED
            }

            else -> getInputTypeByValueType(valueType, renderingType)
        }

    return TrackerInputModel(
        uid = uid,
        label = label,
        value = value,
        focused = focused,
        valueType = trackerInputType,
        optionSet = optionSet,
        error = error,
        warning = warning,
        description = description,
        mandatory = mandatory,
        editable = editable,
        legend =
            legend?.let {
                LegendData(
                    color = Color(it.color),
                    title = it.label ?: "",
                    popUpLegendDescriptionData = it.legendsInfo,
                )
            },
        orientation = renderingType.getOrientation(),
        optionSetConfiguration =
            when (valueType) {
                ValueType.BOOLEAN -> getBooleanOptionConfiguration(resourceManager)
                else -> optionSetConfiguration?.toTrackerOptionSetConfiguration(fetchOptions)
            },
        customIntentUid = customIntent?.uid,
        displayName = displayName,
    )
}

@Composable
private fun org.dhis2.form.model.OptionSetConfiguration.toTrackerOptionSetConfiguration(
    fetchOptions: () -> Unit,
): TrackerOptionSetConfiguration {
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

internal fun getInputTypeForOptionSetByRenderingType(renderingType: UiRenderType?): TrackerInputType =
    when (renderingType) {
        UiRenderType.HORIZONTAL_RADIOBUTTONS,
        UiRenderType.VERTICAL_RADIOBUTTONS,
        -> TrackerInputType.RADIO_BUTTON

        UiRenderType.HORIZONTAL_CHECKBOXES,
        UiRenderType.VERTICAL_CHECKBOXES,
        -> TrackerInputType.CHECKBOX

        UiRenderType.MATRIX -> TrackerInputType.MATRIX

        UiRenderType.SEQUENCIAL -> TrackerInputType.SEQUENTIAL

        else -> TrackerInputType.DROPDOWN
    }

internal fun getInputTypeByValueType(
    valueType: ValueType?,
    renderingType: UiRenderType?,
): TrackerInputType =
    when (valueType) {
        ValueType.TEXT -> {
            when (renderingType) {
                UiRenderType.QR_CODE, UiRenderType.GS1_DATAMATRIX -> TrackerInputType.QR_CODE
                UiRenderType.BAR_CODE -> TrackerInputType.BAR_CODE
                else -> TrackerInputType.TEXT
            }
        }

        ValueType.INTEGER_POSITIVE -> TrackerInputType.INTEGER_POSITIVE
        ValueType.INTEGER_ZERO_OR_POSITIVE -> TrackerInputType.INTEGER_ZERO_OR_POSITIVE
        ValueType.PERCENTAGE -> TrackerInputType.PERCENTAGE
        ValueType.NUMBER -> TrackerInputType.NUMBER
        ValueType.INTEGER_NEGATIVE -> TrackerInputType.INTEGER_NEGATIVE
        ValueType.LONG_TEXT -> TrackerInputType.LONG_TEXT
        ValueType.INTEGER -> TrackerInputType.INTEGER
        ValueType.ORGANISATION_UNIT -> TrackerInputType.ORGANISATION_UNIT
        ValueType.EMAIL -> TrackerInputType.EMAIL
        ValueType.BOOLEAN -> {
            when (renderingType) {
                UiRenderType.HORIZONTAL_CHECKBOXES,
                UiRenderType.VERTICAL_CHECKBOXES,
                -> TrackerInputType.CHECKBOX

                else -> TrackerInputType.RADIO_BUTTON
            }
        }

        ValueType.TRUE_ONLY -> {
            when (renderingType) {
                UiRenderType.TOGGLE -> TrackerInputType.YES_ONLY_SWITCH
                else -> TrackerInputType.YES_ONLY_CHECKBOX
            }
        }

        ValueType.PHONE_NUMBER -> TrackerInputType.PHONE_NUMBER
        ValueType.DATE -> TrackerInputType.DATE
        ValueType.DATETIME -> TrackerInputType.DATE_TIME
        ValueType.TIME -> TrackerInputType.TIME

        ValueType.AGE -> TrackerInputType.AGE
        ValueType.MULTI_TEXT -> TrackerInputType.MULTI_SELECTION

        ValueType.USERNAME,
        ValueType.LETTER,
        ValueType.UNIT_INTERVAL,
        ValueType.TRACKER_ASSOCIATE,
        ValueType.REFERENCE,
        ValueType.COORDINATE,
        ValueType.IMAGE,
        ValueType.FILE_RESOURCE,
        ValueType.GEOJSON,
        ValueType.URL,
        null,
        -> TrackerInputType.NOT_SUPPORTED
    }

internal fun UiRenderType?.getOrientation(): Orientation =
    when (this) {
        UiRenderType.HORIZONTAL_CHECKBOXES,
        UiRenderType.HORIZONTAL_RADIOBUTTONS,
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
