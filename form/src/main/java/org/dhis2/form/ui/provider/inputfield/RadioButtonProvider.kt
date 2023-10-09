package org.dhis2.form.ui.provider.inputfield

import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.dhis2.form.R
import org.dhis2.form.extensions.inputState
import org.dhis2.form.extensions.legend
import org.dhis2.form.extensions.orientation
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.hisp.dhis.mobile.ui.designsystem.component.InputRadioButton
import org.hisp.dhis.mobile.ui.designsystem.component.RadioButtonData

@Composable
internal fun ProvideRadioButtonInput(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
) {
    val data = fieldUiModel.optionSetConfiguration?.optionsToDisplay()?.map { option ->
        RadioButtonData(
            uid = option.uid(),
            selected = fieldUiModel.displayName == option.displayName(),
            enabled = true,
            textInput = option.displayName(),
        )
    } ?: emptyList()

    InputRadioButton(
        modifier = modifier,
        title = fieldUiModel.label,
        radioButtonData = data,
        orientation = fieldUiModel.orientation(),
        supportingText = fieldUiModel.supportingText(),
            state = fieldUiModel.inputState(),
        legendData = fieldUiModel.legend(),
        isRequired = fieldUiModel.mandatory,
        itemSelected = data.find { it.selected },
        onItemChange = { item ->
            fieldUiModel.onSave(
                fieldUiModel.optionSetConfiguration?.optionsToDisplay()
                    ?.find { it.uid() == item?.uid }?.code(),
            )
        },
    )
}

@Composable
internal fun ProvideYesNoRadioButtonInput(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
    resources: Resources,
) {
    val data = listOf(
        RadioButtonData(
            uid = "true",
            selected = fieldUiModel.isAffirmativeChecked,
            enabled = true,
            textInput = resources.getString(R.string.yes),
        ),
        RadioButtonData(
            uid = "false",
            selected = fieldUiModel.isNegativeChecked,
            enabled = true,
            textInput = resources.getString(R.string.no),
        ),
    )

    InputRadioButton(
        modifier = modifier,
        title = fieldUiModel.label,
        radioButtonData = data,
        orientation = fieldUiModel.orientation(),
        supportingText = fieldUiModel.supportingText(),
            state = fieldUiModel.inputState(),
        legendData = fieldUiModel.legend(),
        isRequired = fieldUiModel.mandatory,
        itemSelected = data.find { it.selected },
        onItemChange = { item ->
            when (item?.uid) {
                "true" -> fieldUiModel.onSaveBoolean(true)
                "false" -> fieldUiModel.onSaveBoolean(false)
                else -> fieldUiModel.onClear()
            }
        },
    )
}
