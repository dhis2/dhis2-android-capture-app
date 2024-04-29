package org.dhis2.form.ui.provider.inputfield

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.dhis2.form.extensions.inputState
import org.dhis2.form.extensions.legend
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.hisp.dhis.mobile.ui.designsystem.component.DropdownItem
import org.hisp.dhis.mobile.ui.designsystem.component.InputDropDown
import org.hisp.dhis.mobile.ui.designsystem.component.InputStyle

@Composable
fun ProvideDropdownInput(
    modifier: Modifier,
    inputStyle: InputStyle,
    fieldUiModel: FieldUiModel,
) {
    var selectedItem by remember(fieldUiModel) {
        mutableStateOf(DropdownItem(fieldUiModel.displayName ?: ""))
    }

    val selectableOptions = fieldUiModel.optionSetConfiguration?.optionsToDisplay()

    val dropdownItems = selectableOptions?.map { DropdownItem(it.displayName() ?: it.code() ?: "") }
    InputDropDown(
        modifier = modifier,
        inputStyle = inputStyle,
        title = fieldUiModel.label,
        state = fieldUiModel.inputState(),
        selectedItem = DropdownItem(selectedItem.label),
        supportingTextData = fieldUiModel.supportingText(),
        legendData = fieldUiModel.legend(),
        isRequiredField = fieldUiModel.mandatory,
        onResetButtonClicked = { fieldUiModel.onClear() },
        dropdownItems = dropdownItems ?: emptyList(),
        onItemSelected = { newSelectedItem ->
            selectedItem = newSelectedItem
            fieldUiModel.onSave(
                selectableOptions?.firstOrNull {
                    it.displayName() == newSelectedItem.label
                }?.code(),
            )
        },
    )
}
