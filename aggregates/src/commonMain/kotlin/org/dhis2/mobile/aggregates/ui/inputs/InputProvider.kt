package org.dhis2.mobile.aggregates.ui.inputs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import org.dhis2.mobile.aggregates.model.InputType
import org.dhis2.mobile.aggregates.resources.Res
import org.dhis2.mobile.aggregates.resources.action_done
import org.dhis2.mobile.aggregates.resources.format_error
import org.dhis2.mobile.aggregates.resources.input_action_accept
import org.dhis2.mobile.aggregates.resources.input_action_cancel
import org.dhis2.mobile.aggregates.resources.input_age
import org.dhis2.mobile.aggregates.resources.input_age_date_of_birth
import org.dhis2.mobile.aggregates.resources.input_age_or
import org.dhis2.mobile.aggregates.resources.input_coordinate_add_location
import org.dhis2.mobile.aggregates.resources.input_coordinate_latitude
import org.dhis2.mobile.aggregates.resources.input_coordinate_longitude
import org.dhis2.mobile.aggregates.resources.input_date_out_of_range
import org.dhis2.mobile.aggregates.resources.input_not_supported
import org.dhis2.mobile.aggregates.resources.no_results_found
import org.dhis2.mobile.aggregates.resources.search_to_find_more
import org.dhis2.mobile.aggregates.ui.states.InputData
import org.hisp.dhis.mobile.ui.designsystem.component.AgeInputType
import org.hisp.dhis.mobile.ui.designsystem.component.CheckBoxData
import org.hisp.dhis.mobile.ui.designsystem.component.DateTimeActionType
import org.hisp.dhis.mobile.ui.designsystem.component.InputAge
import org.hisp.dhis.mobile.ui.designsystem.component.InputCoordinate
import org.hisp.dhis.mobile.ui.designsystem.component.InputDateTime
import org.hisp.dhis.mobile.ui.designsystem.component.InputEmail
import org.hisp.dhis.mobile.ui.designsystem.component.InputFileResource
import org.hisp.dhis.mobile.ui.designsystem.component.InputImage
import org.hisp.dhis.mobile.ui.designsystem.component.InputInteger
import org.hisp.dhis.mobile.ui.designsystem.component.InputLetter
import org.hisp.dhis.mobile.ui.designsystem.component.InputLink
import org.hisp.dhis.mobile.ui.designsystem.component.InputLongText
import org.hisp.dhis.mobile.ui.designsystem.component.InputMultiSelection
import org.hisp.dhis.mobile.ui.designsystem.component.InputNegativeInteger
import org.hisp.dhis.mobile.ui.designsystem.component.InputNotSupported
import org.hisp.dhis.mobile.ui.designsystem.component.InputNumber
import org.hisp.dhis.mobile.ui.designsystem.component.InputOrgUnit
import org.hisp.dhis.mobile.ui.designsystem.component.InputPercentage
import org.hisp.dhis.mobile.ui.designsystem.component.InputPhoneNumber
import org.hisp.dhis.mobile.ui.designsystem.component.InputPositiveInteger
import org.hisp.dhis.mobile.ui.designsystem.component.InputPositiveIntegerOrZero
import org.hisp.dhis.mobile.ui.designsystem.component.InputText
import org.hisp.dhis.mobile.ui.designsystem.component.InputUnitInterval
import org.hisp.dhis.mobile.ui.designsystem.component.InputYesNoField
import org.hisp.dhis.mobile.ui.designsystem.component.InputYesNoFieldValues
import org.hisp.dhis.mobile.ui.designsystem.component.InputYesOnlyCheckBox
import org.hisp.dhis.mobile.ui.designsystem.component.UploadFileState
import org.hisp.dhis.mobile.ui.designsystem.component.UploadState
import org.hisp.dhis.mobile.ui.designsystem.component.model.RegExValidations
import org.hisp.dhis.mobile.ui.designsystem.component.state.InputAgeData
import org.hisp.dhis.mobile.ui.designsystem.component.state.InputDateTimeData
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberInputAgeState
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberInputDateTimeState
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing.Spacing0
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun InputProvider(
    modifier: Modifier = Modifier,
    inputData: InputData,
    onAction: (UiAction) -> Unit,
) {
    val savedTextSelection by remember(inputData.value) {
        mutableStateOf(
            TextRange(inputData.value?.length ?: 0),
        )
    }

    var textValue by remember(inputData.value) {
        mutableStateOf(
            TextFieldValue(
                text = inputData.value ?: "",
                selection = savedTextSelection,
            ),
        )
    }

    val imeAction by remember(inputData) {
        mutableStateOf(ImeAction.Next) // TODO: Update from inputData
    }

    when (inputData.inputType) {
        InputType.Age -> {
            InputAge(
                state = rememberInputAgeState(
                    inputAgeData = InputAgeData(
                        title = inputData.label,
                        inputStyle = inputData.inputStyle,
                        isRequired = inputData.isRequired,
                        imeAction = imeAction,
                        dateOfBirthLabel = stringResource(Res.string.input_age_date_of_birth),
                        orLabel = stringResource(Res.string.input_age_or),
                        ageLabel = stringResource(Res.string.input_age),
                        acceptText = stringResource(Res.string.input_action_accept),
                        cancelText = stringResource(Res.string.input_action_cancel),
                        is24hourFormat = true,
                        selectableDates = inputData.ageExtras().selectableDates,
                    ),
                    inputType = inputData.value?.let { AgeInputType.None }
                        ?: AgeInputType.DateOfBirth(textValue),
                    inputState = inputData.inputShellState,
                    legendData = inputData.legendData,
                    supportingText = inputData.supportingText,
                ),
                onValueChanged = {
                    val value = when (it) {
                        is AgeInputType.Age -> it.value.text
                        is AgeInputType.DateOfBirth -> it.value.text
                        else -> null
                    }
                    onAction(UiAction.OnValueChanged(inputData.id, value))
                },
                onNextClicked = { onAction.invoke(UiAction.OnNextClick(inputData.id)) },
                modifier = modifier,
            )
        }

        InputType.Boolean -> {
            InputYesNoField(
                title = inputData.label,
                modifier = modifier,
                state = inputData.inputShellState,
                inputStyle = inputData.inputStyle,
                supportingText = inputData.supportingText,
                legendData = inputData.legendData,
                isRequired = inputData.isRequired,
                itemSelected = inputData.value?.let {
                    when (it.toBoolean()) {
                        true -> InputYesNoFieldValues.YES
                        false -> InputYesNoFieldValues.NO
                    }
                },
                onItemChange = {
                    val value = when (it) {
                        InputYesNoFieldValues.YES -> true
                        InputYesNoFieldValues.NO -> false
                        null -> null
                    }
                    onAction(UiAction.OnValueChanged(inputData.id, value?.toString()))
                },
            )
        }

        InputType.Coordinates -> {
            InputCoordinate(
                title = inputData.label,
                state = inputData.inputShellState,
                inputStyle = inputData.inputStyle,
                supportingText = inputData.supportingText,
                legendData = inputData.legendData,
                coordinates = inputData.coordinateExtras().coordinateValue,
                latitudeText = stringResource(Res.string.input_coordinate_latitude),
                longitudeText = stringResource(Res.string.input_coordinate_longitude),
                addLocationBtnText = stringResource(Res.string.input_coordinate_add_location),
                isRequired = inputData.isRequired,
                modifier = modifier,
                onResetButtonClicked = {
                    onAction(UiAction.OnValueChanged(inputData.id, null))
                },
                onUpdateButtonClicked = {
                    onAction(
                        UiAction.OnCaptureCoordinates(
                            cellId = inputData.id,
                            initialData = inputData.coordinateExtras().coordinateValue,
                            locationType = "POINT", // TODO change by featury type or domain class
                        ),
                    )
                },
            )
        }

        InputType.DateTime, InputType.Date, InputType.Time -> {
            InputDateTime(
                state = rememberInputDateTimeState(
                    inputDateTimeData = InputDateTimeData(
                        title = inputData.label,
                        inputStyle = inputData.inputStyle,
                        imeAction = imeAction,
                        isRequired = inputData.isRequired,
                        actionType = when (inputData.inputType) {
                            InputType.Date -> DateTimeActionType.DATE
                            InputType.DateTime -> DateTimeActionType.DATE_TIME
                            InputType.Time -> DateTimeActionType.TIME
                            else -> throw IllegalArgumentException("Invalid input type")
                        },
                        allowsManualInput = inputData.dateExtras().allowManualInput,
                        visualTransformation = inputData.dateExtras().visualTransformation,
                        is24hourFormat = inputData.dateExtras().is24HourFormat,
                        acceptText = stringResource(Res.string.input_action_accept),
                        cancelText = stringResource(Res.string.input_action_cancel),
                        outOfRangeText = stringResource(Res.string.input_date_out_of_range),
                        incorrectHourFormatText = stringResource(Res.string.format_error),
                        selectableDates = inputData.dateExtras().selectableDates,
                        yearRange = inputData.dateExtras().yearRange,
                    ),
                    inputTextFieldValue = textValue,
                    inputState = inputData.inputShellState,
                    legendData = inputData.legendData,
                    supportingText = inputData.supportingText,
                ),
                onFocusChanged = { onAction.invoke(UiAction.OnFocusChanged(inputData.id, it)) },
                onValueChanged = {
                    textValue = it ?: TextFieldValue()
                    onAction(UiAction.OnValueChanged(inputData.id, textValue.text))
                },
                onNextClicked = { onAction(UiAction.OnNextClick(inputData.id)) },
                onActionClicked = {
                    onAction(
                        UiAction.OnDateTimeAction(
                            inputData.id,
                            textValue.text,
                        ),
                    )
                },
                modifier = modifier,
            )
        }

        InputType.Email -> {
            InputEmail(
                title = inputData.label,
                state = inputData.inputShellState,
                supportingText = inputData.supportingText,
                legendData = inputData.legendData,
                inputTextFieldValue = textValue,
                isRequiredField = inputData.isRequired,
                onNextClicked = { onAction.invoke(UiAction.OnNextClick(inputData.id)) },
                onValueChanged = {
                    textValue = it ?: TextFieldValue()
                    onAction(UiAction.OnValueChanged(inputData.id, textValue.text))
                },
                onFocusChanged = { onAction(UiAction.OnFocusChanged(inputData.id, it)) },
                imeAction = imeAction,
                modifier = modifier,
                onEmailActionCLicked = {
                    onAction(
                        UiAction.OnEmailAction(
                            inputData.id,
                            textValue.text,
                        ),
                    )
                },
                inputStyle = inputData.inputStyle,
            )
        }

        InputType.FileResource -> {
            var uploadingState by remember(inputData.value) {
                mutableStateOf(
                    when (inputData.value) {
                        null -> UploadFileState.ADD
                        else -> UploadFileState.LOADED
                    },
                )
            }

            InputFileResource(
                title = inputData.label,
                buttonText = "button text",
                fileName = inputData.value,
                fileWeight = inputData.fileExtras().fileWeight,
                onSelectFile = {
                    uploadingState = UploadFileState.UPLOADING
                    onAction(UiAction.OnSelectFile(inputData.id))
                },
                onUploadFile = {
                    uploadingState = UploadFileState.UPLOADING
                    onAction(UiAction.OnOpenFile(inputData.id))
                },
                onClear = {
                    uploadingState = UploadFileState.UPLOADING
                    onAction(UiAction.OnValueChanged(inputData.id, null))
                },
                uploadFileState = uploadingState,
                inputShellState = inputData.inputShellState,
                inputStyle = inputData.inputStyle,
                supportingText = inputData.supportingText,
                legendData = inputData.legendData,
                isRequired = inputData.isRequired,
                modifier = modifier,
            )
        }

        InputType.Image -> {
            var uploadingState by remember(inputData.value) {
                mutableStateOf(
                    when (inputData.value) {
                        null -> UploadState.ADD
                        else -> UploadState.LOADED
                    },
                )
            }

            InputImage(
                title = inputData.label,
                state = inputData.inputShellState,
                inputStyle = inputData.inputStyle,
                supportingText = inputData.supportingText,
                legendData = inputData.legendData,
                uploadState = uploadingState,
                addImageBtnText = "Add image",
                downloadButtonVisible = inputData.value != null,
                isRequired = inputData.isRequired,
                load = { TODO() },
                painterFor = { remember { it } },
                modifier = modifier,
                onDownloadButtonClick = { onAction(UiAction.OnDownloadImage(inputData.id)) },
                onShareButtonClick = { onAction(UiAction.OnShareImage(inputData.id)) },
                onResetButtonClicked = { onAction(UiAction.OnValueChanged(inputData.id, null)) },
                onAddButtonClicked = { onAction(UiAction.OnAddImage(inputData.id)) },
            )
        }

        InputType.Integer -> {
            InputInteger(
                title = inputData.label,
                state = inputData.inputShellState,
                inputStyle = inputData.inputStyle,
                supportingText = inputData.supportingText,
                legendData = inputData.legendData,
                inputTextFieldValue = textValue,
                isRequiredField = inputData.isRequired,
                onNextClicked = { onAction.invoke(UiAction.OnNextClick(inputData.id)) },
                onValueChanged = {
                    textValue = it ?: TextFieldValue()
                    onAction(UiAction.OnValueChanged(inputData.id, textValue.text))
                },
                onFocusChanged = { onAction.invoke(UiAction.OnFocusChanged(inputData.id, it)) },
                imeAction = imeAction,
                modifier = modifier,
            )
        }

        InputType.IntegerNegative -> {
            InputNegativeInteger(
                title = inputData.label,
                state = inputData.inputShellState,
                inputStyle = inputData.inputStyle,
                supportingText = inputData.supportingText,
                legendData = inputData.legendData,
                inputTextFieldValue = textValue,
                isRequiredField = inputData.isRequired,
                onNextClicked = { onAction.invoke(UiAction.OnNextClick(inputData.id)) },
                onValueChanged = {
                    textValue = it ?: TextFieldValue()
                    onAction(UiAction.OnValueChanged(inputData.id, textValue.text))
                },
                onFocusChanged = { onAction.invoke(UiAction.OnFocusChanged(inputData.id, it)) },
                imeAction = imeAction,
                modifier = modifier,
            )
        }

        InputType.IntegerPositive -> {
            InputPositiveInteger(
                title = inputData.label,
                state = inputData.inputShellState,
                inputStyle = inputData.inputStyle,
                supportingText = inputData.supportingText,
                legendData = inputData.legendData,
                inputTextFieldValue = textValue,
                isRequiredField = inputData.isRequired,
                onNextClicked = { onAction.invoke(UiAction.OnNextClick(inputData.id)) },
                onValueChanged = {
                    textValue = it ?: TextFieldValue()
                    onAction(UiAction.OnValueChanged(inputData.id, textValue.text))
                },
                onFocusChanged = { onAction.invoke(UiAction.OnFocusChanged(inputData.id, it)) },
                imeAction = imeAction,
                modifier = modifier,
            )
        }

        InputType.IntegerZeroOrPositive -> {
            InputPositiveIntegerOrZero(
                title = inputData.label,
                state = inputData.inputShellState,
                inputStyle = inputData.inputStyle,
                supportingText = inputData.supportingText,
                legendData = inputData.legendData,
                inputTextFieldValue = textValue,
                isRequiredField = inputData.isRequired,
                onNextClicked = { onAction.invoke(UiAction.OnNextClick(inputData.id)) },
                onValueChanged = {
                    textValue = it ?: TextFieldValue()
                    onAction(UiAction.OnValueChanged(inputData.id, textValue.text))
                },
                onFocusChanged = { onAction.invoke(UiAction.OnFocusChanged(inputData.id, it)) },
                imeAction = imeAction,
                modifier = modifier,
            )
        }

        InputType.Letter -> {
            InputLetter(
                title = inputData.label,
                state = inputData.inputShellState,
                inputStyle = inputData.inputStyle,
                supportingText = inputData.supportingText,
                legendData = inputData.legendData,
                inputTextFieldValue = textValue,
                isRequiredField = inputData.isRequired,
                onNextClicked = { onAction.invoke(UiAction.OnNextClick(inputData.id)) },
                onValueChanged = {
                    textValue = it ?: TextFieldValue()
                    onAction(UiAction.OnValueChanged(inputData.id, textValue.text))
                },
                onFocusChanged = { onAction.invoke(UiAction.OnFocusChanged(inputData.id, it)) },
                imeAction = imeAction,
                modifier = modifier,
            )
        }

        InputType.LongText -> {
            InputLongText(
                title = inputData.label,
                state = inputData.inputShellState,
                inputStyle = inputData.inputStyle,
                supportingText = inputData.supportingText,
                legendData = inputData.legendData,
                inputTextFieldValue = textValue,
                isRequiredField = inputData.isRequired,
                onNextClicked = { onAction.invoke(UiAction.OnNextClick(inputData.id)) },
                onValueChanged = {
                    textValue = it ?: TextFieldValue()
                    onAction(UiAction.OnValueChanged(inputData.id, textValue.text))
                },
                onFocusChanged = { onAction.invoke(UiAction.OnFocusChanged(inputData.id, it)) },
                imeAction = imeAction,
                modifier = modifier,
            )
        }

        InputType.MultiText -> {
            val dataMap = buildMap<String, CheckBoxData> {
                // TODO: Fetch options with paging-> paging is not available in multiplatform.
            }

            val (codeList, data) = dataMap.toList().unzip()

            InputMultiSelection(
                items = data,
                title = inputData.label,
                state = inputData.inputShellState,
                bottomSheetLowerPadding = Spacing0,
                supportingTextData = inputData.supportingText,
                legendData = inputData.legendData,
                isRequired = inputData.isRequired,
                onItemsSelected = { updatedData ->
                    val selectedCodes =
                        updatedData.filter { it.checked }.joinToString(",") { it.uid }
                    onAction(UiAction.OnValueChanged(inputData.id, selectedCodes))
                },
                modifier = modifier,
                noResultsFoundString = stringResource(Res.string.no_results_found),
                searchToFindMoreString = stringResource(Res.string.search_to_find_more),
                doneButtonText = stringResource(Res.string.action_done),
                inputStyle = inputData.inputStyle,
                onClearItemSelection = {
                    onAction(UiAction.OnValueChanged(inputData.id, null))
                },
            )
        }

        InputType.Number -> {
            InputNumber(
                title = inputData.label,
                state = inputData.inputShellState,
                inputStyle = inputData.inputStyle,
                supportingText = inputData.supportingText,
                legendData = inputData.legendData,
                inputTextFieldValue = textValue,
                isRequiredField = inputData.isRequired,
                onNextClicked = { onAction.invoke(UiAction.OnNextClick(inputData.id)) },
                onValueChanged = {
                    textValue = it ?: TextFieldValue()
                    onAction(UiAction.OnValueChanged(inputData.id, textValue.text))
                },
                onFocusChanged = { onAction.invoke(UiAction.OnFocusChanged(inputData.id, it)) },
                imeAction = imeAction,
                notation = RegExValidations.EUROPEAN_DECIMAL_NOTATION,
                modifier = modifier,
            )
        }

        InputType.OrganisationUnit -> {
            InputOrgUnit(
                title = inputData.label,
                state = inputData.inputShellState,
                inputStyle = inputData.inputStyle,
                supportingText = inputData.supportingText,
                legendData = inputData.legendData,
                inputText = inputData.displayValue,
                isRequiredField = inputData.isRequired,
                onValueChanged = {
                    onAction(UiAction.OnValueChanged(inputData.id, it))
                },
                onFocusChanged = { onAction.invoke(UiAction.OnFocusChanged(inputData.id, it)) },
                modifier = modifier,
                onOrgUnitActionCLicked = {
                    onAction(UiAction.OnOpenOrgUnitTree(inputData.id, inputData.value))
                },
            )
        }

        InputType.Percentage -> {
            InputPercentage(
                title = inputData.label,
                state = inputData.inputShellState,
                inputStyle = inputData.inputStyle,
                supportingText = inputData.supportingText,
                legendData = inputData.legendData,
                inputTextFieldValue = textValue,
                isRequiredField = inputData.isRequired,
                onNextClicked = { onAction.invoke(UiAction.OnNextClick(inputData.id)) },
                onValueChanged = {
                    textValue = it ?: TextFieldValue()
                    onAction(UiAction.OnValueChanged(inputData.id, textValue.text))
                },
                onFocusChanged = { onAction.invoke(UiAction.OnFocusChanged(inputData.id, it)) },
                imeAction = imeAction,
                modifier = modifier,
            )
        }

        is InputType.PhoneNumber -> {
            InputPhoneNumber(
                title = inputData.label,
                modifier = modifier,
                maxLength = 12,
                minLength = 4,
                state = inputData.inputShellState,
                inputStyle = inputData.inputStyle,
                legendData = inputData.legendData,
                inputTextFieldValue = textValue,
                isRequiredField = inputData.isRequired,
                onCallActionClicked = { onAction(UiAction.OnCall(inputData.id, textValue.text)) },
                onNextClicked = { onAction.invoke(UiAction.OnNextClick(inputData.id)) },
                onValueChanged = {
                    textValue = it ?: TextFieldValue()
                    onAction(UiAction.OnValueChanged(inputData.id, textValue.text))
                },
                onFocusChanged = { onAction(UiAction.OnFocusChanged(inputData.id, it)) },
                imeAction = imeAction,
                supportingText = inputData.supportingText,
                allowedCharacters = RegExValidations.PHONE_NUMBER,
            )
        }

        InputType.Text -> {
            InputText(
                title = inputData.label,
                state = inputData.inputShellState,
                supportingText = inputData.supportingText,
                legendData = inputData.legendData,
                inputTextFieldValue = textValue,
                isRequiredField = inputData.isRequired,
                onNextClicked = { onAction.invoke(UiAction.OnNextClick(inputData.id)) },
                onValueChanged = {
                    textValue = it ?: TextFieldValue()
                    onAction(UiAction.OnValueChanged(inputData.id, textValue.text))
                },
                onFocusChanged = { onAction.invoke(UiAction.OnFocusChanged(inputData.id, it)) },
                imeAction = imeAction,
                modifier = modifier,
                inputStyle = inputData.inputStyle,
            )
        }

        InputType.TrueOnly -> {
            InputYesOnlyCheckBox(
                checkBoxData = CheckBoxData(
                    uid = inputData.id,
                    checked = inputData.value?.toBoolean() ?: false,
                    enabled = true,
                    textInput = inputData.label,
                ),
                modifier = modifier,
                state = inputData.inputShellState,
                inputStyle = inputData.inputStyle,
                supportingText = inputData.supportingText,
                legendData = inputData.legendData,
                isRequired = inputData.isRequired,
                onClick = {
                    onAction(UiAction.OnValueChanged(inputData.id, it.takeIf { it }?.toString()))
                },
            )
        }

        InputType.UnitInterval -> {
            InputUnitInterval(
                title = inputData.label,
                state = inputData.inputShellState,
                inputStyle = inputData.inputStyle,
                supportingText = inputData.supportingText,
                legendData = inputData.legendData,
                inputTextFieldValue = textValue,
                isRequiredField = inputData.isRequired,
                onNextClicked = { onAction.invoke(UiAction.OnNextClick(inputData.id)) },
                onValueChanged = {
                    textValue = it ?: TextFieldValue()
                    onAction(UiAction.OnValueChanged(inputData.id, textValue.text))
                },
                imeAction = imeAction,
                modifier = modifier,
            )
        }

        InputType.Url -> {
            InputLink(
                title = inputData.label,
                state = inputData.inputShellState,
                inputStyle = inputData.inputStyle,
                supportingText = inputData.supportingText,
                legendData = inputData.legendData,
                inputTextFieldValue = textValue,
                isRequiredField = inputData.isRequired,
                onNextClicked = { onAction.invoke(UiAction.OnNextClick(inputData.id)) },
                onValueChanged = {
                    textValue = it ?: TextFieldValue()
                    onAction(UiAction.OnValueChanged(inputData.id, textValue.text))
                },
                onFocusChanged = { onAction.invoke(UiAction.OnFocusChanged(inputData.id, it)) },
                imeAction = imeAction,
                modifier = modifier,
                onLinkActionCLicked = { UiAction.OnLinkClicked(inputData.id, textValue.text) },
            )
        }

        InputType.Username,
        InputType.TrackerAssociate,
        InputType.Reference,
        InputType.GeoJson,
        -> {
            InputNotSupported(
                title = inputData.label,
                notSupportedString = stringResource(Res.string.input_not_supported),
                inputStyle = inputData.inputStyle,
            )
        }
    }
}
