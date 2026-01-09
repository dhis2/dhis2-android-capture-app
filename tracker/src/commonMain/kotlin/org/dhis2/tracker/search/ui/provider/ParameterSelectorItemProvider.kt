package org.dhis2.tracker.search.ui.provider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import org.dhis2.tracker.search.ui.model.ParameterInputModel
import org.dhis2.tracker.search.ui.model.ParameterRenderingType
import org.hisp.dhis.mobile.ui.designsystem.component.InputStyle
import org.hisp.dhis.mobile.ui.designsystem.component.parameter.model.ParameterSelectorItemModel

@Composable
fun provideParameterSelectorItem(
    inputModel: ParameterInputModel,
    helperText: String,
    onNextClicked: () -> Unit,
    onQRScanRequest: (() -> Unit)? = null,
    onBarcodeScanRequest: (() -> Unit)? = null,
): ParameterSelectorItemModel {
    val focusRequester = remember { FocusRequester() }

    val status =
        if (inputModel.focused) {
            ParameterSelectorItemModel.Status.FOCUSED
        } else if (inputModel.value.isNullOrEmpty()) {
            ParameterSelectorItemModel.Status.CLOSED
        } else {
            ParameterSelectorItemModel.Status.UNFOCUSED
        }

    LaunchedEffect(key1 = status) {
        if (status == ParameterSelectorItemModel.Status.FOCUSED) {
            focusRequester.requestFocus()
        }
    }

    return ParameterSelectorItemModel(
        icon = { ProvideParameterIcon(inputModel.valueType, inputModel.renderingType) },
        label = inputModel.label,
        helper = helperText,
        inputField = {
            ParameterInputProvider(
                modifier =
                    Modifier
                        .focusRequester(focusRequester),
                inputStyle = InputStyle.ParameterInputStyle(),
                inputModel = inputModel,
                onNextClicked = onNextClicked,
            )
        },
        status = status,
        onExpand = {
            inputModel.onItemClick()
            when (inputModel.renderingType) {
                ParameterRenderingType.QR_CODE,
                ParameterRenderingType.GS1_DATAMATRIX,
                -> onQRScanRequest?.invoke()
                ParameterRenderingType.BAR_CODE -> onBarcodeScanRequest?.invoke()
                else -> {}
            }
        },
    )
}
