package org.dhis2.tracker.search.ui.provider

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
import org.dhis2.tracker.ui.input.model.TrackerInputModel
import org.dhis2.tracker.ui.input.model.TrackerInputType
import org.dhis2.tracker.ui.input.model.TrackerInputUiEvent
import org.dhis2.tracker.ui.input.provider.TrackerInputProvider
import org.hisp.dhis.mobile.ui.designsystem.component.InputStyle
import org.hisp.dhis.mobile.ui.designsystem.component.parameter.model.ParameterSelectorItemModel
import org.hisp.dhis.mobile.ui.designsystem.resource.provideDHIS2Icon
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor

@Composable
fun provideParameterSelectorItem(
    inputModel: TrackerInputModel,
    helperText: String,
    onNextClicked: () -> Unit,
    onUiEvent: (TrackerInputUiEvent) -> Unit,
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
        icon = { ProvideParameterIcon(inputModel.valueType) },
        label = inputModel.label,
        helper = helperText,
        inputField = {
            TrackerInputProvider(
                modifier =
                    Modifier
                        .focusRequester(focusRequester),
                inputStyle = InputStyle.ParameterInputStyle(),
                inputModel = inputModel,
                onNextClicked = onNextClicked,
                onUiEvent = onUiEvent,
            )
        },
        status = status,
        onExpand = {
            onUiEvent(TrackerInputUiEvent.OnItemClick(inputModel.uid))
            when (inputModel.valueType) {
                TrackerInputType.QR_CODE,
                TrackerInputType.BAR_CODE,
                ->
                    onUiEvent(TrackerInputUiEvent.OnScanButtonClicked(inputModel.uid))

                else -> {}
            }
        },
    )
}

@Composable
private fun ProvideParameterIcon(valueType: TrackerInputType?) =
    when (valueType) {
        TrackerInputType.QR_CODE ->
            Icon(
                imageVector = Icons.Outlined.QrCode2,
                contentDescription = "QR Code Icon",
                tint = SurfaceColor.Primary,
            )

        TrackerInputType.BAR_CODE ->
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
