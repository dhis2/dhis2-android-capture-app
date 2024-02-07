package org.dhis2.usescases.searchTrackEntity.searchparameters.provider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    var value by remember(searchParameter.value) {
        mutableStateOf(TextFieldValue(searchParameter.value ?: ""))
    }

    return ParameterSelectorItemModel(
        label = searchParameter.label,
        helper = searchParameter.helper,
        inputField = {
            InputText(
                title = searchParameter.label,
                state = InputShellState.UNFOCUSED,
                inputTextFieldValue = value,
                inputStyle = InputStyle.ParameterInputStyle(),
                onValueChanged = {
                    value = it ?: TextFieldValue()
                    onValueChange(
                        searchParameter.uid,
                        value.text,
                    )
                },
            )
        },
        status = if (value.text.isEmpty()) {
            ParameterSelectorItemModel.Status.CLOSED
        } else {
            ParameterSelectorItemModel.Status.OPENED
        },
    )
}
