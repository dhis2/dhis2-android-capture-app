package org.dhis2.tracker.ui.input.provider

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import org.dhis2.tracker.ui.input.model.TrackerInputModel
import org.dhis2.tracker.ui.input.model.TrackerInputType
import org.dhis2.tracker.ui.input.model.TrackerInputUiEvent
import org.dhis2.tracker.ui.input.model.inputState
import org.dhis2.tracker.ui.input.model.supportingText
import org.hisp.dhis.mobile.ui.designsystem.component.CheckBoxData
import org.hisp.dhis.mobile.ui.designsystem.component.DropdownItem
import org.hisp.dhis.mobile.ui.designsystem.component.InputBarCode
import org.hisp.dhis.mobile.ui.designsystem.component.InputDropDown
import org.hisp.dhis.mobile.ui.designsystem.component.InputEmail
import org.hisp.dhis.mobile.ui.designsystem.component.InputInteger
import org.hisp.dhis.mobile.ui.designsystem.component.InputLetter
import org.hisp.dhis.mobile.ui.designsystem.component.InputLink
import org.hisp.dhis.mobile.ui.designsystem.component.InputLongText
import org.hisp.dhis.mobile.ui.designsystem.component.InputNegativeInteger
import org.hisp.dhis.mobile.ui.designsystem.component.InputNotSupported
import org.hisp.dhis.mobile.ui.designsystem.component.InputNumber
import org.hisp.dhis.mobile.ui.designsystem.component.InputOrgUnit
import org.hisp.dhis.mobile.ui.designsystem.component.InputPercentage
import org.hisp.dhis.mobile.ui.designsystem.component.InputPhoneNumber
import org.hisp.dhis.mobile.ui.designsystem.component.InputPositiveInteger
import org.hisp.dhis.mobile.ui.designsystem.component.InputPositiveIntegerOrZero
import org.hisp.dhis.mobile.ui.designsystem.component.InputQRCode
import org.hisp.dhis.mobile.ui.designsystem.component.InputStyle
import org.hisp.dhis.mobile.ui.designsystem.component.InputText
import org.hisp.dhis.mobile.ui.designsystem.component.InputUnitInterval
import org.hisp.dhis.mobile.ui.designsystem.component.InputYesOnlyCheckBox
import org.hisp.dhis.mobile.ui.designsystem.component.InputYesOnlySwitch
import org.hisp.dhis.mobile.ui.designsystem.resource.provideDHIS2Icon
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor

@Composable
fun ParameterInputProvider(
    modifier: Modifier = Modifier,
    inputModel: TrackerInputModel,
    inputStyle: InputStyle,
    onNextClicked: () -> Unit,
    onUiEvent: (TrackerInputUiEvent) -> Unit,
) {
    var textValue by remember(inputModel.uid) {
        mutableStateOf(
            TextFieldValue(
                text = inputModel.value ?: "",
                selection = TextRange(inputModel.value?.length ?: 0),
            ),
        )
    }

    val focusRequester = remember { FocusRequester() }
    val modifierWithFocus = modifier.focusRequester(focusRequester)

    LaunchedEffect(inputModel.focused) {
        if (inputModel.focused) {
            focusRequester.requestFocus()
        }
    }

    val onImeActionClick: (ImeAction) -> Unit = { imeAction ->
        if (imeAction == ImeAction.Next) {
            onNextClicked()
        }
    }

    when (inputModel.valueType) {
        TrackerInputType.TEXT -> {
            InputText(
                modifier = modifierWithFocus.fillMaxWidth(),
                title = inputModel.label,
                state = inputModel.inputState(),
                supportingText = inputModel.supportingText(),
                legendData = inputModel.legend,
                isRequiredField = inputModel.mandatory,
                inputStyle = inputStyle,
                inputTextFieldValue = textValue,
                onValueChanged = { newValue ->
                    newValue?.let {
                        if (it.text != inputModel.value) {
                            inputModel.onValueChange(it.text.takeIf { text -> text.isNotEmpty() })
                        }
                    }
                },
                imeAction = ImeAction.Next,
                onImeActionClick = onImeActionClick,
            )
        }

        TrackerInputType.LONG_TEXT -> {
            InputLongText(
                modifier = modifierWithFocus.fillMaxWidth(),
                title = inputModel.label,
                state = inputModel.inputState(),
                supportingText = inputModel.supportingText(),
                legendData = inputModel.legend,
                isRequiredField = inputModel.mandatory,
                inputStyle = inputStyle,
                inputTextFieldValue = textValue,
                onValueChanged = { newValue ->
                    newValue?.let {
                        if (it.text != inputModel.value) {
                            inputModel.onValueChange(it.text.takeIf { text -> text.isNotEmpty() })
                        }
                    }
                },
                imeAction = ImeAction.Next,
                onImeActionClick = onImeActionClick,
            )
        }

        TrackerInputType.LETTER -> {
            InputLetter(
                modifier = modifierWithFocus.fillMaxWidth(),
                title = inputModel.label,
                state = inputModel.inputState(),
                supportingText = inputModel.supportingText(),
                legendData = inputModel.legend,
                isRequiredField = inputModel.mandatory,
                inputStyle = inputStyle,
                inputTextFieldValue = textValue,
                onValueChanged = { newValue ->
                    newValue?.let {
                        if (it.text != inputModel.value) {
                            inputModel.onValueChange(it.text.takeIf { text -> text.isNotEmpty() })
                        }
                    }
                },
                imeAction = ImeAction.Next,
                onImeActionClick = onImeActionClick,
            )
        }

        TrackerInputType.EMAIL -> {
            InputEmail(
                modifier = modifierWithFocus.fillMaxWidth(),
                title = inputModel.label,
                state = inputModel.inputState(),
                supportingText = inputModel.supportingText(),
                legendData = inputModel.legend,
                isRequiredField = inputModel.mandatory,
                inputStyle = inputStyle,
                inputTextFieldValue = textValue,
                onEmailActionCLicked = {},
                onValueChanged = { newValue ->
                    newValue?.let {
                        if (it.text != inputModel.value) {
                            inputModel.onValueChange(it.text.takeIf { text -> text.isNotEmpty() })
                        }
                    }
                },
                imeAction = ImeAction.Next,
                onImeActionClick = onImeActionClick,
            )
        }

        TrackerInputType.PHONE_NUMBER -> {
            InputPhoneNumber(
                modifier = modifierWithFocus.fillMaxWidth(),
                title = inputModel.label,
                state = inputModel.inputState(),
                supportingText = inputModel.supportingText(),
                legendData = inputModel.legend,
                isRequiredField = inputModel.mandatory,
                inputStyle = inputStyle,
                inputTextFieldValue = textValue,
                onCallActionClicked = {},
                onValueChanged = { newValue ->
                    newValue?.let {
                        if (it.text != inputModel.value) {
                            inputModel.onValueChange(it.text.takeIf { text -> text.isNotEmpty() })
                        }
                    }
                },
                imeAction = ImeAction.Next,
                onImeActionClick = onImeActionClick,
            )
        }

        TrackerInputType.URL -> {
            InputLink(
                modifier = modifierWithFocus.fillMaxWidth(),
                title = inputModel.label,
                state = inputModel.inputState(),
                supportingText = inputModel.supportingText(),
                legendData = inputModel.legend,
                isRequiredField = inputModel.mandatory,
                inputStyle = inputStyle,
                inputTextFieldValue = textValue,
                onLinkActionCLicked = {},
                onValueChanged = { newValue ->
                    newValue?.let {
                        if (it.text != inputModel.value) {
                            inputModel.onValueChange(it.text.takeIf { text -> text.isNotEmpty() })
                        }
                    }
                },
                imeAction = ImeAction.Next,
                onImeActionClick = onImeActionClick,
            )
        }

        TrackerInputType.NUMBER -> {
            InputNumber(
                modifier = modifierWithFocus.fillMaxWidth(),
                title = inputModel.label,
                state = inputModel.inputState(),
                supportingText = inputModel.supportingText(),
                legendData = inputModel.legend,
                isRequiredField = inputModel.mandatory,
                inputStyle = inputStyle,
                inputTextFieldValue = textValue,
                onValueChanged = { newValue ->
                    newValue?.let {
                        if (it.text != inputModel.value) {
                            inputModel.onValueChange(it.text.takeIf { text -> text.isNotEmpty() })
                        }
                    }
                },
                imeAction = ImeAction.Next,
                onImeActionClick = onImeActionClick,
            )
        }

        TrackerInputType.INTEGER -> {
            InputInteger(
                modifier = modifierWithFocus.fillMaxWidth(),
                title = inputModel.label,
                state = inputModel.inputState(),
                supportingText = inputModel.supportingText(),
                legendData = inputModel.legend,
                isRequiredField = inputModel.mandatory,
                inputStyle = inputStyle,
                inputTextFieldValue = textValue,
                onValueChanged = { newValue ->
                    newValue?.let {
                        if (it.text != inputModel.value) {
                            inputModel.onValueChange(it.text.takeIf { text -> text.isNotEmpty() })
                        }
                    }
                },
                imeAction = ImeAction.Next,
                onImeActionClick = onImeActionClick,
            )
        }

        TrackerInputType.INTEGER_POSITIVE -> {
            InputPositiveInteger(
                modifier = modifierWithFocus.fillMaxWidth(),
                title = inputModel.label,
                state = inputModel.inputState(),
                supportingText = inputModel.supportingText(),
                legendData = inputModel.legend,
                isRequiredField = inputModel.mandatory,
                inputStyle = inputStyle,
                inputTextFieldValue = textValue,
                onValueChanged = { newValue ->
                    newValue?.let {
                        if (it.text != inputModel.value) {
                            inputModel.onValueChange(it.text.takeIf { text -> text.isNotEmpty() })
                        }
                    }
                },
                imeAction = ImeAction.Next,
                onImeActionClick = onImeActionClick,
            )
        }

        TrackerInputType.INTEGER_NEGATIVE -> {
            InputNegativeInteger(
                modifier = modifierWithFocus.fillMaxWidth(),
                title = inputModel.label,
                state = inputModel.inputState(),
                supportingText = inputModel.supportingText(),
                legendData = inputModel.legend,
                isRequiredField = inputModel.mandatory,
                inputStyle = inputStyle,
                inputTextFieldValue = textValue,
                onValueChanged = { newValue ->
                    newValue?.let {
                        if (it.text != inputModel.value) {
                            inputModel.onValueChange(it.text.takeIf { text -> text.isNotEmpty() })
                        }
                    }
                },
                imeAction = ImeAction.Next,
                onImeActionClick = onImeActionClick,
            )
        }

        TrackerInputType.INTEGER_ZERO_OR_POSITIVE -> {
            InputPositiveIntegerOrZero(
                modifier = modifierWithFocus.fillMaxWidth(),
                title = inputModel.label,
                state = inputModel.inputState(),
                supportingText = inputModel.supportingText(),
                legendData = inputModel.legend,
                isRequiredField = inputModel.mandatory,
                inputStyle = inputStyle,
                inputTextFieldValue = textValue,
                onValueChanged = { newValue ->
                    newValue?.let {
                        if (it.text != inputModel.value) {
                            inputModel.onValueChange(it.text.takeIf { text -> text.isNotEmpty() })
                        }
                    }
                },
                imeAction = ImeAction.Next,
                onImeActionClick = onImeActionClick,
            )
        }

        TrackerInputType.PERCENTAGE -> {
            InputPercentage(
                modifier = modifierWithFocus.fillMaxWidth(),
                title = inputModel.label,
                state = inputModel.inputState(),
                supportingText = inputModel.supportingText(),
                legendData = inputModel.legend,
                isRequiredField = inputModel.mandatory,
                inputStyle = inputStyle,
                inputTextFieldValue = textValue,
                onValueChanged = { newValue ->
                    newValue?.let {
                        if (it.text != inputModel.value) {
                            inputModel.onValueChange(it.text.takeIf { text -> text.isNotEmpty() })
                        }
                    }
                },
                imeAction = ImeAction.Next,
                onImeActionClick = onImeActionClick,
            )
        }

        TrackerInputType.UNIT_INTERVAL -> {
            InputUnitInterval(
                modifier = modifierWithFocus.fillMaxWidth(),
                title = inputModel.label,
                state = inputModel.inputState(),
                supportingText = inputModel.supportingText(),
                legendData = inputModel.legend,
                isRequiredField = inputModel.mandatory,
                inputStyle = inputStyle,
                inputTextFieldValue = textValue,
                onValueChanged = { newValue ->
                    newValue?.let {
                        if (it.text != inputModel.value) {
                            inputModel.onValueChange(it.text.takeIf { text -> text.isNotEmpty() })
                        }
                    }
                },
                imeAction = ImeAction.Next,
                onImeActionClick = onImeActionClick,
            )
        }

        TrackerInputType.AGE -> {
            ProvideTrackerAgeInput(
                model = inputModel,
                inputStyle = inputStyle,
                onNextClicked = onNextClicked,
                modifier = modifierWithFocus,
            )
        }

        TrackerInputType.DATE_TIME, TrackerInputType.DATE, TrackerInputType.TIME -> {
            ProvideTrackerDateTimeInput(
                model = inputModel,
                inputStyle = inputStyle,
                onNextClicked = onNextClicked,
                modifier = modifierWithFocus,
            )
        }

        TrackerInputType.ORGANISATION_UNIT -> {
            InputOrgUnit(
                modifier = modifierWithFocus.fillMaxWidth(),
                title = inputModel.label,
                state = inputModel.inputState(),
                supportingText = inputModel.supportingText(),
                legendData = inputModel.legend,
                isRequiredField = inputModel.mandatory,
                inputStyle = inputStyle,
                inputText = inputModel.value,
                onValueChanged = { newValue ->
                    inputModel.onValueChange(newValue)
                },
                onOrgUnitActionCLicked = {
                    onUiEvent(
                        TrackerInputUiEvent.OnOrgUnitButtonClicked(
                            uid = inputModel.uid,
                            label = inputModel.label,
                            value = inputModel.value,
                        ),
                    )
                },
            )
        }

        TrackerInputType.MULTI_SELECTION -> {
            TODO()
        }

        TrackerInputType.QR_CODE -> {
            InputQRCode(
                modifier = modifierWithFocus.fillMaxWidth(),
                title = inputModel.label,
                state = inputModel.inputState(),
                supportingText = inputModel.supportingText(),
                legendData = inputModel.legend,
                isRequiredField = inputModel.mandatory,
                inputStyle = inputStyle,
                inputTextFieldValue = textValue,
                onQRButtonClicked = {
                    onUiEvent(
                        TrackerInputUiEvent.OnQRButtonClicked(
                            uid = inputModel.uid,
                        ),
                    )
                },
                onValueChanged = { newValue ->
                    newValue?.let {
                        if (it.text != inputModel.value) {
                            inputModel.onValueChange(it.text.takeIf { text -> text.isNotEmpty() })
                        }
                    }
                },
                imeAction = ImeAction.Next,
                onImeActionClick = onImeActionClick,
            )
        }

        TrackerInputType.BAR_CODE -> {
            InputBarCode(
                modifier = modifierWithFocus.fillMaxWidth(),
                title = inputModel.label,
                state = inputModel.inputState(),
                supportingText = inputModel.supportingText(),
                legendData = inputModel.legend,
                isRequiredField = inputModel.mandatory,
                inputStyle = inputStyle,
                inputTextFieldValue = textValue,
                onActionButtonClicked = {
                    onUiEvent(
                        TrackerInputUiEvent.OnBarcodeButtonClicked(
                            uid = inputModel.uid,
                        ),
                    )
                },
                onValueChanged = { newValue ->
                    newValue?.let {
                        if (it.text != inputModel.value) {
                            inputModel.onValueChange(it.text.takeIf { text -> text.isNotEmpty() })
                        }
                    }
                },
                imeAction = ImeAction.Next,
                onImeActionClick = onImeActionClick,
            )
        }

        TrackerInputType.CHECKBOX -> {
            TODO()
        }

        TrackerInputType.RADIO_BUTTON -> {
            TODO()
        }

        TrackerInputType.YES_ONLY_SWITCH -> {
            InputYesOnlySwitch(
                modifier = modifierWithFocus.fillMaxWidth(),
                title = inputModel.label,
                state = inputModel.inputState(),
                supportingText = inputModel.supportingText(),
                legendData = inputModel.legend,
                isRequired = inputModel.mandatory,
                inputStyle = inputStyle,
                isChecked = inputModel.value?.toBooleanStrictOrNull() == true,
                onClick = { isChecked ->
                    inputModel.onValueChange(if (isChecked) "true" else null)
                },
            )
        }

        TrackerInputType.YES_ONLY_CHECKBOX -> {
            InputYesOnlyCheckBox(
                modifier = modifierWithFocus.fillMaxWidth(),
                checkBoxData =
                    CheckBoxData(
                        uid = inputModel.uid,
                        checked = inputModel.value?.toBooleanStrictOrNull() == true,
                        enabled = inputModel.editable,
                        textInput = inputModel.label,
                    ),
                state = inputModel.inputState(),
                supportingText = inputModel.supportingText(),
                legendData = inputModel.legend,
                isRequired = inputModel.mandatory,
                inputStyle = inputStyle,
                onClick = { isChecked ->
                    inputModel.onValueChange(if (isChecked) "true" else null)
                },
            )
        }

        TrackerInputType.DROPDOWN -> {
            val optionSetConfiguration = inputModel.optionSetConfiguration!!

            var selectedItem by remember(inputModel.uid, inputModel.value) {
                val displayName =
                    optionSetConfiguration.options
                        .find {
                            it.code == inputModel.value
                        }?.displayName
                mutableStateOf(displayName ?: inputModel.value ?: "")
            }

            val options = optionSetConfiguration.options
            val useDropdown by remember {
                derivedStateOf { options.size < 15 }
            }

            InputDropDown(
                modifier = modifierWithFocus.fillMaxWidth(),
                inputStyle = inputStyle,
                title = inputModel.label,
                state = inputModel.inputState(),
                selectedItem = DropdownItem(selectedItem),
                supportingTextData = inputModel.supportingText(),
                legendData = inputModel.legend,
                isRequiredField = inputModel.mandatory,
                onResetButtonClicked = {
                    selectedItem = ""
                    inputModel.onValueChange(null)
                },
                fetchItem = { index ->
                    DropdownItem(options.getOrNull(index)?.displayName ?: "")
                },
                onSearchOption = { query ->
                    optionSetConfiguration.onSearch?.invoke(query)
                },
                itemCount = options.size,
                useDropDown = useDropdown,
                onItemSelected = { index, newSelectedItem ->
                    selectedItem = newSelectedItem.label
                    inputModel.onValueChange(options.getOrNull(index)?.code)
                },
                loadOptions = {
                    optionSetConfiguration.onLoadOptions?.invoke()
                },
                onDismiss = {
                    optionSetConfiguration.onSearch?.invoke("")
                },
            )
        }

        TrackerInputType.PERIOD_SELECTOR,
        TrackerInputType.MATRIX,
        TrackerInputType.SEQUENTIAL,
        TrackerInputType.NOT_SUPPORTED,
        -> {
            InputNotSupported(
                modifier = modifierWithFocus.fillMaxWidth(),
                title = inputModel.label,
                inputStyle = inputStyle,
            )
        }
    }
}

@Composable
fun ProvideParameterIcon(valueType: TrackerInputType?) =
    when (valueType) {
        TrackerInputType.QR_CODE ->
            Icon(
                imageVector = Icons.Outlined.QrCode2,
                contentDescription = "QR Code Icon",
                tint = SurfaceColor.Primary,
            )

        TrackerInputType.BAR_CODE ->
            Icon(
                painter = provideDHIS2Icon("material_barcode_scanner"),
                contentDescription = "Barcode Icon",
                tint = SurfaceColor.Primary,
            )

        else ->
            Icon(
                imageVector = Icons.Outlined.AddCircleOutline,
                contentDescription = "Add Icon",
                tint = SurfaceColor.Primary,
            )
    }
