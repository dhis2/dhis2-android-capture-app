package org.dhis2.tracker.input.ui.provider

import androidx.compose.foundation.layout.fillMaxWidth
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
import org.dhis2.mobile.commons.orgunit.OrgUnitSelectorScope
import org.dhis2.mobile.tracker.resources.Res
import org.dhis2.mobile.tracker.resources.custom_intent_launch
import org.dhis2.tracker.input.model.TrackerInputType
import org.dhis2.tracker.input.ui.action.TrackerInputUiEvent
import org.dhis2.tracker.input.ui.state.TrackerInputUiState
import org.dhis2.tracker.input.ui.state.inputState
import org.dhis2.tracker.input.ui.state.supportingText
import org.hisp.dhis.mobile.ui.designsystem.component.CheckBoxData
import org.hisp.dhis.mobile.ui.designsystem.component.CustomIntentState
import org.hisp.dhis.mobile.ui.designsystem.component.DropdownItem
import org.hisp.dhis.mobile.ui.designsystem.component.InputBarCode
import org.hisp.dhis.mobile.ui.designsystem.component.InputCustomIntent
import org.hisp.dhis.mobile.ui.designsystem.component.InputDropDown
import org.hisp.dhis.mobile.ui.designsystem.component.InputEmail
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
import org.hisp.dhis.mobile.ui.designsystem.component.InputQRCode
import org.hisp.dhis.mobile.ui.designsystem.component.InputStyle
import org.hisp.dhis.mobile.ui.designsystem.component.InputText
import org.hisp.dhis.mobile.ui.designsystem.component.InputUnitInterval
import org.hisp.dhis.mobile.ui.designsystem.component.InputYesOnlyCheckBox
import org.hisp.dhis.mobile.ui.designsystem.component.InputYesOnlySwitch
import org.jetbrains.compose.resources.stringResource

@Composable
fun TrackerInputProvider(
    modifier: Modifier = Modifier,
    inputModel: TrackerInputUiState,
    inputStyle: InputStyle,
    onNextClicked: () -> Unit,
    onUiEvent: (TrackerInputUiEvent) -> Unit,
) {
    var textValue by remember(inputModel.uid, inputModel.value) {
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
                    manageOnValueChange(
                        newValue = newValue?.text,
                        inputModel = inputModel,
                        onUiEvent = onUiEvent,
                    )
                },
                imeAction = ImeAction.Next,
                onImeActionClick = onImeActionClick,
                onFocusChanged = { isFocused ->
                    handleOnFocusChange(
                        isFocused = isFocused,
                        inputModelId = inputModel.uid,
                        onUiEvent = onUiEvent,
                    )
                },
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
                    manageOnValueChange(
                        newValue = newValue?.text,
                        inputModel = inputModel,
                        onUiEvent = onUiEvent,
                    )
                },
                imeAction = ImeAction.Next,
                onImeActionClick = onImeActionClick,
                onFocusChanged = { isFocused ->
                    handleOnFocusChange(
                        isFocused = isFocused,
                        inputModelId = inputModel.uid,
                        onUiEvent = onUiEvent,
                    )
                },
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
                    manageOnValueChange(
                        newValue = newValue?.text,
                        inputModel = inputModel,
                        onUiEvent = onUiEvent,
                    )
                },
                imeAction = ImeAction.Next,
                onImeActionClick = onImeActionClick,
                onFocusChanged = { isFocused ->
                    handleOnFocusChange(
                        isFocused = isFocused,
                        inputModelId = inputModel.uid,
                        onUiEvent = onUiEvent,
                    )
                },
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
                    manageOnValueChange(
                        newValue = newValue?.text,
                        inputModel = inputModel,
                        onUiEvent = onUiEvent,
                    )
                },
                imeAction = ImeAction.Next,
                onImeActionClick = onImeActionClick,
                onFocusChanged = { isFocused ->
                    handleOnFocusChange(
                        isFocused = isFocused,
                        inputModelId = inputModel.uid,
                        onUiEvent = onUiEvent,
                    )
                },
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
                    manageOnValueChange(
                        newValue = newValue?.text,
                        inputModel = inputModel,
                        onUiEvent = onUiEvent,
                    )
                },
                imeAction = ImeAction.Next,
                onImeActionClick = onImeActionClick,
                onFocusChanged = { isFocused ->
                    handleOnFocusChange(
                        isFocused = isFocused,
                        inputModelId = inputModel.uid,
                        onUiEvent = onUiEvent,
                    )
                },
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
                    manageOnValueChange(
                        newValue = newValue?.text,
                        inputModel = inputModel,
                        onUiEvent = onUiEvent,
                    )
                },
                imeAction = ImeAction.Next,
                onImeActionClick = onImeActionClick,
                onFocusChanged = { isFocused ->
                    handleOnFocusChange(
                        isFocused = isFocused,
                        inputModelId = inputModel.uid,
                        onUiEvent = onUiEvent,
                    )
                },
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
                    manageOnValueChange(
                        newValue = newValue?.text,
                        inputModel = inputModel,
                        onUiEvent = onUiEvent,
                    )
                },
                imeAction = ImeAction.Next,
                onImeActionClick = onImeActionClick,
                onFocusChanged = { isFocused ->
                    handleOnFocusChange(
                        isFocused = isFocused,
                        inputModelId = inputModel.uid,
                        onUiEvent = onUiEvent,
                    )
                },
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
                    manageOnValueChange(
                        newValue = newValue?.text,
                        inputModel = inputModel,
                        onUiEvent = onUiEvent,
                    )
                },
                imeAction = ImeAction.Next,
                onImeActionClick = onImeActionClick,
                onFocusChanged = { isFocused ->
                    handleOnFocusChange(
                        isFocused = isFocused,
                        inputModelId = inputModel.uid,
                        onUiEvent = onUiEvent,
                    )
                },
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
                    manageOnValueChange(
                        newValue = newValue?.text,
                        inputModel = inputModel,
                        onUiEvent = onUiEvent,
                    )
                },
                imeAction = ImeAction.Next,
                onImeActionClick = onImeActionClick,
                onFocusChanged = { isFocused ->
                    handleOnFocusChange(
                        isFocused = isFocused,
                        inputModelId = inputModel.uid,
                        onUiEvent = onUiEvent,
                    )
                },
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
                    manageOnValueChange(
                        newValue = newValue?.text,
                        inputModel = inputModel,
                        onUiEvent = onUiEvent,
                    )
                },
                imeAction = ImeAction.Next,
                onImeActionClick = onImeActionClick,
                onFocusChanged = { isFocused ->
                    handleOnFocusChange(
                        isFocused = isFocused,
                        inputModelId = inputModel.uid,
                        onUiEvent = onUiEvent,
                    )
                },
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
                    manageOnValueChange(
                        newValue = newValue?.text,
                        inputModel = inputModel,
                        onUiEvent = onUiEvent,
                    )
                },
                imeAction = ImeAction.Next,
                onImeActionClick = onImeActionClick,
                onFocusChanged = { isFocused ->
                    handleOnFocusChange(
                        isFocused = isFocused,
                        inputModelId = inputModel.uid,
                        onUiEvent = onUiEvent,
                    )
                },
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
                    manageOnValueChange(
                        newValue = newValue?.text,
                        inputModel = inputModel,
                        onUiEvent = onUiEvent,
                    )
                },
                imeAction = ImeAction.Next,
                onImeActionClick = onImeActionClick,
                onFocusChanged = { isFocused ->
                    handleOnFocusChange(
                        isFocused = isFocused,
                        inputModelId = inputModel.uid,
                        onUiEvent = onUiEvent,
                    )
                },
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
                    manageOnValueChange(
                        newValue = newValue?.text,
                        inputModel = inputModel,
                        onUiEvent = onUiEvent,
                    )
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
                onValueChange = { newValue ->
                    manageOnValueChange(
                        newValue = newValue,
                        inputModel = inputModel,
                        onUiEvent = onUiEvent,
                    )
                },
            )
        }

        TrackerInputType.DATE_TIME, TrackerInputType.DATE, TrackerInputType.TIME -> {
            ProvideTrackerDateTimeInput(
                model = inputModel,
                inputStyle = inputStyle,
                onNextClicked = onNextClicked,
                modifier = modifierWithFocus,
                onValueChange = { newValue ->
                    onUiEvent(
                        TrackerInputUiEvent.OnValueChange(
                            uid = inputModel.uid,
                            value = newValue,
                        ),
                    )
                },
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
                inputText = inputModel.displayName,
                onValueChanged = { newValue ->
                    onUiEvent(
                        TrackerInputUiEvent.OnValueChange(
                            uid = inputModel.uid,
                            value = newValue,
                        ),
                    )
                },
                onOrgUnitActionCLicked = {
                    onUiEvent(
                        TrackerInputUiEvent.OnOrgUnitButtonClicked(
                            uid = inputModel.uid,
                            label = inputModel.label,
                            value = inputModel.value,
                            orgUnitSelectorScope =
                                inputModel.orgUnitSelectorScope
                                    ?: OrgUnitSelectorScope.UserSearchScope(),
                        ),
                    )
                },
            )
        }

        TrackerInputType.MULTI_SELECTION -> {
            val options =
                inputModel.optionSetConfiguration?.options?.map { optionItem ->
                    CheckBoxData(
                        uid = optionItem.code,
                        checked =
                            optionItem.code.let { code ->
                                inputModel.value
                                    ?.takeIf { it.isNotEmpty() }
                                    ?.split(",")
                                    ?.contains(code)
                            } ?: false,
                        enabled = inputModel.editable,
                        textInput = optionItem.displayName,
                    )
                } ?: emptyList()

            InputMultiSelection(
                modifier = modifierWithFocus.fillMaxWidth(),
                title = inputModel.label,
                items = options,
                state = inputModel.inputState(),
                supportingTextData = inputModel.supportingText(),
                legendData = inputModel.legend,
                isRequired = inputModel.mandatory,
                inputStyle = inputStyle,
                onItemsSelected = { checkBoxData ->
                    val checkedValues =
                        checkBoxData
                            .filter { it.checked }
                            .joinToString(",") { it.uid }
                            .takeIf { it.isNotEmpty() }
                    onUiEvent(
                        TrackerInputUiEvent.OnValueChange(
                            uid = inputModel.uid,
                            value = checkedValues,
                        ),
                    )
                },
                onClearItemSelection = {
                    onUiEvent(
                        TrackerInputUiEvent.OnValueChange(
                            uid = inputModel.uid,
                            value = null,
                        ),
                    )
                },
            )
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
                        TrackerInputUiEvent.OnScanButtonClicked(
                            uid = inputModel.uid,
                            optionSet = inputModel.optionSet,
                            renderType = inputModel.valueType,
                        ),
                    )
                },
                onValueChanged = { newValue ->
                    manageOnValueChange(
                        newValue = newValue?.text,
                        inputModel = inputModel,
                        onUiEvent = onUiEvent,
                    )
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
                        TrackerInputUiEvent.OnScanButtonClicked(
                            uid = inputModel.uid,
                            optionSet = inputModel.optionSet,
                            renderType = inputModel.valueType,
                        ),
                    )
                },
                onValueChanged = { newValue ->
                    manageOnValueChange(
                        newValue = newValue?.text,
                        inputModel = inputModel,
                        onUiEvent = onUiEvent,
                    )
                },
                imeAction = ImeAction.Next,
                onImeActionClick = onImeActionClick,
            )
        }

        TrackerInputType.HORIZONTAL_CHECKBOXES, TrackerInputType.VERTICAL_CHECKBOXES -> {
            TrackerCheckboxInputProvider(
                model = inputModel,
                inputStyle = inputStyle,
                modifier = modifierWithFocus,
                onValueChange = { newValue ->
                    onUiEvent(
                        TrackerInputUiEvent.OnValueChange(
                            uid = inputModel.uid,
                            value = newValue,
                        ),
                    )
                },
            )
        }

        TrackerInputType.HORIZONTAL_RADIOBUTTONS, TrackerInputType.VERTICAL_RADIOBUTTONS -> {
            TrackerRadioButtonInputProvider(
                model = inputModel,
                inputStyle = inputStyle,
                modifier = modifierWithFocus,
                onValueChange = { newValue ->
                    onUiEvent(
                        TrackerInputUiEvent.OnValueChange(
                            uid = inputModel.uid,
                            value = newValue,
                        ),
                    )
                },
            )
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
                    onUiEvent(
                        TrackerInputUiEvent.OnValueChange(
                            uid = inputModel.uid,
                            value = getYesOnlyValue(isChecked),
                        ),
                    )
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
                    onUiEvent(
                        TrackerInputUiEvent.OnValueChange(
                            uid = inputModel.uid,
                            value = if (isChecked) "true" else null,
                        ),
                    )
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
                    onUiEvent(
                        TrackerInputUiEvent.OnValueChange(
                            uid = inputModel.uid,
                            value = null,
                        ),
                    )
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
                    onUiEvent(
                        TrackerInputUiEvent.OnValueChange(
                            uid = inputModel.uid,
                            value = options.getOrNull(index)?.code,
                        ),
                    )
                },
                loadOptions = {
                    optionSetConfiguration.onLoadOptions?.invoke()
                },
                onDismiss = {
                    optionSetConfiguration.onSearch?.invoke("")
                },
            )
        }

        TrackerInputType.CUSTOM_INTENT -> {
            val values by remember(inputModel) {
                derivedStateOf {
                    inputModel.value
                        ?.takeIf { it.isNotEmpty() }
                        ?.split(",")
                        ?.toMutableList() ?: mutableListOf()
                }
            }

            var customIntentState by remember(inputModel, inputModel.value) {
                mutableStateOf(
                    if (inputModel.value.isNullOrEmpty()) {
                        CustomIntentState.LAUNCH
                    } else {
                        CustomIntentState.LOADED
                    },
                )
            }

            InputCustomIntent(
                title = inputModel.label,
                buttonText = stringResource(Res.string.custom_intent_launch),
                supportingText = inputModel.supportingText(),
                inputShellState = inputModel.inputState(),
                inputStyle = inputStyle,
                modifier = modifier,
                isRequired = inputModel.mandatory,
                onLaunch = {
                    customIntentState = CustomIntentState.LOADING
                    inputModel.customIntentUid?.let { customIntentUid ->
                        onUiEvent(
                            TrackerInputUiEvent.OnLaunchCustomIntent(
                                inputModel.uid,
                                customIntentUid,
                            ),
                        )
                    }
                },
                onClear = {
                    onUiEvent(
                        TrackerInputUiEvent.OnValueChange(
                            uid = inputModel.uid,
                            value = null,
                        ),
                    )
                },
                customIntentState = customIntentState,
                values = values.toList(),
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

fun manageOnValueChange(
    newValue: String?,
    inputModel: TrackerInputUiState,
    onUiEvent: (TrackerInputUiEvent) -> Unit,
) {
    newValue?.let {
        if (it != inputModel.value) {
            onUiEvent(
                TrackerInputUiEvent.OnValueChange(
                    uid = inputModel.uid,
                    value = it.takeIf { text -> text.isNotEmpty() },
                ),
            )
        }
    }
}

private fun getYesOnlyValue(checked: Boolean): String? = if (!checked) "true" else null

private fun handleOnFocusChange(
    isFocused: Boolean,
    inputModelId: String,
    onUiEvent: (TrackerInputUiEvent) -> Unit,
) {
    if (isFocused) {
        onUiEvent(TrackerInputUiEvent.OnItemClick(inputModelId))
    }
}
