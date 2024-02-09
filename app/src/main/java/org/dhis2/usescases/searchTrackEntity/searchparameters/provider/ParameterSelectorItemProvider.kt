package org.dhis2.usescases.searchTrackEntity.searchparameters.provider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.dhis2.usescases.searchTrackEntity.searchparameters.model.SearchParameter
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.component.InputStyle
import org.hisp.dhis.mobile.ui.designsystem.component.InputText
import org.hisp.dhis.mobile.ui.designsystem.component.parameter.model.ParameterSelectorItemModel

@Composable
fun provideParameterSelectorItem(
    searchParameter: SearchParameter,
    onValueChange: (uid: String, value: String?) -> Unit,
): ParameterSelectorItemModel {
    val focusRequester = remember { FocusRequester() }

    val textSelection = TextRange(searchParameter.value?.length ?: 0)
    var value by remember(searchParameter.value) {
        mutableStateOf(TextFieldValue(searchParameter.value ?: "", textSelection))
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
        label = searchParameter.label,
        helper = searchParameter.helper,
        inputField = {
            ProvideInputField(
                modifier = modifierWithFocus,
                searchParameter = searchParameter,
                status = status,
                value = value,
                onValueChange = {
                    value = it ?: TextFieldValue()
                    onValueChange(
                        searchParameter.uid,
                        value.text,
                    )
                },
            )
        },
        status = status,
        onExpand = {
            status = ParameterSelectorItemModel.Status.UNFOCUSED
        },
    )
}

@Composable
fun ProvideInputField(
    modifier: Modifier,
    searchParameter: SearchParameter,
    status: ParameterSelectorItemModel.Status,
    value: TextFieldValue?,
    onValueChange: (value: TextFieldValue?) -> Unit,

) {
    InputText(
        modifier = modifier,
        title = searchParameter.label,
        state = when (status) {
            ParameterSelectorItemModel.Status.FOCUSED -> InputShellState.FOCUSED
            else -> InputShellState.UNFOCUSED
        },
        inputTextFieldValue = value,
        inputStyle = InputStyle.ParameterInputStyle(),
        onValueChanged = {
            onValueChange.invoke(it)
        },
    )
}
