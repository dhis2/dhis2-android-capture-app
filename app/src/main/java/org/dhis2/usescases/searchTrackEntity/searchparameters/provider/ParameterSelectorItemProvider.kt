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
fun provideParameterSelectorItem(searchParameter: SearchParameter): ParameterSelectorItemModel {
    var inputTextValue1: String? by remember { mutableStateOf(null) }

    return ParameterSelectorItemModel(
        label = "Label",
        helper = "Optional",
        inputField = {
            InputText(
                title = "Label",
                state = InputShellState.UNFOCUSED,
                inputText = inputTextValue1,
                inputStyle = InputStyle.ParameterInputStyle(),
                onValueChanged = {
                    inputTextValue1 = it
                },
            )
        },
        status = if (inputTextValue1.isNullOrEmpty()) {
            ParameterSelectorItemModel.Status.CLOSED
        } else {
            ParameterSelectorItemModel.Status.OPENED
        },
    )
}
