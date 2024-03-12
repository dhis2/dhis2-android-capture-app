package org.dhis2.form.ui.provider.inputfield

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.launch
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.extensions.autocompleteList
import org.dhis2.form.extensions.inputState
import org.dhis2.form.extensions.legend
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.UiRenderType
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.intent.FormIntent
import org.dhis2.form.ui.keyboard.keyboardAsState
import org.hisp.dhis.android.core.common.ValueType
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
import org.hisp.dhis.mobile.ui.designsystem.component.InputStyle
import org.hisp.dhis.mobile.ui.designsystem.component.internal.RegExValidations

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FieldProvider(
    modifier: Modifier,
    inputStyle: InputStyle = InputStyle.DataInputStyle(),
    fieldUiModel: FieldUiModel,
    uiEventHandler: (RecyclerViewUiEvents) -> Unit,
    intentHandler: (FormIntent) -> Unit,
    resources: ResourceManager,
    focusManager: FocusManager,
    onNextClicked: () -> Unit,
) {
    val context = LocalContext.current
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val focusRequester = remember { FocusRequester() }
    var visibleArea by remember { mutableStateOf(Rect.Zero) }
    val scope = rememberCoroutineScope()
    val keyboardState by keyboardAsState()

    var modifierWithFocus = modifier
        .bringIntoViewRequester(bringIntoViewRequester)
        .onSizeChanged { intSize ->
            visibleArea = Rect(
                size = Size(intSize.width.toFloat(), intSize.height.toFloat()),
                offset = Offset(0f, 20f),
            )
        }
        .onFocusChanged {
            if (it.isFocused && !fieldUiModel.focused) {
                scope.launch {
                    bringIntoViewRequester.bringIntoView(visibleArea)
                    fieldUiModel.onItemClick()
                }
            }
        }

    if (!fieldUiModel.needKeyboard()) {
        modifierWithFocus = modifierWithFocus
            .focusRequester(focusRequester)
            .focusable()
    }

    LaunchedEffect(keyboardState) {
        if (fieldUiModel.focused) {
            bringIntoViewRequester.bringIntoView(visibleArea)
        }
    }

    if (fieldUiModel.optionSet == null) {
        when (fieldUiModel.valueType) {
            ValueType.TEXT -> {
                ProvideInputsForValueTypeText(
                    modifier = modifierWithFocus,
                    inputStyle = inputStyle,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    uiEventHandler = uiEventHandler,
                    focusManager = focusManager,
                    onNextClicked = onNextClicked,
                )
            }

            ValueType.INTEGER_POSITIVE -> {
                ProvideIntegerPositive(
                    modifier = modifierWithFocus,
                    inputStyle = inputStyle,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    focusManager = focusManager,
                    onNextClicked = onNextClicked,
                )
            }

            ValueType.INTEGER_ZERO_OR_POSITIVE -> {
                ProvideIntegerPositiveOrZero(
                    modifier = modifierWithFocus,
                    inputStyle = inputStyle,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    focusManager = focusManager,
                    onNextClicked = onNextClicked,

                )
            }

            ValueType.PERCENTAGE -> {
                ProvidePercentage(
                    modifier = modifierWithFocus,
                    inputStyle = inputStyle,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    focusManager = focusManager,
                    onNextClicked = onNextClicked,

                )
            }

            ValueType.NUMBER -> {
                ProvideNumber(
                    modifier = modifierWithFocus,
                    inputStyle = inputStyle,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    focusManager = focusManager,
                    onNextClicked = onNextClicked,

                )
            }

            ValueType.INTEGER_NEGATIVE -> {
                ProvideIntegerNegative(
                    modifier = modifierWithFocus,
                    inputStyle = inputStyle,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    focusManager = focusManager,
                    onNextClicked = onNextClicked,

                )
            }

            ValueType.LONG_TEXT -> {
                ProvideLongText(
                    modifier = modifierWithFocus,
                    inputStyle = inputStyle,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    focusManager = focusManager,
                    onNextClicked = onNextClicked,

                )
            }

            ValueType.LETTER -> {
                ProvideLetter(
                    modifier = modifierWithFocus,
                    inputStyle = inputStyle,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    focusManager = focusManager,
                    onNextClicked = onNextClicked,

                )
            }

            ValueType.INTEGER -> {
                ProvideInteger(
                    modifier = modifierWithFocus,
                    inputStyle = inputStyle,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    focusManager = focusManager,
                    onNextClicked = onNextClicked,

                )
            }

            ValueType.ORGANISATION_UNIT -> {
                ProvideOrgUnitInput(
                    modifier = modifierWithFocus,
                    inputStyle = inputStyle,
                    fieldUiModel = fieldUiModel,
                    uiEventHandler = uiEventHandler,
                    intentHandler = intentHandler,
                    focusRequester = focusRequester,
                )
            }

            ValueType.UNIT_INTERVAL -> {
                ProvideUnitIntervalInput(
                    modifier = modifierWithFocus,
                    inputStyle = inputStyle,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    onNextClicked = onNextClicked,
                )
            }

            ValueType.EMAIL -> {
                ProvideEmail(
                    modifier = modifierWithFocus,
                    inputStyle = inputStyle,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    uiEventHandler = uiEventHandler,
                    focusManager = focusManager,
                    onNextClicked = onNextClicked,

                )
            }

            ValueType.FILE_RESOURCE -> {
                ProvideInputFileResource(
                    modifier = modifierWithFocus,
                    fieldUiModel = fieldUiModel,
                    resources = resources,
                    uiEventHandler = uiEventHandler,
                )
            }

            ValueType.URL -> {
                ProvideInputLink(
                    modifier = modifierWithFocus,
                    inputStyle = inputStyle,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    uiEventHandler = uiEventHandler,
                    focusManager = focusManager,
                    onNextClicked = onNextClicked,

                )
            }

            ValueType.BOOLEAN -> {
                when (fieldUiModel.renderingType) {
                    UiRenderType.HORIZONTAL_CHECKBOXES,
                    UiRenderType.VERTICAL_CHECKBOXES,
                    -> {
                        ProvideYesNoCheckBoxInput(
                            modifier = modifierWithFocus,
                            inputStyle = inputStyle,
                            fieldUiModel = fieldUiModel,
                            intentHandler = intentHandler,
                            resources = resources,
                            focusRequester = focusRequester,
                        )
                    }

                    else -> {
                        ProvideYesNoRadioButtonInput(
                            modifier = modifierWithFocus,
                            inputStyle = inputStyle,
                            fieldUiModel = fieldUiModel,
                            intentHandler = intentHandler,
                            resources = resources,
                            focusRequester = focusRequester,
                        )
                    }
                }
            }

            ValueType.TRUE_ONLY -> {
                when (fieldUiModel.renderingType) {
                    UiRenderType.TOGGLE -> {
                        ProvideYesOnlySwitchInput(
                            modifier = modifierWithFocus,
                            inputStyle = inputStyle,
                            fieldUiModel = fieldUiModel,
                            intentHandler = intentHandler,
                        )
                    }

                    else -> {
                        ProvideYesOnlyCheckBoxInput(
                            modifier = modifierWithFocus,
                            inputStyle = inputStyle,
                            fieldUiModel = fieldUiModel,
                            intentHandler = intentHandler,
                            focusRequester = focusRequester,
                        )
                    }
                }
            }

            ValueType.PHONE_NUMBER -> {
                ProvideInputPhoneNumber(
                    modifier = modifierWithFocus,
                    inputStyle = inputStyle,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    uiEventHandler = uiEventHandler,
                    focusManager = focusManager,
                    onNextClicked = onNextClicked,

                )
            }

            ValueType.DATE,
            ValueType.DATETIME,
            ValueType.TIME,
            -> {
                ProvideInputDate(
                    modifier = modifierWithFocus,
                    inputStyle = inputStyle,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    onNextClicked = onNextClicked,
                )
            }

            ValueType.IMAGE -> {
                when (fieldUiModel.renderingType) {
                    UiRenderType.CANVAS -> {
                        ProvideInputSignature(
                            modifier = modifierWithFocus,
                            fieldUiModel = fieldUiModel,
                        )
                    }

                    else -> {
                        ProvideInputImage(
                            modifier = modifierWithFocus,
                            fieldUiModel = fieldUiModel,
                            intentHandler = intentHandler,
                            uiEventHandler = uiEventHandler,
                            resources = resources,
                        )
                    }
                }
            }

            ValueType.COORDINATE -> {
                when (fieldUiModel.renderingType) {
                    UiRenderType.POLYGON, UiRenderType.MULTI_POLYGON -> {
                        ProvidePolygon(
                            modifier = modifierWithFocus,
                            fieldUiModel = fieldUiModel,
                        )
                    }

                    else -> {
                        ProvideInputCoordinate(
                            modifier = modifierWithFocus,
                            fieldUiModel = fieldUiModel,
                            intentHandler = intentHandler,
                            uiEventHandler = uiEventHandler,
                            resources = resources,
                            focusRequester = focusRequester,
                        )
                    }
                }
            }

            ValueType.AGE -> {
                ProvideInputAge(
                    modifier = modifierWithFocus,
                    inputStyle = inputStyle,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    resources = resources,
                )
            }

            ValueType.MULTI_TEXT -> {
                ProvideMultiSelectionInput(
                    modifier = modifierWithFocus,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                )
            }

            ValueType.REFERENCE,
            ValueType.GEOJSON,
            ValueType.USERNAME,
            ValueType.TRACKER_ASSOCIATE,
            null,
            -> {
                InputNotSupported(title = fieldUiModel.label)
            }
        }
    } else {
        when (fieldUiModel.renderingType) {
            UiRenderType.HORIZONTAL_RADIOBUTTONS,
            UiRenderType.VERTICAL_RADIOBUTTONS,
            -> {
                ProvideRadioButtonInput(
                    modifier = modifierWithFocus,
                    inputStyle = inputStyle,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    focusRequester = focusRequester,
                )
            }

            UiRenderType.HORIZONTAL_CHECKBOXES,
            UiRenderType.VERTICAL_CHECKBOXES,
            -> {
                ProvideCheckBoxInput(
                    modifier = modifierWithFocus,
                    inputStyle = inputStyle,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    focusRequester = focusRequester,
                )
            }

            UiRenderType.MATRIX -> {
                ProvideMatrixInput(
                    modifier = modifierWithFocus,
                    inputStyle = inputStyle,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    context = context,
                )
            }

            UiRenderType.SEQUENCIAL -> {
                ProvideSequentialInput(
                    modifier = modifierWithFocus,
                    inputStyle = inputStyle,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    context = context,
                )
            }

            // "Remaining option sets" are in fun getLayoutForOptionSet

            else -> {
                ProvideDropdownInput(
                    modifier = modifierWithFocus,
                    inputStyle = inputStyle,
                    fieldUiModel = fieldUiModel,
                )
            }
        }
    }
}

@Composable
private fun ProvideIntegerPositive(
    modifier: Modifier,
    inputStyle: InputStyle,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    focusManager: FocusManager,
    onNextClicked: () -> Unit,
) {
    val textSelection = TextRange(if (fieldUiModel.value != null) fieldUiModel.value!!.length else 0)

    var value by remember(fieldUiModel.value) {
        mutableStateOf(TextFieldValue(fieldUiModel.value ?: "", textSelection))
    }

    InputPositiveInteger(
        modifier = modifier.fillMaxWidth(),
        inputStyle = inputStyle,
        title = fieldUiModel.label,
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        inputTextFieldValue = value,
        isRequiredField = fieldUiModel.mandatory,
        onNextClicked = onNextClicked,
        onValueChanged = {
            value = it ?: TextFieldValue()
            intentHandler(
                FormIntent.OnTextChange(
                    fieldUiModel.uid,
                    value.text,
                    fieldUiModel.valueType,
                ),
            )
        },
        autoCompleteList = fieldUiModel.autocompleteList(),
        autoCompleteItemSelected = {
            focusManager.clearFocus()
        },
    )
}

@Composable
private fun ProvideIntegerPositiveOrZero(
    modifier: Modifier,
    inputStyle: InputStyle,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    focusManager: FocusManager,
    onNextClicked: () -> Unit,

) {
    val textSelection = TextRange(if (fieldUiModel.value != null) fieldUiModel.value!!.length else 0)

    var value by remember(fieldUiModel.value) {
        mutableStateOf(TextFieldValue(fieldUiModel.value ?: "", textSelection))
    }

    InputPositiveIntegerOrZero(
        modifier = modifier.fillMaxWidth(),
        inputStyle = inputStyle,
        title = fieldUiModel.label,
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        inputTextFieldValue = value,
        isRequiredField = fieldUiModel.mandatory,
        onNextClicked = onNextClicked,
        onValueChanged = {
            value = it ?: TextFieldValue()
            intentHandler(
                FormIntent.OnTextChange(
                    fieldUiModel.uid,
                    value.text,
                    fieldUiModel.valueType,
                ),
            )
        },
        autoCompleteList = fieldUiModel.autocompleteList(),
        autoCompleteItemSelected = {
            focusManager.clearFocus()
        },
    )
}

@Composable
private fun ProvidePercentage(
    modifier: Modifier = Modifier,
    inputStyle: InputStyle,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    focusManager: FocusManager,
    onNextClicked: () -> Unit,

) {
    val textSelection = TextRange(if (fieldUiModel.value != null) fieldUiModel.value!!.length else 0)

    var value by remember(fieldUiModel.value) {
        mutableStateOf(TextFieldValue(fieldUiModel.value ?: "", textSelection))
    }

    InputPercentage(
        modifier = modifier.fillMaxWidth(),
        inputStyle = inputStyle,
        title = fieldUiModel.label,
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        inputTextFieldValue = value,
        isRequiredField = fieldUiModel.mandatory,
        onNextClicked = onNextClicked,
        onValueChanged = {
            value = it ?: TextFieldValue()
            intentHandler(
                FormIntent.OnTextChange(
                    fieldUiModel.uid,
                    value.text,
                    fieldUiModel.valueType,
                ),
            )
        },
        autoCompleteList = fieldUiModel.autocompleteList(),
        autoCompleteItemSelected = {
            focusManager.clearFocus()
        },
    )
}

@Composable
private fun ProvideNumber(
    modifier: Modifier,
    inputStyle: InputStyle,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    focusManager: FocusManager,
    onNextClicked: () -> Unit,

) {
    val textSelection = TextRange(if (fieldUiModel.value != null) fieldUiModel.value!!.length else 0)

    var value by remember(fieldUiModel.value) {
        mutableStateOf(TextFieldValue(fieldUiModel.value ?: "", textSelection))
    }

    InputNumber(
        modifier = modifier.fillMaxWidth(),
        inputStyle = inputStyle,
        title = fieldUiModel.label,
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        inputTextFieldValue = value,
        isRequiredField = fieldUiModel.mandatory,
        onNextClicked = onNextClicked,
        onValueChanged = {
            value = it ?: TextFieldValue()
            intentHandler(
                FormIntent.OnTextChange(
                    fieldUiModel.uid,
                    value.text,
                    fieldUiModel.valueType,
                ),
            )
        },
        notation = RegExValidations.BRITISH_DECIMAL_NOTATION,
        autoCompleteList = fieldUiModel.autocompleteList(),
        autoCompleteItemSelected = {
            focusManager.clearFocus()
        },
    )
}

@Composable
private fun ProvideIntegerNegative(
    modifier: Modifier = Modifier,
    inputStyle: InputStyle,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    focusManager: FocusManager,
    onNextClicked: () -> Unit,

) {
    val textSelection = TextRange(if (fieldUiModel.value != null) fieldUiModel.value!!.length else 0)
    var value by remember(fieldUiModel.value) {
        mutableStateOf(TextFieldValue(fieldUiModel.value?.replace("-", "") ?: "", textSelection))
    }

    InputNegativeInteger(
        modifier = modifier.fillMaxWidth(),
        inputStyle = inputStyle,
        title = fieldUiModel.label,
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        inputTextFieldValue = value,
        isRequiredField = fieldUiModel.mandatory,
        onNextClicked = onNextClicked,
        onValueChanged = {
            value = it ?: TextFieldValue()
            intentHandler(
                FormIntent.OnTextChange(
                    fieldUiModel.uid,
                    value.text,
                    fieldUiModel.valueType,
                ),
            )
        },
        autoCompleteList = fieldUiModel.autocompleteList(),
        autoCompleteItemSelected = {
            focusManager.clearFocus()
        },
    )
}

@Composable
private fun ProvideLongText(
    modifier: Modifier,
    inputStyle: InputStyle,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    focusManager: FocusManager,
    onNextClicked: () -> Unit,

) {
    val textSelection = TextRange(if (fieldUiModel.value != null) fieldUiModel.value!!.length else 0)

    var value by remember(fieldUiModel.value) {
        mutableStateOf(TextFieldValue(fieldUiModel.value ?: "", textSelection))
    }

    InputLongText(
        modifier = modifier.fillMaxWidth(),
        inputStyle = inputStyle,
        title = fieldUiModel.label,
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        inputTextFieldValue = value,
        isRequiredField = fieldUiModel.mandatory,
        onNextClicked = onNextClicked,
        onValueChanged = {
            value = it ?: TextFieldValue()
            intentHandler(
                FormIntent.OnTextChange(
                    fieldUiModel.uid,
                    value.text,
                    fieldUiModel.valueType,
                ),
            )
        },
        imeAction = ImeAction.Default,
        autoCompleteList = fieldUiModel.autocompleteList(),
        autoCompleteItemSelected = {
            focusManager.clearFocus()
        },
    )
}

@Composable
private fun ProvideLetter(
    modifier: Modifier,
    inputStyle: InputStyle,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    focusManager: FocusManager,
    onNextClicked: () -> Unit,

) {
    val textSelection = TextRange(if (fieldUiModel.value != null) fieldUiModel.value!!.length else 0)
    var value by remember(fieldUiModel.value) {
        mutableStateOf(TextFieldValue(fieldUiModel.value ?: "", textSelection))
    }

    InputLetter(
        modifier = modifier.fillMaxWidth(),
        inputStyle = inputStyle,
        title = fieldUiModel.label,
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        inputTextFieldValue = value,
        isRequiredField = fieldUiModel.mandatory,
        onNextClicked = onNextClicked,
        onValueChanged = {
            value = it ?: TextFieldValue()
            intentHandler(
                FormIntent.OnTextChange(
                    fieldUiModel.uid,
                    value.text,
                    fieldUiModel.valueType,
                ),
            )
        },
        autoCompleteList = fieldUiModel.autocompleteList(),
        autoCompleteItemSelected = {
            focusManager.clearFocus()
        },
    )
}

@Composable
private fun ProvideInteger(
    modifier: Modifier,
    inputStyle: InputStyle,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    focusManager: FocusManager,
    onNextClicked: () -> Unit,

) {
    val textSelection = TextRange(if (fieldUiModel.value != null) fieldUiModel.value!!.length else 0)
    var value by remember(fieldUiModel.value) {
        mutableStateOf(TextFieldValue(fieldUiModel.value ?: "", textSelection))
    }

    InputInteger(
        modifier = modifier.fillMaxWidth(),
        inputStyle = inputStyle,
        title = fieldUiModel.label,
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        inputTextFieldValue = value,
        isRequiredField = fieldUiModel.mandatory,
        onNextClicked = onNextClicked,
        onValueChanged = {
            value = it ?: TextFieldValue()
            intentHandler(
                FormIntent.OnTextChange(
                    fieldUiModel.uid,
                    value.text,
                    fieldUiModel.valueType,
                ),
            )
        },
        autoCompleteList = fieldUiModel.autocompleteList(),
        autoCompleteItemSelected = {
            focusManager.clearFocus()
        },
    )
}

@Composable
private fun ProvideEmail(
    modifier: Modifier,
    inputStyle: InputStyle,
    fieldUiModel: FieldUiModel,
    uiEventHandler: (RecyclerViewUiEvents) -> Unit,
    intentHandler: (FormIntent) -> Unit,
    focusManager: FocusManager,
    onNextClicked: () -> Unit,
) {
    val textSelection = TextRange(if (fieldUiModel.value != null) fieldUiModel.value!!.length else 0)

    var value by remember(fieldUiModel.value) {
        mutableStateOf(TextFieldValue(fieldUiModel.value ?: "", textSelection))
    }

    InputEmail(
        modifier = modifier.fillMaxWidth(),
        inputStyle = inputStyle,
        title = fieldUiModel.label,
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        inputTextFieldValue = value,
        isRequiredField = fieldUiModel.mandatory,
        onNextClicked = onNextClicked,
        onValueChanged = {
            value = it ?: TextFieldValue()
            intentHandler(
                FormIntent.OnTextChange(
                    fieldUiModel.uid,
                    value.text,
                    fieldUiModel.valueType,
                ),
            )
        },
        onEmailActionCLicked = {
            uiEventHandler.invoke(
                RecyclerViewUiEvents.OpenChooserIntent(
                    Intent.ACTION_SENDTO,
                    value.text,
                    fieldUiModel.uid,
                ),
            )
        },
        autoCompleteList = fieldUiModel.autocompleteList(),
        autoCompleteItemSelected = {
            focusManager.clearFocus()
        },
    )
}

@Composable
private fun ProvideInputPhoneNumber(
    fieldUiModel: FieldUiModel,
    inputStyle: InputStyle,
    intentHandler: (FormIntent) -> Unit,
    uiEventHandler: (RecyclerViewUiEvents) -> Unit,
    focusManager: FocusManager,
    modifier: Modifier = Modifier,
    onNextClicked: () -> Unit,

) {
    val textSelection = TextRange(if (fieldUiModel.value != null) fieldUiModel.value!!.length else 0)

    var value by remember(fieldUiModel.value) {
        mutableStateOf(TextFieldValue(fieldUiModel.value ?: "", textSelection))
    }

    InputPhoneNumber(
        modifier = modifier.fillMaxWidth(),
        inputStyle = inputStyle,
        title = fieldUiModel.label,
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        inputTextFieldValue = value,
        isRequiredField = fieldUiModel.mandatory,
        onNextClicked = onNextClicked,
        onValueChanged = {
            value = it ?: TextFieldValue()
            intentHandler(
                FormIntent.OnTextChange(
                    fieldUiModel.uid,
                    value.text,
                    fieldUiModel.valueType,
                ),
            )
        },
        onCallActionClicked = {
            uiEventHandler.invoke(
                RecyclerViewUiEvents.OpenChooserIntent(
                    Intent.ACTION_DIAL,
                    value.text,
                    fieldUiModel.uid,
                ),
            )
        },
        autoCompleteList = fieldUiModel.autocompleteList(),
        autoCompleteItemSelected = {
            focusManager.clearFocus()
        },
    )
}

@Composable
private fun ProvideInputLink(
    modifier: Modifier,
    inputStyle: InputStyle,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    uiEventHandler: (RecyclerViewUiEvents) -> Unit,
    focusManager: FocusManager,
    onNextClicked: () -> Unit,

) {
    val textSelection = TextRange(if (fieldUiModel.value != null) fieldUiModel.value!!.length else 0)

    var value by remember(fieldUiModel.value) {
        mutableStateOf(TextFieldValue(fieldUiModel.value ?: "", textSelection))
    }

    InputLink(
        modifier = modifier.fillMaxWidth(),
        inputStyle = inputStyle,
        title = fieldUiModel.label,
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        inputTextFieldValue = value,
        isRequiredField = fieldUiModel.mandatory,
        onNextClicked = onNextClicked,
        onValueChanged = {
            value = it ?: TextFieldValue()
            intentHandler(
                FormIntent.OnTextChange(
                    fieldUiModel.uid,
                    value.text,
                    fieldUiModel.valueType,
                ),
            )
        },
        onLinkActionCLicked = {
            uiEventHandler.invoke(
                RecyclerViewUiEvents.OpenChooserIntent(
                    Intent.ACTION_VIEW,
                    value.text,
                    fieldUiModel.uid,
                ),
            )
        },
        autoCompleteList = fieldUiModel.autocompleteList(),
        autoCompleteItemSelected = {
            focusManager.clearFocus()
        },
    )
}

@Composable
private fun ProvideOrgUnitInput(
    modifier: Modifier,
    inputStyle: InputStyle,
    fieldUiModel: FieldUiModel,
    uiEventHandler: (RecyclerViewUiEvents) -> Unit,
    intentHandler: (FormIntent) -> Unit,
    focusRequester: FocusRequester,
) {
    var inputFieldValue by remember(
        fieldUiModel,
    ) {
        mutableStateOf(fieldUiModel.displayName)
    }

    InputOrgUnit(
        modifier = modifier.fillMaxWidth(),
        inputStyle = inputStyle,
        title = fieldUiModel.label,
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        inputText = inputFieldValue ?: "",
        isRequiredField = fieldUiModel.mandatory,
        onValueChanged = {
            inputFieldValue = it
            focusRequester.requestFocus()
            intentHandler(
                FormIntent.OnSave(
                    fieldUiModel.uid,
                    it,
                    fieldUiModel.valueType,
                ),
            )
        },
        onOrgUnitActionCLicked = {
            focusRequester.requestFocus()
            uiEventHandler.invoke(
                RecyclerViewUiEvents.OpenOrgUnitDialog(
                    fieldUiModel.uid,
                    fieldUiModel.label,
                    fieldUiModel.value,
                    fieldUiModel.orgUnitSelectorScope,
                ),
            )
        },

    )
}

private fun FieldUiModel.needKeyboard() = optionSet == null &&
    valueType?.let { it.isText || it.isNumeric || it.isDate } ?: false
