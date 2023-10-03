package org.dhis2.form.ui.provider

import android.content.Context
import android.content.res.Resources
import android.text.TextWatcher
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import org.dhis2.form.BR
import org.dhis2.form.R
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.UiRenderType
import org.dhis2.form.ui.LatitudeLongitudeTextWatcher
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.intent.FormIntent
import org.dhis2.form.ui.provider.inputfield.ProvideCheckBoxInput
import org.dhis2.form.ui.provider.inputfield.ProvideRadioButtonInput
import org.dhis2.form.ui.provider.inputfield.ProvideYesNoCheckBoxInput
import org.dhis2.form.ui.provider.inputfield.ProvideYesNoRadioButtonInput
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.mobile.ui.designsystem.component.InputInteger
import org.hisp.dhis.mobile.ui.designsystem.component.InputLetter
import org.hisp.dhis.mobile.ui.designsystem.component.InputLongText
import org.hisp.dhis.mobile.ui.designsystem.component.InputNegativeInteger
import org.hisp.dhis.mobile.ui.designsystem.component.InputNumber
import org.hisp.dhis.mobile.ui.designsystem.component.InputPercentage
import org.hisp.dhis.mobile.ui.designsystem.component.InputPositiveInteger
import org.hisp.dhis.mobile.ui.designsystem.component.InputPositiveIntegerOrZero
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.component.InputText
import org.hisp.dhis.mobile.ui.designsystem.component.LegendData
import org.hisp.dhis.mobile.ui.designsystem.component.internal.RegExValidations

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
    if (fieldUiModel.optionSet == null) {
        when (fieldUiModel.valueType) {
            ValueType.TEXT -> {
                ProvideInputText(
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                )
            }

            ValueType.INTEGER_POSITIVE -> {
                ProvideIntegerPositive(
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                )
            }

            ValueType.INTEGER_ZERO_OR_POSITIVE -> {
                ProvideIntegerPositiveOrZero(
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                )
            }

            ValueType.PERCENTAGE -> {
                ProvidePercentage(
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                )
            }

            ValueType.NUMBER -> {
                ProvideNumber(
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                )
            }

            ValueType.INTEGER_NEGATIVE -> {
                ProvideIntegerNegative(
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                )
            }

            ValueType.LONG_TEXT -> {
                ProvideLongText(
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                )
            }

            ValueType.LETTER -> {
                ProvideLetter(
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                )
            }

            ValueType.INTEGER -> {
                ProvideInteger(
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                )
            }

            ValueType.BOOLEAN -> {
                when (fieldUiModel.renderingType) {
                    UiRenderType.HORIZONTAL_CHECKBOXES,
                    UiRenderType.VERTICAL_CHECKBOXES,
                    -> {
                        ProvideYesNoCheckBoxInput(
                            fieldUiModel = fieldUiModel,
                            resources = resources,
                        )
                    }

                    else -> {
                        ProvideYesNoRadioButtonInput(
                            fieldUiModel = fieldUiModel,
                            resources = resources,
                        )
                    }
                }
            }

            ValueType.TRUE_ONLY -> {
                // TODO
                /*When
                ValueTypeRenderingType.TOGGLE -> Yesonlyswitch
                ValueTypeRenderingType.HORIZONTAL_CHECKBOXES,
                ValueTypeRenderingType.VERTICAL_CHECKBOXES -> Yesonlycheckbox

                 else -> YesonlyRadioButton?? exists?*/
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
                    fieldUiModel = fieldUiModel,
                )
            }

            UiRenderType.HORIZONTAL_CHECKBOXES,
            UiRenderType.VERTICAL_CHECKBOXES,
            -> {
                ProvideCheckBoxInput(
                    fieldUiModel = fieldUiModel,
                )
            }

            // TODO ("Remaining option sets")

            else -> { // TODO (Remove when all optionsets)
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
    }
}

@Composable
private fun ProvideInputText(
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
) {
    var value by remember {
        mutableStateOf(fieldUiModel.value)
    }
    InputText(
        modifier = Modifier.fillMaxWidth(),
        title = fieldUiModel.label,
        state = when {
            fieldUiModel.error != null -> InputShellState.ERROR
            !fieldUiModel.editable -> InputShellState.DISABLED
            fieldUiModel.focused -> InputShellState.FOCUSED
            else -> InputShellState.UNFOCUSED
        },
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend?.let {
            LegendData(Color(it.color), it.label ?: "", null)
        },
        inputText = value ?: "",
        onValueChanged = {
            value = it
            intentHandler(
                FormIntent.OnSave(
                    fieldUiModel.uid,
                    value,
                    fieldUiModel.valueType,
                ),
            )
        },
    )
}

@Composable
private fun ProvideIntegerPositive(
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
) {
    var value by remember {
        mutableStateOf(fieldUiModel.value)
    }

    InputPositiveInteger(
        modifier = Modifier.fillMaxWidth(),
        title = fieldUiModel.label,
        state = when {
            fieldUiModel.error != null -> InputShellState.ERROR
            !fieldUiModel.editable -> InputShellState.DISABLED
            fieldUiModel.focused -> InputShellState.FOCUSED
            else -> InputShellState.UNFOCUSED
        },
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend?.let {
            LegendData(Color(it.color), it.label ?: "", null)
        },
        inputText = value ?: "",
        onValueChanged = {
            value = it
            intentHandler(
                FormIntent.OnSave(
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
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
) {
    var value by remember {
        mutableStateOf(fieldUiModel.value)
    }

    InputPositiveIntegerOrZero(
        modifier = Modifier.fillMaxWidth(),
        title = fieldUiModel.label,
        state = when {
            fieldUiModel.error != null -> InputShellState.ERROR
            !fieldUiModel.editable -> InputShellState.DISABLED
            fieldUiModel.focused -> InputShellState.FOCUSED
            else -> InputShellState.UNFOCUSED
        },
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend?.let {
            LegendData(Color(it.color), it.label ?: "", null)
        },
        inputText = value ?: "",
        onValueChanged = {
            value = it
            intentHandler(
                FormIntent.OnSave(
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
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
) {
    var value by remember {
        mutableStateOf(fieldUiModel.value)
    }

    InputPercentage(
        modifier = Modifier.fillMaxWidth(),
        title = fieldUiModel.label,
        state = when {
            fieldUiModel.error != null -> InputShellState.ERROR
            !fieldUiModel.editable -> InputShellState.DISABLED
            fieldUiModel.focused -> InputShellState.FOCUSED
            else -> InputShellState.UNFOCUSED
        },
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend?.let {
            LegendData(Color(it.color), it.label ?: "", null)
        },
        inputText = value ?: "",
        onValueChanged = {
            value = it
            intentHandler(
                FormIntent.OnSave(
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
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
) {
    var value by remember {
        mutableStateOf(fieldUiModel.value)
    }

    InputNumber(
        modifier = Modifier.fillMaxWidth(),
        title = fieldUiModel.label,
        state = when {
            fieldUiModel.error != null -> InputShellState.ERROR
            !fieldUiModel.editable -> InputShellState.DISABLED
            fieldUiModel.focused -> InputShellState.FOCUSED
            else -> InputShellState.UNFOCUSED
        },
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend?.let {
            LegendData(Color(it.color), it.label ?: "", null)
        },
        inputText = value ?: "",
        onValueChanged = {
            value = it
            intentHandler(
                FormIntent.OnSave(
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
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
) {
    var value by remember {
        mutableStateOf(fieldUiModel.value?.replace("-", ""))
    }

    InputNegativeInteger(
        modifier = Modifier.fillMaxWidth(),
        title = fieldUiModel.label,
        state = when {
            fieldUiModel.error != null -> InputShellState.ERROR
            !fieldUiModel.editable -> InputShellState.DISABLED
            fieldUiModel.focused -> InputShellState.FOCUSED
            else -> InputShellState.UNFOCUSED
        },
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend?.let {
            LegendData(Color(it.color), it.label ?: "", null)
        },
        inputText = value ?: "",
        onValueChanged = {
            value = it
            intentHandler(
                FormIntent.OnSave(
                    fieldUiModel.uid,
                    "-$value",
                    fieldUiModel.valueType,
                ),
            )
        },
    )
}

@Composable
private fun ProvideLongText(
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
) {
    var value by remember {
        mutableStateOf(fieldUiModel.value)
    }

    InputLongText(
        modifier = Modifier.fillMaxWidth(),
        title = fieldUiModel.label,
        state = when {
            fieldUiModel.error != null -> InputShellState.ERROR
            !fieldUiModel.editable -> InputShellState.DISABLED
            fieldUiModel.focused -> InputShellState.FOCUSED
            else -> InputShellState.UNFOCUSED
        },
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend?.let {
            LegendData(Color(it.color), it.label ?: "", null)
        },
        inputText = value ?: "",
        onValueChanged = {
            value = it
            intentHandler(
                FormIntent.OnSave(
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
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
) {
    var value by remember {
        mutableStateOf(fieldUiModel.value)
    }

    InputLetter(
        modifier = Modifier.fillMaxWidth(),
        title = fieldUiModel.label,
        state = when {
            fieldUiModel.error != null -> InputShellState.ERROR
            !fieldUiModel.editable -> InputShellState.DISABLED
            fieldUiModel.focused -> InputShellState.FOCUSED
            else -> InputShellState.UNFOCUSED
        },
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend?.let {
            LegendData(Color(it.color), it.label ?: "", null)
        },
        inputText = value ?: "",
        onValueChanged = {
            value = it
            intentHandler(
                FormIntent.OnSave(
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
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
) {
    var value by remember {
        mutableStateOf(fieldUiModel.value)
    }

    InputInteger(
        modifier = Modifier.fillMaxWidth(),
        title = fieldUiModel.label,
        state = when {
            fieldUiModel.error != null -> InputShellState.ERROR
            !fieldUiModel.editable -> InputShellState.DISABLED
            fieldUiModel.focused -> InputShellState.FOCUSED
            else -> InputShellState.UNFOCUSED
        },
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend?.let {
            LegendData(Color(it.color), it.label ?: "", null)
        },
        inputText = value ?: "",
        onValueChanged = {
            value = it
            intentHandler(
                FormIntent.OnSave(
                    fieldUiModel.uid,
                    value,
                    fieldUiModel.valueType,
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
