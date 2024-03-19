package org.dhis2.form.ui.provider.inputfield

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.dhis2.form.extensions.inputState
import org.dhis2.form.extensions.legend
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.ui.intent.FormIntent
import org.hisp.dhis.mobile.ui.designsystem.component.CheckBoxData
import org.hisp.dhis.mobile.ui.designsystem.component.InputMultiSelection

@Composable
internal fun ProvideMultiSelectionInput(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
) {
    val optionsToDisplay = fieldUiModel.optionSetConfiguration?.optionsToDisplay() ?: emptyList()
    val data = optionsToDisplay.map { option ->
        CheckBoxData(
            uid = option.uid(),
            checked = option.code()?.let { fieldUiModel.value?.split(",")?.contains(it) } ?: false,
            enabled = true,
            textInput = option.displayName() ?: "",
        )
    }

    InputMultiSelection(
        modifier = modifier,
        title = fieldUiModel.label,
        items = data,
        state = fieldUiModel.inputState(),
        supportingTextData = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        isRequired = fieldUiModel.mandatory,
        onItemsSelected = {
            val checkedValues = it.filter { item -> item.checked }.mapNotNull {
                optionsToDisplay.find { option -> option.uid() == it.uid }?.code()
            }

            intentHandler(
                FormIntent.OnSave(
                    fieldUiModel.uid,
                    checkedValues.joinToString(separator = ","),
                    fieldUiModel.valueType,
                ),
            )
        },
        onClearItemSelection = {
            intentHandler(FormIntent.ClearValue(fieldUiModel.uid))
        },
    )
}
