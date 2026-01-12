package org.dhis2.tracker.search.ui.provider

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import org.dhis2.tracker.search.ui.model.ParameterInputModel
import org.dhis2.tracker.search.ui.model.ParameterInputType
import org.hisp.dhis.mobile.ui.designsystem.component.InputEmail
import org.hisp.dhis.mobile.ui.designsystem.component.InputInteger
import org.hisp.dhis.mobile.ui.designsystem.component.InputLetter
import org.hisp.dhis.mobile.ui.designsystem.component.InputLink
import org.hisp.dhis.mobile.ui.designsystem.component.InputLongText
import org.hisp.dhis.mobile.ui.designsystem.component.InputNegativeInteger
import org.hisp.dhis.mobile.ui.designsystem.component.InputNotSupported
import org.hisp.dhis.mobile.ui.designsystem.component.InputNumber
import org.hisp.dhis.mobile.ui.designsystem.component.InputPercentage
import org.hisp.dhis.mobile.ui.designsystem.component.InputPhoneNumber
import org.hisp.dhis.mobile.ui.designsystem.component.InputPositiveInteger
import org.hisp.dhis.mobile.ui.designsystem.component.InputPositiveIntegerOrZero
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.component.InputStyle
import org.hisp.dhis.mobile.ui.designsystem.component.InputText
import org.hisp.dhis.mobile.ui.designsystem.resource.provideDHIS2Icon
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor

@Composable
fun ParameterInputProvider(
    modifier: Modifier = Modifier,
    inputModel: ParameterInputModel,
    inputStyle: InputStyle = InputStyle.ParameterInputStyle(),
    onNextClicked: () -> Unit,
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
        ParameterInputType.TEXT -> {
            InputText(
                modifier = modifierWithFocus.fillMaxWidth(),
                title = inputModel.label,
                state = InputShellState.UNFOCUSED,
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

        ParameterInputType.LONG_TEXT -> {
            InputLongText(
                modifier = modifierWithFocus.fillMaxWidth(),
                title = inputModel.label,
                state = InputShellState.UNFOCUSED,
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

        ParameterInputType.LETTER -> {
            InputLetter(
                modifier = modifierWithFocus.fillMaxWidth(),
                title = inputModel.label,
                state = InputShellState.UNFOCUSED,
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

        ParameterInputType.EMAIL -> {
            InputEmail(
                modifier = modifierWithFocus.fillMaxWidth(),
                title = inputModel.label,
                state = InputShellState.UNFOCUSED,
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

        ParameterInputType.PHONE_NUMBER -> {
            InputPhoneNumber(
                modifier = modifierWithFocus.fillMaxWidth(),
                title = inputModel.label,
                state = InputShellState.UNFOCUSED,
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

        ParameterInputType.URL -> {
            InputLink(
                modifier = modifierWithFocus.fillMaxWidth(),
                title = inputModel.label,
                state = InputShellState.UNFOCUSED,
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

        ParameterInputType.NUMBER -> {
            InputNumber(
                modifier = modifierWithFocus.fillMaxWidth(),
                title = inputModel.label,
                state = InputShellState.UNFOCUSED,
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

        ParameterInputType.INTEGER -> {
            InputInteger(
                modifier = modifierWithFocus.fillMaxWidth(),
                title = inputModel.label,
                state = InputShellState.UNFOCUSED,
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

        ParameterInputType.INTEGER_POSITIVE -> {
            InputPositiveInteger(
                modifier = modifierWithFocus.fillMaxWidth(),
                title = inputModel.label,
                state = InputShellState.UNFOCUSED,
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

        ParameterInputType.INTEGER_NEGATIVE -> {
            InputNegativeInteger(
                modifier = modifierWithFocus.fillMaxWidth(),
                title = inputModel.label,
                state = InputShellState.UNFOCUSED,
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

        ParameterInputType.INTEGER_ZERO_OR_POSITIVE -> {
            InputPositiveIntegerOrZero(
                modifier = modifierWithFocus.fillMaxWidth(),
                title = inputModel.label,
                state = InputShellState.UNFOCUSED,
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

        ParameterInputType.PERCENTAGE -> {
            InputPercentage(
                modifier = modifierWithFocus.fillMaxWidth(),
                title = inputModel.label,
                state = InputShellState.UNFOCUSED,
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

        ParameterInputType.UNIT_INTERVAL -> TODO()
        ParameterInputType.AGE -> TODO()
        ParameterInputType.ORGANISATION_UNIT -> TODO()
        ParameterInputType.MULTI_SELECTION -> TODO()
        ParameterInputType.QR_CODE -> {
            TODO()
        }

        ParameterInputType.BAR_CODE -> {
            TODO()
        }

        ParameterInputType.CHECKBOX -> TODO()
        ParameterInputType.RADIO_BUTTON -> TODO()
        ParameterInputType.YES_ONLY_SWITCH -> TODO()
        ParameterInputType.YES_ONLY_CHECKBOX -> TODO()
        ParameterInputType.DATE_TIME -> TODO()
        ParameterInputType.PERIOD_SELECTOR -> TODO()
        ParameterInputType.MATRIX -> TODO()
        ParameterInputType.SEQUENTIAL -> TODO()
        ParameterInputType.DROPDOWN -> TODO()

        ParameterInputType.NOT_SUPPORTED -> {
            InputNotSupported(
                modifier = modifierWithFocus.fillMaxWidth(),
                title = inputModel.label,
                inputStyle = inputStyle,
            )
        }
    }
}

@Composable
fun ProvideParameterIcon(valueType: ParameterInputType?) =
    when (valueType) {
        ParameterInputType.QR_CODE ->
            Icon(
                imageVector = Icons.Outlined.QrCode2,
                contentDescription = "QR Code Icon",
                tint = SurfaceColor.Primary,
            )

        ParameterInputType.BAR_CODE ->
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
