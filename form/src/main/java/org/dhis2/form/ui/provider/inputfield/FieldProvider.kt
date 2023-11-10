package org.dhis2.form.ui.provider.inputfield

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.ImeAction
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
import org.hisp.dhis.mobile.ui.designsystem.component.internal.RegExValidations

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun FieldProvider(
    modifier: Modifier,
    context: Context,
    fieldUiModel: FieldUiModel,
    uiEventHandler: (RecyclerViewUiEvents) -> Unit,
    intentHandler: (FormIntent) -> Unit,
    resources: ResourceManager,
    focusManager: FocusManager,
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    val modifierWithFocus = modifier
        .bringIntoViewRequester(bringIntoViewRequester)
        .focusRequester(focusRequester)
        .onFocusChanged {
            if (it.isFocused && !fieldUiModel.focused) {
                scope.launch {
                    bringIntoViewRequester.bringIntoView()
                    fieldUiModel.onItemClick()
                }
            }
        }
        .focusable()

    val modifierWithFocusForText = modifier
        .bringIntoViewRequester(bringIntoViewRequester)
        .onFocusChanged {
            if (it.isFocused && !fieldUiModel.focused) {
                scope.launch {
                    bringIntoViewRequester.bringIntoView()
                    fieldUiModel.onItemClick()
                }
            }
        }

    if (fieldUiModel.optionSet == null) {
        when (fieldUiModel.valueType) {
            ValueType.TEXT -> {
                ProvideInputsForValueTypeText(
                    modifier = modifierWithFocusForText,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    uiEventHandler = uiEventHandler,
                    focusManager = focusManager,
                )
            }

            ValueType.INTEGER_POSITIVE -> {
                ProvideIntegerPositive(
                    modifier = modifierWithFocusForText,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    focusManager = focusManager,
                )
            }

            ValueType.INTEGER_ZERO_OR_POSITIVE -> {
                ProvideIntegerPositiveOrZero(
                    modifier = modifierWithFocusForText,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    focusManager = focusManager,
                )
            }

            ValueType.PERCENTAGE -> {
                ProvidePercentage(
                    modifier = modifierWithFocusForText,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    focusManager = focusManager,
                )
            }

            ValueType.NUMBER -> {
                ProvideNumber(
                    modifier = modifierWithFocusForText,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    focusManager = focusManager,
                )
            }

            ValueType.INTEGER_NEGATIVE -> {
                ProvideIntegerNegative(
                    modifier = modifierWithFocusForText,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    focusManager = focusManager,
                )
            }

            ValueType.LONG_TEXT -> {
                ProvideLongText(
                    modifier = modifierWithFocusForText,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    focusManager = focusManager,
                )
            }

            ValueType.LETTER -> {
                ProvideLetter(
                    modifier = modifierWithFocusForText,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    focusManager = focusManager,
                )
            }

            ValueType.INTEGER -> {
                ProvideInteger(
                    modifier = modifierWithFocusForText,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    focusManager = focusManager,
                )
            }

            ValueType.ORGANISATION_UNIT -> {
                ProvideOrgUnitInput(
                    modifier = modifierWithFocus,
                    fieldUiModel = fieldUiModel,
                    uiEventHandler = uiEventHandler,
                    intentHandler = intentHandler,
                    focusRequester = focusRequester,
                )
            }

            ValueType.UNIT_INTERVAL -> {
                ProvideUnitIntervalInput(
                    modifier = modifierWithFocusForText,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                )
            }

            ValueType.EMAIL -> {
                ProvideEmail(
                    modifier = modifierWithFocusForText,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    uiEventHandler = uiEventHandler,
                    focusManager = focusManager,
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
                    modifier = modifierWithFocusForText,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    uiEventHandler = uiEventHandler,
                    focusManager = focusManager,
                )
            }

            ValueType.BOOLEAN -> {
                when (fieldUiModel.renderingType) {
                    UiRenderType.HORIZONTAL_CHECKBOXES,
                    UiRenderType.VERTICAL_CHECKBOXES,
                    -> {
                        ProvideYesNoCheckBoxInput(
                            modifier = modifierWithFocus,
                            fieldUiModel = fieldUiModel,
                            intentHandler = intentHandler,
                            resources = resources,
                            focusRequester = focusRequester,
                        )
                    }

                    else -> {
                        ProvideYesNoRadioButtonInput(
                            modifier = modifierWithFocus,
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
                            fieldUiModel = fieldUiModel,
                            intentHandler = intentHandler,
                        )
                    }

                    else -> {
                        ProvideYesOnlyCheckBoxInput(
                            modifier = modifierWithFocus,
                            fieldUiModel = fieldUiModel,
                            intentHandler = intentHandler,
                            focusRequester = focusRequester,
                        )
                    }
                }
            }

            ValueType.PHONE_NUMBER -> {
                ProvideInputPhoneNumber(
                    modifier = modifierWithFocusForText,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    uiEventHandler = uiEventHandler,
                    focusManager = focusManager,
                )
            }

            ValueType.DATE,
            ValueType.DATETIME,
            ValueType.TIME,
            -> {
                ProvideInputDate(
                    modifier = modifierWithFocusForText,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    uiEventHandler = uiEventHandler,
                )
            }

            ValueType.IMAGE -> {
                when (fieldUiModel.renderingType) {
                    UiRenderType.CANVAS -> {
                        ProvideInputSignature(
                            modifier = modifierWithFocus,
                            fieldUiModel = fieldUiModel,
                            intentHandler = intentHandler,
                            uiEventHandler = uiEventHandler,
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
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    uiEventHandler = uiEventHandler,
                    resources = resources,
                )
            }
            ValueType.REFERENCE,
            ValueType.GEOJSON,
            ValueType.USERNAME,
            ValueType.TRACKER_ASSOCIATE,
            ValueType.MULTI_TEXT,
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
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    focusRequester = focusRequester,
                )
            }

            UiRenderType.MATRIX -> {
                ProvideMatrixInput(
                    modifier = modifierWithFocus,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    context = context,
                )
            }

            UiRenderType.SEQUENCIAL -> {
                ProvideSequentialInput(
                    modifier = modifierWithFocus,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    context = context,
                )
            }

            // "Remaining option sets" are in fun getLayoutForOptionSet

            else -> {
                ProvideDropdownInput(
                    modifier = modifierWithFocus,
                    fieldUiModel = fieldUiModel,
                )
            }
        }
    }
}

@Composable
private fun ProvideIntegerPositive(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    focusManager: FocusManager,
) {
    var value by remember {
        mutableStateOf(fieldUiModel.value)
    }

    InputPositiveInteger(
        modifier = modifier.fillMaxWidth(),
        title = fieldUiModel.label,
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        inputText = value ?: "",
        isRequiredField = fieldUiModel.mandatory,
        onValueChanged = {
            value = it
            intentHandler(
                FormIntent.OnTextChange(
                    fieldUiModel.uid,
                    value,
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
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    focusManager: FocusManager,
) {
    var value by remember {
        mutableStateOf(fieldUiModel.value)
    }

    InputPositiveIntegerOrZero(
        modifier = modifier.fillMaxWidth(),
        title = fieldUiModel.label,
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        inputText = value ?: "",
        isRequiredField = fieldUiModel.mandatory,
        onValueChanged = {
            value = it
            intentHandler(
                FormIntent.OnTextChange(
                    fieldUiModel.uid,
                    value,
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
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    focusManager: FocusManager,
) {
    var value by remember {
        mutableStateOf(fieldUiModel.value)
    }

    InputPercentage(
        modifier = modifier.fillMaxWidth(),
        title = fieldUiModel.label,
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        inputText = value ?: "",
        isRequiredField = fieldUiModel.mandatory,
        onValueChanged = {
            value = it
            intentHandler(
                FormIntent.OnTextChange(
                    fieldUiModel.uid,
                    value,
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
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    focusManager: FocusManager,
) {
    var value by remember {
        mutableStateOf(fieldUiModel.value)
    }

    InputNumber(
        modifier = modifier.fillMaxWidth(),
        title = fieldUiModel.label,
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        inputText = value ?: "",
        isRequiredField = fieldUiModel.mandatory,
        onValueChanged = {
            value = it
            intentHandler(
                FormIntent.OnTextChange(
                    fieldUiModel.uid,
                    value,
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
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    focusManager: FocusManager,
) {
    var value by remember {
        mutableStateOf(fieldUiModel.value?.replace("-", ""))
    }

    InputNegativeInteger(
        modifier = modifier.fillMaxWidth(),
        title = fieldUiModel.label,
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        inputText = value ?: "",
        isRequiredField = fieldUiModel.mandatory,
        onValueChanged = {
            value = it
            intentHandler(
                FormIntent.OnTextChange(
                    fieldUiModel.uid,
                    value,
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
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    focusManager: FocusManager,
) {
    var value by remember {
        mutableStateOf(fieldUiModel.value)
    }

    InputLongText(
        modifier = modifier.fillMaxWidth(),
        title = fieldUiModel.label,
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        inputText = value ?: "",
        isRequiredField = fieldUiModel.mandatory,
        onValueChanged = {
            value = it
            intentHandler(
                FormIntent.OnTextChange(
                    fieldUiModel.uid,
                    value,
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
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    focusManager: FocusManager,
) {
    var value by remember {
        mutableStateOf(fieldUiModel.value)
    }

    InputLetter(
        modifier = modifier.fillMaxWidth(),
        title = fieldUiModel.label,
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        inputText = value ?: "",
        isRequiredField = fieldUiModel.mandatory,
        onValueChanged = {
            value = it
            intentHandler(
                FormIntent.OnTextChange(
                    fieldUiModel.uid,
                    value,
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
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    focusManager: FocusManager,
) {
    var value by remember {
        mutableStateOf(fieldUiModel.value)
    }

    InputInteger(
        modifier = modifier.fillMaxWidth(),
        title = fieldUiModel.label,
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        inputText = value ?: "",
        isRequiredField = fieldUiModel.mandatory,
        onValueChanged = {
            value = it
            intentHandler(
                FormIntent.OnTextChange(
                    fieldUiModel.uid,
                    value,
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
    fieldUiModel: FieldUiModel,
    uiEventHandler: (RecyclerViewUiEvents) -> Unit,
    intentHandler: (FormIntent) -> Unit,
    focusManager: FocusManager,
) {
    var value by remember {
        mutableStateOf(fieldUiModel.value)
    }

    InputEmail(
        modifier = modifier.fillMaxWidth(),
        title = fieldUiModel.label,
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        inputText = value ?: "",
        isRequiredField = fieldUiModel.mandatory,
        onValueChanged = {
            value = it
            intentHandler(
                FormIntent.OnTextChange(
                    fieldUiModel.uid,
                    value,
                    fieldUiModel.valueType,
                ),
            )
        },
        onEmailActionCLicked = {
            uiEventHandler.invoke(
                RecyclerViewUiEvents.OpenChooserIntent(
                    Intent.ACTION_SENDTO,
                    value,
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
    intentHandler: (FormIntent) -> Unit,
    uiEventHandler: (RecyclerViewUiEvents) -> Unit,
    focusManager: FocusManager,
    modifier: Modifier = Modifier,
) {
    var value by remember {
        mutableStateOf(fieldUiModel.value)
    }

    InputPhoneNumber(
        modifier = modifier.fillMaxWidth(),
        title = fieldUiModel.label,
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        inputText = value ?: "",
        isRequiredField = fieldUiModel.mandatory,
        onValueChanged = {
            value = it
            intentHandler(
                FormIntent.OnTextChange(
                    fieldUiModel.uid,
                    value,
                    fieldUiModel.valueType,
                ),
            )
        },
        onCallActionClicked = {
            uiEventHandler.invoke(
                RecyclerViewUiEvents.OpenChooserIntent(
                    Intent.ACTION_DIAL,
                    value,
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
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    uiEventHandler: (RecyclerViewUiEvents) -> Unit,
    focusManager: FocusManager,
) {
    var value by remember {
        mutableStateOf(fieldUiModel.value)
    }

    InputLink(
        modifier = modifier.fillMaxWidth(),
        title = fieldUiModel.label,
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        inputText = value ?: "",
        isRequiredField = fieldUiModel.mandatory,
        onValueChanged = {
            value = it
            intentHandler(
                FormIntent.OnTextChange(
                    fieldUiModel.uid,
                    value,
                    fieldUiModel.valueType,
                ),
            )
        },
        onLinkActionCLicked = {
            uiEventHandler.invoke(
                RecyclerViewUiEvents.OpenChooserIntent(
                    Intent.ACTION_VIEW,
                    value,
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
                ),
            )
        },

    )
}
