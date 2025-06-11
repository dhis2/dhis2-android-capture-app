package org.dhis2.form.ui.provider.inputfield

import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.dhis2.form.extensions.autocompleteList
import org.dhis2.form.extensions.inputState
import org.dhis2.form.extensions.legend
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.UiRenderType
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.intent.FormIntent
import org.hisp.dhis.mobile.ui.designsystem.component.CustomIntentState
import org.hisp.dhis.mobile.ui.designsystem.component.InputBarCode
import org.hisp.dhis.mobile.ui.designsystem.component.InputCustomIntent
import org.hisp.dhis.mobile.ui.designsystem.component.InputQRCode
import org.hisp.dhis.mobile.ui.designsystem.component.InputStyle
import org.hisp.dhis.mobile.ui.designsystem.component.InputText

@Composable
internal fun ProvideInputsForValueTypeText(
    modifier: Modifier = Modifier,
    inputStyle: InputStyle,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    uiEventHandler: (RecyclerViewUiEvents) -> Unit,
    focusManager: FocusManager,
    onNextClicked: () -> Unit,
) {
    when (fieldUiModel.renderingType) {
        UiRenderType.QR_CODE, UiRenderType.GS1_DATAMATRIX -> {
            ProvideQRInput(
                modifier = modifier,
                inputStyle = inputStyle,
                fieldUiModel = fieldUiModel,
                intentHandler = intentHandler,
                uiEventHandler = uiEventHandler,
                focusManager = focusManager,
                onNextClicked = onNextClicked,
            )
        }

        UiRenderType.BAR_CODE -> {
            ProvideBarcodeInput(
                modifier = modifier,
                inputStyle = inputStyle,
                fieldUiModel = fieldUiModel,
                intentHandler = intentHandler,
                uiEventHandler = uiEventHandler,
                focusManager = focusManager,
                onNextClicked = onNextClicked,
            )
        } else -> {
            if (fieldUiModel.uid == "customIntentId") {
                ProvideCustomIntentInput(
                    modifier = modifier,
                    inputStyle = inputStyle,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    focusManager = focusManager,
                    onNextClicked = onNextClicked,
                    uiEventHandler = uiEventHandler,
                )
            } else {
                ProvideDefaultTextInput(
                    modifier = modifier,
                    inputStyle = inputStyle,
                    fieldUiModel = fieldUiModel,
                    intentHandler = intentHandler,
                    focusManager = focusManager,
                    onNextClicked = onNextClicked,
                )
            }
        }
    }
}

@Composable
private fun ProvideQRInput(
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

    InputQRCode(
        modifier = modifier.fillMaxWidth(),
        title = fieldUiModel.label,
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        inputTextFieldValue = value,
        inputStyle = inputStyle,
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
        onQRButtonClicked = {
            if (value.text.isEmpty()) {
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
                        value = value.text,
                        renderingType = fieldUiModel.renderingType,
                        editable = fieldUiModel.editable,
                        label = fieldUiModel.label,
                    ),
                )
            }
        },
        autoCompleteList = fieldUiModel.autocompleteList(),
        autoCompleteItemSelected = {
            focusManager.clearFocus()
        },
    )
}

@Composable
private fun ProvideDefaultTextInput(
    modifier: Modifier,
    inputStyle: InputStyle = InputStyle.DataInputStyle(),
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    focusManager: FocusManager,
    onNextClicked: () -> Unit,
) {
    val textSelection = TextRange(fieldUiModel.value?.length ?: 0)
    var value by remember(fieldUiModel.value) {
        mutableStateOf(TextFieldValue(fieldUiModel.value ?: "", textSelection))
    }
    InputText(
        modifier = modifier.fillMaxWidth(),
        title = fieldUiModel.label,
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        inputTextFieldValue = value,
        inputStyle = inputStyle,
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
        onAutoCompleteItemSelected = {
            focusManager.clearFocus()
        },
    )
}

@Composable
private fun ProvideCustomIntentInput(
    modifier: Modifier,
    inputStyle: InputStyle = InputStyle.DataInputStyle(),
    fieldUiModel: FieldUiModel,
    uiEventHandler: (RecyclerViewUiEvents) -> Unit,
    intentHandler: (FormIntent) -> Unit,
    focusManager: FocusManager,
    onNextClicked: () -> Unit,
) {
    var values = remember() {
        fieldUiModel.value?.takeIf { it.isNotEmpty() }?.let { mutableStateListOf(it) }
            ?: mutableStateListOf()
    }
    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val content = result.data?.data.toString()
                values = mutableStateListOf(content)
                values.add(result.data?.getBooleanExtra("biometricsComplete", false).toString())
                result.data?.getStringExtra("identification")?.let {
                    values.add(it)
                }
                result.data?.getStringExtra("sessionId")?.let {
                    values.add(it)
                }
            }
        }

    val state by remember(values) {
        mutableStateOf(if (values.isNotEmpty()) CustomIntentState.LOADED else CustomIntentState.LAUNCH)
    }
    InputCustomIntent(
        title = "Custom Intent sample",
        buttonText = "launch",
        onLaunch = {
            val intentData = Intent("packageName").apply {
                putExtra("projectId", "projectId")
                putExtra("moduleId", "testDhisModuleId")
                putExtra("userId", "testDhisUserId")
                putExtra("sessionId", "testDhisSessionId")
            }
            val intent = Intent.createChooser(
                intentData,
                "Custom intent!",
            )
            launcher.launch(intent)
        },
        onClear = {},
        customIntentState = state,
        values = values.toList(),
    )
}

@Composable
private fun ProvideBarcodeInput(
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

    InputBarCode(
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
        onActionButtonClicked = {
            if (value.text.isEmpty()) {
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
                        value = value.text,
                        renderingType = fieldUiModel.renderingType,
                        editable = fieldUiModel.editable,
                        label = fieldUiModel.label,
                    ),
                )
            }
        },
        autoCompleteList = fieldUiModel.autocompleteList(),
        autoCompleteItemSelected = {
            focusManager.clearFocus()
        },
    )
}
