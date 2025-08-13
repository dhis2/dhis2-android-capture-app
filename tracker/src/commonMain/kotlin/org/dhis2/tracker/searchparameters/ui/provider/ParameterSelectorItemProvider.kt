package org.dhis2.tracker.searchparameters.ui.provider

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import org.dhis2.mobile.commons.input.InputType
import org.dhis2.mobile.commons.input.InputUiState
import org.dhis2.mobile.commons.input.UiAction
import org.dhis2.mobile.commons.input.provider.ProvideInputField
import org.dhis2.mobile.tracker.resources.Res
import org.dhis2.mobile.tracker.resources.optional
import org.hisp.dhis.mobile.ui.designsystem.component.parameter.model.ParameterSelectorItemModel
import org.hisp.dhis.mobile.ui.designsystem.resource.provideDHIS2Icon
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.jetbrains.compose.resources.stringResource

@Composable
fun provideParameterSelectorItem(
    inputUiState: InputUiState,
    onAction: (UiAction) -> Unit,
): ParameterSelectorItemModel {
    val focusRequester = remember { FocusRequester() }

    val status = if (inputUiState.focused) {
        ParameterSelectorItemModel.Status.FOCUSED
    } else if (inputUiState.value.isNullOrEmpty()) {
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
        icon = {
            ProvideIcon(inputUiState.inputType)
        },
        label = inputUiState.label,
        helper = stringResource(Res.string.optional),
        inputField = {
            ProvideInputField(
                modifier = Modifier
                    .focusRequester(focusRequester),
                inputData = inputUiState,
                onAction = onAction,
            )
        },
        status = status,
        onExpand = {
            performOnExpandActions(inputUiState, onAction)
        },
    )
}

private fun performOnExpandActions(inputUiState: InputUiState, onAction: (UiAction) -> Unit) {
    when (inputUiState.inputType) {
        is InputType.Barcode -> {
            onAction(
                UiAction.OnBarCodeScan(
                    id = inputUiState.id,
                    optionSet = null,
                ),
            )
        }

        is InputType.QRCode -> {
            onAction(
                UiAction.OnQRCodeScan(
                    id = inputUiState.id,
                    optionSet = null,
                ),
            )
        }

        else -> {
            // no-op
        }
    }
}

@Composable
private fun ProvideIcon(
    inputType: InputType,
) =
    when (inputType) {
        is InputType.QRCode -> {
            Icon(
                imageVector = Icons.Outlined.QrCode2,
                contentDescription = "Icon Button",
                tint = SurfaceColor.Primary,
            )
        }

        is InputType.Barcode -> {
            Icon(
                painter = provideDHIS2Icon("material_barcode_scanner"),
                contentDescription = "Icon Button",
                tint = SurfaceColor.Primary,
            )
        }

        else -> Icon(
            imageVector = Icons.Outlined.AddCircleOutline,
            contentDescription = "Icon Button",
            tint = SurfaceColor.Primary,
        )
    }
