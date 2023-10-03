package org.dhis2.form.ui.provider.inputfield

import android.content.res.Resources
import androidx.compose.runtime.Composable
import org.dhis2.form.R
import org.dhis2.form.extensions.legend
import org.dhis2.form.extensions.orientation
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.hisp.dhis.mobile.ui.designsystem.component.CheckBoxData
import org.hisp.dhis.mobile.ui.designsystem.component.InputCheckBox

@Composable
internal fun ProvideCheckBoxInput(
    fieldUiModel: FieldUiModel,
) {
    val data = fieldUiModel.optionSetConfiguration?.optionsToDisplay()?.map { option ->
        CheckBoxData(
            uid = option.uid(),
            checked = fieldUiModel.displayName == option.displayName(),
            enabled = true,
            textInput = option.displayName(),
        )
    } ?: emptyList()

    InputCheckBox(
        title = fieldUiModel.label,
        checkBoxData = data,
        orientation = fieldUiModel.orientation(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        isRequired = fieldUiModel.mandatory,
        onItemChange = { item ->
            fieldUiModel.onSave(
                fieldUiModel.optionSetConfiguration?.optionsToDisplay()
                    ?.find { it.uid() == item.uid }?.code(),
            )
        },
        onClearSelection = {
            fieldUiModel.onClear()
        },
    )
}

@Composable
internal fun ProvideYesNoCheckBoxInput(
    fieldUiModel: FieldUiModel,
    resources: Resources,
) {
    val data = listOf(
        CheckBoxData(
            uid = "true",
            checked = fieldUiModel.isAffirmativeChecked,
            enabled = true,
            textInput = resources.getString(R.string.yes),
        ),
        CheckBoxData(
            uid = "false",
            checked = fieldUiModel.isNegativeChecked,
            enabled = true,
            textInput = resources.getString(R.string.no),
        ),
    )

    InputCheckBox(
        title = fieldUiModel.label,
        checkBoxData = data,
        orientation = fieldUiModel.orientation(),
        supportingText = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        isRequired = fieldUiModel.mandatory,
        onItemChange = { item ->
            when (item.uid) {
                "true" -> fieldUiModel.onSaveBoolean(true)
                "false" -> fieldUiModel.onSaveBoolean(false)
                else -> fieldUiModel.onClear()
            }
        },
        onClearSelection = {
            fieldUiModel.onClear()
        },
    )
}
