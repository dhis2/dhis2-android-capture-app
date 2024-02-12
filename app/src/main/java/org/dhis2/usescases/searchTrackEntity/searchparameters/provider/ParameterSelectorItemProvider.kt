package org.dhis2.usescases.searchTrackEntity.searchparameters.provider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.ui.provider.inputfield.FieldProvider
import org.hisp.dhis.mobile.ui.designsystem.component.parameter.model.ParameterSelectorItemModel

@Composable
fun provideParameterSelectorItem(
    resources: ResourceManager,
    focusManager: FocusManager,
    fieldUiModel: FieldUiModel,
    callback: FieldUiModel.Callback,
): ParameterSelectorItemModel {
    val focusRequester = remember { FocusRequester() }

    val textSelection = TextRange(fieldUiModel.value?.length ?: 0)
    val value by remember(fieldUiModel.value) {
        mutableStateOf(TextFieldValue(fieldUiModel.value ?: "", textSelection))
    }

    var status by remember {
        mutableStateOf(
            if (value.text.isEmpty()) {
                ParameterSelectorItemModel.Status.CLOSED
            } else {
                ParameterSelectorItemModel.Status.UNFOCUSED
            },
        )
    }

    val modifierWithFocus = Modifier
        .focusRequester(focusRequester)
        .onFocusChanged {
            status = if (it.isFocused) {
                ParameterSelectorItemModel.Status.FOCUSED
            } else {
                ParameterSelectorItemModel.Status.UNFOCUSED
            }
        }

    return ParameterSelectorItemModel(
        label = fieldUiModel.label,
        helper = "Optional",
        inputField = {
            FieldProvider(
                modifier = modifierWithFocus,
                fieldUiModel = fieldUiModel,
                uiEventHandler = callback::recyclerViewUiEvents,
                intentHandler = callback::intent,
                resources = resources,
                focusManager = focusManager,
                onNextClicked = { },
            )
        },
        status = status,
        onExpand = {
            status = ParameterSelectorItemModel.Status.UNFOCUSED
        },
    )
}
