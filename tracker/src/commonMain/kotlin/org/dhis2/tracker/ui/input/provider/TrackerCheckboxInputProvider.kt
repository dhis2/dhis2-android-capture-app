package org.dhis2.tracker.ui.input.provider

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.dhis2.tracker.ui.input.model.TrackerInputModel
import org.dhis2.tracker.ui.input.model.inputState
import org.dhis2.tracker.ui.input.model.supportingText
import org.hisp.dhis.mobile.ui.designsystem.component.CheckBoxData
import org.hisp.dhis.mobile.ui.designsystem.component.InputCheckBox
import org.hisp.dhis.mobile.ui.designsystem.component.InputStyle

@Composable
fun TrackerCheckboxInputProvider(
    model: TrackerInputModel,
    inputStyle: InputStyle,
    modifier: Modifier,
    onValueChange: (String?) -> Unit,
) {
    val dataMap =
        buildMap {
            model.optionSetConfiguration?.options.let { optionDataList ->
                optionDataList?.forEach { optionData ->
                    put(
                        optionData.code,
                        CheckBoxData(
                            uid = optionData.code,
                            checked = model.value == optionData.code,
                            enabled = true,
                            textInput = optionData.displayName,
                        ),
                    )
                }
            }
        }

    val (codeList, data) = dataMap.toList().unzip()

    InputCheckBox(
        modifier = modifier,
        inputStyle = inputStyle,
        title = model.label,
        checkBoxData = data,
        orientation = model.orientation,
        state = model.inputState(),
        supportingText = model.supportingText(),
        legendData = model.legend,
        isRequired = model.mandatory,
        onItemChange = { item ->
            val selectedIndex = data.indexOf(item)
            onValueChange(if (item.checked) null else codeList[selectedIndex])
        },
        onClearSelection = {
            onValueChange(null)
        },
    )
}
