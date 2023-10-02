package org.dhis2.form.ui.provider

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.UiRenderType
import org.dhis2.form.ui.intent.FormIntent
import org.hisp.dhis.mobile.ui.designsystem.component.InputRadioButton
import org.hisp.dhis.mobile.ui.designsystem.component.LegendData
import org.hisp.dhis.mobile.ui.designsystem.component.Orientation
import org.hisp.dhis.mobile.ui.designsystem.component.RadioButtonData

@Composable
internal fun ProvideRadioButtonInput(
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
) {
    val orientation = if (fieldUiModel.renderingType == UiRenderType.VERTICAL_RADIOBUTTONS) {
        Orientation.VERTICAL
    } else {
        Orientation.HORIZONTAL
    }

    val data = fieldUiModel.optionSetConfiguration?.optionsToDisplay()?.map { option ->
        RadioButtonData(
            uid = option.uid(),
            selected = fieldUiModel.displayName == option.displayName(),
            enabled = true,
            textInput = option.displayName(),
        )
    } ?: emptyList()

    InputRadioButton(
        title = fieldUiModel.label,
        radioButtonData = data,
        orientation = orientation,
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend?.let {
            LegendData(Color(it.color), it.label ?: "", null)
        },
        isRequired = fieldUiModel.mandatory,
        itemSelected = data.find { it.selected },
        onItemChange = { item ->
            intentHandler.invoke(
                FormIntent.OnSave(
                    fieldUiModel.uid,
                    fieldUiModel.optionSetConfiguration?.optionsToDisplay()
                        ?.find { it.uid() == item?.uid }?.code(),
                    fieldUiModel.valueType,
                ),
            )
        },
    )
}
