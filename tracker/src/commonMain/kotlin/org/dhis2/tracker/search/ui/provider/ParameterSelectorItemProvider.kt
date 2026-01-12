package org.dhis2.tracker.search.ui.provider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import org.dhis2.tracker.ui.input.model.TrackerInputModel
import org.dhis2.tracker.ui.input.model.TrackerInputType
import org.dhis2.tracker.ui.input.model.TrackerInputUiEvent
import org.dhis2.tracker.ui.input.provider.ParameterInputProvider
import org.dhis2.tracker.ui.input.provider.ProvideParameterIcon
import org.hisp.dhis.mobile.ui.designsystem.component.InputStyle
import org.hisp.dhis.mobile.ui.designsystem.component.parameter.model.ParameterSelectorItemModel

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
            ParameterInputProvider(
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
            inputModel.onItemClick()
            when (inputModel.valueType) {
                TrackerInputType.QR_CODE ->
                    onUiEvent(TrackerInputUiEvent.OnQRButtonClicked(inputModel.uid))
                TrackerInputType.BAR_CODE ->
                    onUiEvent(TrackerInputUiEvent.OnBarcodeButtonClicked(inputModel.uid))
                else -> {}
            }
        },
    )
}
