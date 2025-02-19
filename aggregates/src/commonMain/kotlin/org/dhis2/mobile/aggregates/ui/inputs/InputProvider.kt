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
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
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

    val inputState by remember {
        mutableStateOf(InputShellState.UNFOCUSED) // TODO: this should be internal?
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
                        dateOfBirthLabel = "Date of birth",
                        orLabel = "or", // TODO: Seriously?? XD
                        ageLabel = "Age",
                        acceptText = "Accept",
                        cancelText = "Cancel",
                        is24hourFormat = true, // TODO: How is this configured in the app?
                        selectableDates = inputData.ageExtras().selectableDates,
                    ),
                    inputType = inputData.value?.let { AgeInputType.None }
                        ?: AgeInputType.DateOfBirth(textValue),
                    inputState = inputState,
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
                onNextClicked = { onAction.invoke(UiAction.OnNextClick) },
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
                latitudeText = "latitude",
                longitudeText = "longitude",
                addLocationBtnText = "add location",
                isRequired = inputData.isRequired,
                modifier = modifier,
                onResetButtonClicked = {
                    onAction(UiAction.OnValueChanged(inputData.id, null))
                },
                onUpdateButtonClicked = {
                    onAction(UiAction.OnCaptureCoordinates(inputData.id))
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
                        acceptText = "Accept",
                        cancelText = "Cancel",
                        outOfRangeText = "out of range",
                        incorrectHourFormatText = "incorrect format",
                        selectableDates = inputData.dateExtras().selectableDates,
                        yearRange = inputData.dateExtras().yearRange, // TODO: Is it really needed if we have a selectableDates?
                    ),
                    inputTextFieldValue = textValue,
                    inputState = inputState,
                    legendData = inputData.legendData,
                    supportingText = inputData.supportingText,
                ),
                onFocusChanged = { onAction.invoke(UiAction.OnFocusChanged(it)) },
                onValueChanged = {
                    textValue = it ?: TextFieldValue()
                    onAction(UiAction.OnValueChanged(inputData.id, textValue.text))
                },
                onNextClicked = { onAction(UiAction.OnNextClick) },
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
                onNextClicked = { onAction.invoke(UiAction.OnNextClick) },
                onValueChanged = {
                    textValue = it ?: TextFieldValue()
                    onAction(UiAction.OnValueChanged(inputData.id, textValue.text))
                },
                onFocusChanged = { onAction(UiAction.OnFocusChanged(it)) },
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
            var uploadingState by remember(inputData.value) { // TODO: This should be internal
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
                    uploadingState = UploadFileState.UPLOADING // TODO: This should be internal
                    onAction(UiAction.OnSelectFile(inputData.id))
                },
                onUploadFile = {
                    uploadingState = UploadFileState.UPLOADING // TODO: This should be internal
                    onAction(UiAction.OnOpenFile(inputData.id))
                },
                onClear = {
                    uploadingState = UploadFileState.UPLOADING // TODO: This should be internal
                    onAction(UiAction.OnValueChanged(inputData.id, null))
                },
                uploadFileState = uploadingState,
                inputShellState = inputState,
                inputStyle = inputData.inputStyle,
                supportingText = inputData.supportingText,
                legendData = inputData.legendData,
                isRequired = inputData.isRequired,
                modifier = modifier,
            )
        }

        InputType.Image -> {
            var uploadingState by remember(inputData.value) { // TODO: This should be internal and shared with file
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
                onNextClicked = { onAction.invoke(UiAction.OnNextClick) },
                onValueChanged = {
                    textValue = it ?: TextFieldValue()
                    onAction(UiAction.OnValueChanged(inputData.id, textValue.text))
                },
                onFocusChanged = { onAction.invoke(UiAction.OnFocusChanged(it)) },
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
                onNextClicked = { onAction.invoke(UiAction.OnNextClick) },
                onValueChanged = {
                    textValue = it ?: TextFieldValue()
                    onAction(UiAction.OnValueChanged(inputData.id, textValue.text))
                },
                onFocusChanged = { onAction.invoke(UiAction.OnFocusChanged(it)) },
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
                onNextClicked = { onAction.invoke(UiAction.OnNextClick) },
                onValueChanged = {
                    textValue = it ?: TextFieldValue()
                    onAction(UiAction.OnValueChanged(inputData.id, textValue.text))
                },
                onFocusChanged = { onAction.invoke(UiAction.OnFocusChanged(it)) },
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
                onNextClicked = { onAction.invoke(UiAction.OnNextClick) },
                onValueChanged = {
                    textValue = it ?: TextFieldValue()
                    onAction(UiAction.OnValueChanged(inputData.id, textValue.text))
                },
                onFocusChanged = { onAction.invoke(UiAction.OnFocusChanged(it)) },
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
                onNextClicked = { onAction.invoke(UiAction.OnNextClick) },
                onValueChanged = {
                    textValue = it ?: TextFieldValue()
                    onAction(UiAction.OnValueChanged(inputData.id, textValue.text))
                },
                onFocusChanged = { onAction.invoke(UiAction.OnFocusChanged(it)) },
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
                onNextClicked = { onAction.invoke(UiAction.OnNextClick) },
                onValueChanged = {
                    textValue = it ?: TextFieldValue()
                    onAction(UiAction.OnValueChanged(inputData.id, textValue.text))
                },
                onFocusChanged = { onAction.invoke(UiAction.OnFocusChanged(it)) },
                imeAction = imeAction,
                modifier = modifier,
            )
        }

        InputType.MultiText -> {
            val dataMap = buildMap<String, CheckBoxData> {
                /*fieldUiModel.optionSetConfiguration?.optionFlow?.collectAsLazyPagingItems()?.let { paging ->
                    repeat(paging.itemCount) { index ->
                        val optionData = paging[index]
                        put(
                            optionData?.option?.code() ?: "",
                            CheckBoxData(
                                uid = optionData?.option?.code() ?: "",
                                checked = optionData?.option?.code()?.let { inputData.value?.split(",")?.contains(it) } ?: false,
                                enabled = true,
                                textInput = optionData?.option?.displayName() ?: "",
                            ),
                        )
                    }
                }*/
            }

            val (codeList, data) = dataMap.toList().unzip()

            InputMultiSelection(
                items = data,
                title = inputData.label,
                state = inputData.inputShellState,
                windowInsets = { TODO() }, // TODO: This could be moved to a LocalComposition and provided by the theme
                bottomSheetLowerPadding = Spacing0, // TODO: This could be moved to a LocalComposition and provided by the theme
                supportingTextData = inputData.supportingText,
                legendData = inputData.legendData,
                isRequired = inputData.isRequired,
                onItemsSelected = { updatedData ->
                    val selectedCodes =
                        updatedData.filter { it.checked }.joinToString(",") { it.uid }
                    onAction(UiAction.OnValueChanged(inputData.id, selectedCodes))
                },
                modifier = modifier,
                noResultsFoundString = "no result found",
                searchToFindMoreString = "search to find more",
                doneButtonText = "done",
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
                onNextClicked = { onAction.invoke(UiAction.OnNextClick) },
                onValueChanged = {
                    textValue = it ?: TextFieldValue()
                    onAction(UiAction.OnValueChanged(inputData.id, textValue.text))
                },
                onFocusChanged = { onAction.invoke(UiAction.OnFocusChanged(it)) },
                imeAction = imeAction,
                notation = RegExValidations.EUROPEAN_DECIMAL_NOTATION, // TODO: Harmonize with PhoneNumber. This could be a DHIS2Theme local configuration
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
                inputText = inputData.value,
                isRequiredField = inputData.isRequired,
                onValueChanged = {
                    onAction(UiAction.OnValueChanged(inputData.id, it))
                },
                onFocusChanged = { onAction.invoke(UiAction.OnFocusChanged(it)) },
                modifier = modifier,
                onOrgUnitActionCLicked = {
                    TODO()
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
                onNextClicked = { onAction.invoke(UiAction.OnNextClick) },
                onValueChanged = {
                    textValue = it ?: TextFieldValue()
                    onAction(UiAction.OnValueChanged(inputData.id, textValue.text))
                },
                onFocusChanged = { onAction.invoke(UiAction.OnFocusChanged(it)) },
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
                onNextClicked = { onAction.invoke(UiAction.OnNextClick) },
                onValueChanged = {
                    textValue = it ?: TextFieldValue()
                    onAction(UiAction.OnValueChanged(inputData.id, textValue.text))
                },
                onFocusChanged = { onAction(UiAction.OnFocusChanged(it)) },
                imeAction = imeAction,
                supportingText = inputData.supportingText,
                allowedCharacters = RegExValidations.PHONE_NUMBER, // TODO: Harmonize with Number notation. Why to expose it?
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
                onNextClicked = { onAction.invoke(UiAction.OnNextClick) },
                onValueChanged = {
                    textValue = it ?: TextFieldValue()
                    onAction(UiAction.OnValueChanged(inputData.id, textValue.text))
                },
                onFocusChanged = { onAction.invoke(UiAction.OnFocusChanged(it)) },
                imeAction = imeAction,
                modifier = modifier,
                inputStyle = inputData.inputStyle,
            )
        }

        InputType.TruOnly -> {
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
                onNextClicked = { onAction.invoke(UiAction.OnNextClick) },
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
                onNextClicked = { onAction.invoke(UiAction.OnNextClick) },
                onValueChanged = {
                    textValue = it ?: TextFieldValue()
                    onAction(UiAction.OnValueChanged(inputData.id, textValue.text))
                },
                onFocusChanged = { onAction.invoke(UiAction.OnFocusChanged(it)) },
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
                notSupportedString = "Not supported",
                inputStyle = inputData.inputStyle,
            )
        }
    }
}
