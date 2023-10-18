package org.dhis2.form.ui.provider.inputfield

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.dhis2.form.extensions.inputState
import org.dhis2.form.extensions.legend
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.UiRenderType
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.intent.FormIntent
import org.hisp.dhis.mobile.ui.designsystem.component.InputBarCode
import org.hisp.dhis.mobile.ui.designsystem.component.InputQRCode
import org.hisp.dhis.mobile.ui.designsystem.component.InputText

@Composable
internal fun ProvideInputsForValueTypeText(
    modifier: Modifier = Modifier,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    uiEventHandler: (RecyclerViewUiEvents) -> Unit,
) {
    when (fieldUiModel.renderingType) {
        UiRenderType.QR_CODE -> {
            ProvideQRInput(
                modifier = modifier,
                fieldUiModel = fieldUiModel,
                intentHandler = intentHandler,
                uiEventHandler = uiEventHandler,
            )
        }
        UiRenderType.BAR_CODE -> {
            ProvideBarcodeInput(
                modifier = modifier,
                fieldUiModel = fieldUiModel,
                intentHandler = intentHandler,
                uiEventHandler = uiEventHandler,
            )
        } else -> {
            ProvideDefaultTextInput(
                modifier = modifier,
                fieldUiModel = fieldUiModel,
                intentHandler = intentHandler,
            )
        }
    }
}

@Composable
private fun ProvideQRInput(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    uiEventHandler: (RecyclerViewUiEvents) -> Unit,
) {
    var value by remember(fieldUiModel.value) {
        mutableStateOf(fieldUiModel.value)
    }

    InputQRCode(
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
        onQRButtonClicked = {
            if (value.isNullOrEmpty()) {
                uiEventHandler.invoke(
                    RecyclerViewUiEvents.ScanQRCode(
                        fieldUiModel.uid,
                        optionSet = fieldUiModel.optionSet,
                        fieldUiModel.renderingType,
                    ),
                )
            } else {
                uiEventHandler.invoke(
                    RecyclerViewUiEvents.DisplayQRCode(
                        fieldUiModel.uid,
                        optionSet = fieldUiModel.optionSet,
                        value = value!!,
                        renderingType = fieldUiModel.renderingType,
                        editable = fieldUiModel.editable,
                    ),
                )
            }
        },
    )
}

@Composable
private fun ProvideDefaultTextInput(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
) {
    var value by remember {
        mutableStateOf(fieldUiModel.value)
    }
    InputText(
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
private fun ProvideBarcodeInput(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    uiEventHandler: (RecyclerViewUiEvents) -> Unit,
) {
    var value by remember(fieldUiModel.value) {
        mutableStateOf(fieldUiModel.value)
    }

    InputBarCode(
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
        onActionButtonClicked = {
            if (value.isNullOrEmpty()) {
                uiEventHandler.invoke(
                    RecyclerViewUiEvents.ScanQRCode(
                        fieldUiModel.uid,
                        optionSet = fieldUiModel.optionSet,
                        fieldUiModel.renderingType,
                    ),
                )
            } else {
                uiEventHandler.invoke(
                    RecyclerViewUiEvents.DisplayQRCode(
                        fieldUiModel.uid,
                        optionSet = fieldUiModel.optionSet,
                        value = value!!,
                        renderingType = fieldUiModel.renderingType,
                        editable = fieldUiModel.editable,
                    ),
                )
            }
        },
    )
}
