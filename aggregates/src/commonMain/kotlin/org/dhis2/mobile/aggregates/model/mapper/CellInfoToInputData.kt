package org.dhis2.mobile.aggregates.model.mapper

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import org.dhis2.mobile.aggregates.model.CellInfo
import org.dhis2.mobile.aggregates.model.CellValueExtra
import org.dhis2.mobile.aggregates.model.InputType
import org.dhis2.mobile.aggregates.ui.states.InputData
import org.dhis2.mobile.aggregates.ui.states.InputExtra
import org.dhis2.mobile.commons.extensions.getFormattedFileSize
import org.dhis2.mobile.commons.extensions.toColor
import org.hisp.dhis.mobile.ui.designsystem.component.CheckBoxData
import org.hisp.dhis.mobile.ui.designsystem.component.Coordinates
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.component.LegendData
import org.hisp.dhis.mobile.ui.designsystem.component.RadioButtonData
import org.hisp.dhis.mobile.ui.designsystem.component.SelectableDates
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextData
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextState
import org.hisp.dhis.mobile.ui.designsystem.component.model.DateTimeTransformation
import org.hisp.dhis.mobile.ui.designsystem.component.model.DateTransformation
import org.hisp.dhis.mobile.ui.designsystem.component.model.TimeTransformation

internal suspend fun CellInfo.toInputData(cellId: String) = supervisorScope {
    InputData(
        id = cellId,
        label = label,
        value = value,
        displayValue = displayValue,
        inputType = inputType,
        inputShellState = InputShellState.UNFOCUSED,
        inputExtra = when (inputType) {
            InputType.Age -> InputExtra.Age(
                selectableDates = SelectableDates("01011940", "12312300"),
            )

            InputType.Date, InputType.Time, InputType.DateTime ->
                InputExtra.Date(
                    allowManualInput = true,
                    is24HourFormat = true,
                    visualTransformation = when (inputType) {
                        InputType.Date -> DateTransformation()
                        InputType.DateTime -> DateTimeTransformation()
                        InputType.Time -> TimeTransformation()
                        else -> throw IllegalArgumentException("Invalid input type")
                    },
                    selectableDates = SelectableDates("01011940", "12312300"),
                    yearRange = IntRange(1940, 2300),
                )

            InputType.Coordinates -> InputExtra.Coordinate(
                coordinateValue = (inputExtra as? CellValueExtra.Coordinates)?.let {
                    Coordinates(
                        latitude = it.lat,
                        longitude = it.lon,
                    )
                },
            )

            InputType.FileResource -> InputExtra.File(
                fileWeight = value?.let { getFormattedFileSize(value) },
            )

            InputType.MultiText -> (inputExtra as? CellValueExtra.Options)?.let {
                InputExtra.MultiText(
                    numberOfOptions = it.optionCount,
                    options = it.options.map { optionData ->
                        async {
                            CheckBoxData(
                                uid = optionData.code ?: optionData.uid,
                                checked = optionData.code?.let { code ->
                                    value?.split(",")
                                        ?.find { valueCode -> valueCode == code } != null
                                } ?: false,
                                enabled = true,
                                textInput = optionData.label,
                            )
                        }
                    }.awaitAll(),
                    optionsFetched = it.optionsFetched,
                )
            } ?: InputExtra.None

            InputType.OptionSet -> (inputExtra as? CellValueExtra.Options)?.let {
                InputExtra.OptionSet(
                    numberOfOptions = it.optionCount,
                    options = it.options.map { optionData ->
                        async {
                            RadioButtonData(
                                uid = optionData.code ?: optionData.uid,
                                selected = optionData.code?.let { code ->
                                    value == code
                                } ?: false,
                                enabled = true,
                                textInput = optionData.label,
                            )
                        }
                    }.awaitAll(),
                    optionsFetched = it.optionsFetched,
                )
            } ?: InputExtra.None

            else -> InputExtra.None
        },
        supportingText = supportingText.map { text ->
            SupportingTextData(
                text = text,
                state = SupportingTextState.DEFAULT,
            )
        } + errors.map { error ->
            SupportingTextData(
                text = error,
                state = SupportingTextState.ERROR,
            )
        } + warnings.map { warning ->
            SupportingTextData(
                text = warning,
                state = SupportingTextState.WARNING,
            )
        },
        legendData = if (legendLabel != null) {
            LegendData(
                color = legendColor?.toColor() ?: Color.Unspecified,
                title = legendLabel,
            )
        } else {
            null
        },
        isRequired = isRequired,
    )
}
