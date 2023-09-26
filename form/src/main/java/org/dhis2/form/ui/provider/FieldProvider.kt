package org.dhis2.form.ui.provider

import android.content.Context
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
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import org.dhis2.form.BR
import org.dhis2.form.R
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.ui.LatitudeLongitudeTextWatcher
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.intent.FormIntent
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.component.InputText
import org.hisp.dhis.mobile.ui.designsystem.component.LegendData
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextData
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextState

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
) {
    when {
        fieldUiModel.optionSet == null && fieldUiModel.valueType == ValueType.TEXT -> {
            ProvideInputText(fieldUiModel, intentHandler, uiEventHandler)
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
}

@Composable
private fun ProvideInputText(
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    uiEventHandler: (RecyclerViewUiEvents) -> Unit,
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
        supportingText = mutableListOf<SupportingTextData>().apply {
            fieldUiModel.error?.let {
                add(
                    SupportingTextData(
                        it,
                        SupportingTextState.ERROR,
                    ),
                )
            }
            fieldUiModel.warning?.let {
                add(
                    SupportingTextData(
                        it,
                        SupportingTextState.WARNING,
                    ),
                )
            }
            fieldUiModel.description?.let {
                add(
                    SupportingTextData(
                        it,
                        SupportingTextState.DEFAULT,
                    ),
                )
            }
        },
        legendData = fieldUiModel.legend?.let {
            LegendData(Color(it.color), it.label ?: "", null)
        },
        inputText = value ?: "",
        onNextClicked = {
            intentHandler.invoke(FormIntent.OnNext(fieldUiModel.uid, value))
        },
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
