package org.dhis2.mobile.aggregates.ui.states.mapper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.Done
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import org.dhis2.mobile.aggregates.model.CellInfo
import org.dhis2.mobile.aggregates.model.CellValueExtra
import org.dhis2.mobile.aggregates.ui.provider.ResourceManager
import org.dhis2.mobile.aggregates.ui.states.ButtonAction
import org.dhis2.mobile.aggregates.ui.states.CellSelectionState.InputDataUiState
import org.dhis2.mobile.commons.extensions.toColor
import org.dhis2.mobile.commons.input.InputExtra
import org.dhis2.mobile.commons.input.InputType
import org.hisp.dhis.mobile.ui.designsystem.component.CheckBoxData
import org.hisp.dhis.mobile.ui.designsystem.component.Coordinates
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.component.LegendData
import org.hisp.dhis.mobile.ui.designsystem.component.RadioButtonData
import org.hisp.dhis.mobile.ui.designsystem.component.SelectableDates
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextData
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextState
import org.hisp.dhis.mobile.ui.designsystem.component.UploadFileState
import org.hisp.dhis.mobile.ui.designsystem.component.model.DateTimeTransformation
import org.hisp.dhis.mobile.ui.designsystem.component.model.DateTransformation
import org.hisp.dhis.mobile.ui.designsystem.component.model.TimeTransformation
import org.hisp.dhis.mobile.ui.designsystem.component.table.ui.TableSelection

internal class InputDataUiStateMapper(
    private val resourceManager: ResourceManager,
) {
    suspend fun map(
        cellId: String,
        cellInfo: CellInfo,
        validationError: String?,
        valueWithError: String?,
        currentCell: TableSelection.CellSelection?,
        isLastCell: Boolean,
    ) = supervisorScope {
        InputDataUiState(
            id = cellId,
            label = cellInfo.label,
            value = cellInfo.value,
            displayValue =
                valueWithError?.takeIf { valueWithError.isNotEmpty() }
                    ?: cellInfo.displayValue,
            inputType = cellInfo.inputType,
            inputShellState =
                when {
                    validationError != null || cellInfo.errors.isNotEmpty() -> InputShellState.ERROR
                    cellInfo.warnings.isNotEmpty() -> InputShellState.WARNING
                    else -> InputShellState.FOCUSED
                },
            inputExtra =
                when (cellInfo.inputType) {
                    InputType.Age -> InputExtra.Age

                    InputType.Date, InputType.Time, InputType.DateTime ->
                        InputExtra.Date(
                            allowManualInput = true,
                            is24HourFormat = true,
                            visualTransformation =
                                when (cellInfo.inputType) {
                                    InputType.Date -> DateTransformation()
                                    InputType.DateTime -> DateTimeTransformation()
                                    InputType.Time -> TimeTransformation()
                                    else -> throw IllegalArgumentException("Invalid input type")
                                },
                            selectableDates = SelectableDates("01011940", "12312300"),
                            yearRange = IntRange(1940, 2300),
                        )

                    InputType.Coordinates ->
                        InputExtra.Coordinate(
                            coordinateValue =
                                (cellInfo.inputExtra as? CellValueExtra.Coordinates)?.let {
                                    Coordinates(
                                        latitude = it.lat,
                                        longitude = it.lon,
                                    )
                                },
                        )

                    InputType.FileResource,
                    InputType.Image,
                    ->
                        (cellInfo.inputExtra as? CellValueExtra.FileResource)?.let {
                            InputExtra.File(
                                fileState =
                                    when (cellInfo.value) {
                                        null -> UploadFileState.ADD
                                        else -> UploadFileState.LOADED
                                    },
                                filePath = it.filePath,
                                fileWeight = it.fileWeight,
                            )
                        } ?: InputExtra.File(UploadFileState.ADD, null, null)

                    InputType.MultiText ->
                        (cellInfo.inputExtra as? CellValueExtra.Options)?.let {
                            InputExtra.MultiText(
                                numberOfOptions = it.optionCount,
                                options =
                                    it.options
                                        .map { optionData ->
                                            async {
                                                CheckBoxData(
                                                    uid = optionData.code ?: optionData.uid,
                                                    checked =
                                                        optionData.code?.let { code ->
                                                            cellInfo.value
                                                                ?.split(",")
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

                    InputType.OptionSet ->
                        (cellInfo.inputExtra as? CellValueExtra.Options)?.let {
                            InputExtra.OptionSet(
                                numberOfOptions = it.optionCount,
                                options =
                                    it.options
                                        .map { optionData ->
                                            async {
                                                RadioButtonData(
                                                    uid = optionData.code ?: optionData.uid,
                                                    selected =
                                                        optionData.code?.let { code ->
                                                            cellInfo.value == code
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
            supportingText =
                cellInfo.supportingText.map { text ->
                    SupportingTextData(
                        text = text,
                        state = SupportingTextState.DEFAULT,
                    )
                } +
                    cellInfo.errors.plus(validationError).filterNotNull().map { error ->
                        SupportingTextData(
                            text = error,
                            state = SupportingTextState.ERROR,
                        )
                    } +
                    cellInfo.warnings.map { warning ->
                        SupportingTextData(
                            text = warning,
                            state = SupportingTextState.WARNING,
                        )
                    },
            legendData =
                if (cellInfo.legendLabel != null) {
                    LegendData(
                        color = cellInfo.legendColor?.toColor() ?: Color.Unspecified,
                        title = cellInfo.legendLabel,
                    )
                } else {
                    null
                },
            isRequired = cellInfo.isRequired,
            buttonAction =
                getButtonAction(
                    isLastCell,
                ),
            currentSelectedCell = currentCell,
        )
    }

    private suspend fun getButtonAction(isLastCell: Boolean): ButtonAction =
        if (isLastCell) {
            ButtonAction(
                buttonText = resourceManager.provideDone(),
                icon = Icons.Default.Done,
                isDoneAction = true,
            )
        } else {
            ButtonAction(
                buttonText = resourceManager.provideNext(),
                icon = Icons.AutoMirrored.Outlined.ArrowForward,
                isDoneAction = false,
            )
        }
}
