package org.dhis2.tracker.search.ui.provider

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.dhis2.tracker.ui.input.model.TrackerInputModel
import org.dhis2.tracker.ui.input.model.inputState
import org.dhis2.tracker.ui.input.model.supportingText
import org.hisp.dhis.mobile.ui.designsystem.component.InputRadioButton
import org.hisp.dhis.mobile.ui.designsystem.component.InputStyle
import org.hisp.dhis.mobile.ui.designsystem.component.Orientation
import org.hisp.dhis.mobile.ui.designsystem.component.RadioButtonData

@Composable
fun TrackerRadioButtonInputProvider(
    model: TrackerInputModel,
    inputStyle: InputStyle,
    modifier: Modifier,
) {
    val dataMap =
        buildMap {
            model.optionSetConfiguration?.options.let { optionDataList ->
                optionDataList?.forEach { optionData ->
                    put(
                        optionData.code,
                        RadioButtonData(
                            uid = optionData.code,
                            selected = model.value == optionData.code,
                            enabled = true,
                            textInput = optionData.displayName,
                        ),
                    )
                }
            }
        }

    val (codeList, data) = dataMap.toList().unzip()

    InputRadioButton(
        modifier = modifier,
        inputStyle = inputStyle,
        title = model.label,
        radioButtonData = data,
        orientation = Orientation.VERTICAL,
        state = model.inputState(),
        supportingText = model.supportingText(),
        legendData = model.legend,
        isRequired = model.mandatory,
        itemSelected = data.find { it.selected },
        onItemChange = { item ->
            if (item != null) {
                val selectedIndex = data.indexOf(item)
                model.onValueChange(codeList[selectedIndex])
            } else {
                model.onValueChange(null)
            }
        },
    )
}
