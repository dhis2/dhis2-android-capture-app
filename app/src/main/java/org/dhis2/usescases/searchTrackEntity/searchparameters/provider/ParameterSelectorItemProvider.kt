package org.dhis2.usescases.searchTrackEntity.searchparameters.provider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    var inputTextValue: String? by remember(searchParameter.value) { mutableStateOf(null) }

    return ParameterSelectorItemModel(
        label = searchParameter.label,
        helper = searchParameter.helper,
        inputField = {
            InputText(
                title = searchParameter.label,
                state = InputShellState.UNFOCUSED,
                inputText = inputTextValue,
                inputStyle = InputStyle.ParameterInputStyle(),
                onValueChanged = {
                    inputTextValue = it
                    onValueChange(
                        searchParameter.uid,
                        it,
                    )
                },
            )
        },
        status = if (inputTextValue.isNullOrEmpty()) {
            ParameterSelectorItemModel.Status.CLOSED
        } else {
            ParameterSelectorItemModel.Status.OPENED
        },
    )
}
