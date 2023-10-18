package org.dhis2.form.ui.provider.inputfield

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.text.TextWatcher
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import kotlinx.coroutines.launch
import org.dhis2.form.BR
import org.dhis2.form.R
import org.dhis2.form.extensions.inputState
import org.dhis2.form.extensions.legend
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.UiRenderType
import org.dhis2.form.ui.LatitudeLongitudeTextWatcher
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.intent.FormIntent
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.mobile.ui.designsystem.component.InputEmail
import org.hisp.dhis.mobile.ui.designsystem.component.InputInteger
import org.hisp.dhis.mobile.ui.designsystem.component.InputLetter
import org.hisp.dhis.mobile.ui.designsystem.component.InputLink
import org.hisp.dhis.mobile.ui.designsystem.component.InputLongText
import org.hisp.dhis.mobile.ui.designsystem.component.InputNegativeInteger
import org.hisp.dhis.mobile.ui.designsystem.component.InputNumber
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
    needToForceUpdate: Boolean,
    textWatcher: TextWatcher,
    coordinateTextWatcher: LatitudeLongitudeTextWatcher,
    uiEventHandler: (RecyclerViewUiEvents) -> Unit,
    intentHandler: (FormIntent) -> Unit,
    resources: Resources,
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    val modifierWithFocus = modifier
        .bringIntoViewRequester(bringIntoViewRequester)
        .onFocusEvent {
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
                    modifier = modifierWithFocus,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    uiEventHandler = uiEventHandler,
                )
            }

            ValueType.INTEGER_POSITIVE -> {
                ProvideIntegerPositive(
                    modifier = modifierWithFocus,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                )
            }

            ValueType.INTEGER_ZERO_OR_POSITIVE -> {
                ProvideIntegerPositiveOrZero(
                    modifier = modifierWithFocus,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                )
            }

            ValueType.PERCENTAGE -> {
                ProvidePercentage(
                    modifier = modifierWithFocus,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                )
            }

            ValueType.NUMBER -> {
                ProvideNumber(
                    modifier = modifierWithFocus,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                )
            }

            ValueType.INTEGER_NEGATIVE -> {
                ProvideIntegerNegative(
                    modifier = modifierWithFocus,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                )
            }

            ValueType.LONG_TEXT -> {
                ProvideLongText(
                    modifier = modifierWithFocus,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                )
            }

            ValueType.LETTER -> {
                ProvideLetter(
                    modifier = modifierWithFocus,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                )
            }

            ValueType.INTEGER -> {
                ProvideInteger(
                    modifier = modifierWithFocus,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                )
            }

            ValueType.EMAIL -> {
                ProvideEmail(
                    modifier = modifierWithFocus,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    uiEventHandler = uiEventHandler,
                )
            }

            ValueType.URL -> {
                ProvideInputLink(
                    modifier = modifierWithFocus,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    uiEventHandler = uiEventHandler,
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
                        )
                    }

                    else -> {
                        ProvideYesNoRadioButtonInput(
                            modifier = modifierWithFocus,
                            fieldUiModel = fieldUiModel,
                            intentHandler = intentHandler,
                            resources = resources,
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
                        )
                    }
                }
            }

            ValueType.PHONE_NUMBER -> {
                ProvideInputPhoneNumber(
                    modifier = modifierWithFocus,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    uiEventHandler = uiEventHandler,
                )
            }

            ValueType.DATE,
            ValueType.DATETIME,
            ValueType.TIME,
            -> {
                ProvideInputDate(
                    modifier = modifierWithFocus,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    uiEventHandler = uiEventHandler,
                )
            }

            else -> {
                AndroidViewBinding(
                    modifier = modifier.fillMaxWidth(),
                    factory = { inflater, viewgroup, add ->
                        getFieldView(
                            context,
                            inflater,
                            viewgroup,
                            add,
                            fieldUiModel.layoutId,
                            needToForceUpdate,
                        )
                    },
                    update = {
                        this.setVariable(BR.textWatcher, textWatcher)
                        this.setVariable(BR.coordinateWatcher, coordinateTextWatcher)
                        this.setVariable(BR.item, fieldUiModel)
                    },
                )
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
                )
            }

            UiRenderType.HORIZONTAL_CHECKBOXES,
            UiRenderType.VERTICAL_CHECKBOXES,
            -> {
                ProvideCheckBoxInput(
                    modifier = modifierWithFocus,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
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
    )
}

@Composable
private fun ProvideIntegerPositiveOrZero(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
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
    )
}

@Composable
private fun ProvidePercentage(
    modifier: Modifier = Modifier,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
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
    )
}

@Composable
private fun ProvideNumber(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
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
    )
}

@Composable
private fun ProvideIntegerNegative(
    modifier: Modifier = Modifier,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
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
    )
}

@Composable
private fun ProvideLongText(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
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
    )
}

@Composable
private fun ProvideLetter(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
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
    )
}

@Composable
private fun ProvideInteger(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
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
    )
}

@Composable
private fun ProvideEmail(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
    uiEventHandler: (RecyclerViewUiEvents) -> Unit,
    intentHandler: (FormIntent) -> Unit,
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
    )
}

@Composable
private fun ProvideInputPhoneNumber(
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    uiEventHandler: (RecyclerViewUiEvents) -> Unit,
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
    )
}

@Composable
private fun ProvideInputLink(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    uiEventHandler: (RecyclerViewUiEvents) -> Unit,

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
    )
}

private fun getFieldView(
    context: Context,
    inflater: LayoutInflater,
    viewgroup: ViewGroup,
    add: Boolean,
    layoutId: Int,
    needToForceUpdate: Boolean,
): ViewDataBinding {
    val layoutInflater =
        if (needToForceUpdate) {
            inflater.cloneInContext(
                ContextThemeWrapper(
                    context,
                    R.style.searchFormInputText,
                ),
            )
        } else {
            inflater.cloneInContext(
                ContextThemeWrapper(
                    context,
                    R.style.formInputText,
                ),
            )
        }

    return DataBindingUtil.inflate(layoutInflater, layoutId, viewgroup, add)
}
