package org.dhis2.form.ui.provider.inputfield

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
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
    fetchOptions: () -> Unit,
) {
    var selectedItem by remember(fieldUiModel) {
        mutableStateOf(DropdownItem(fieldUiModel.displayName ?: ""))
    }

    val optionSetConfiguration = fieldUiModel.optionSetConfiguration

    val optionsData =
        optionSetConfiguration
            ?.optionFlow
            ?.collectAsLazyPagingItems()
            ?.also { LaunchedEffect(optionSetConfiguration) { it.refresh() } }

    val useDropdown by remember {
        derivedStateOf {
            optionSetConfiguration?.searchEmitter?.value?.isEmpty() == true &&
                (optionsData?.itemCount ?: 0) < 15
        }
    }

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
        fetchItem = { index ->
            DropdownItem(optionsData?.get(index)?.option?.displayName() ?: "")
        },
        onSearchOption = { query ->
            fieldUiModel.optionSetConfiguration?.onSearch?.invoke(query)
        },
        itemCount = optionsData?.itemCount ?: 0,
        useDropDown = useDropdown,
        onItemSelected = { index, newSelectedItem ->
            selectedItem = newSelectedItem
            fieldUiModel.onSave(
                optionsData?.get(index)?.option?.code(),
            )
        },
        loadOptions = fetchOptions,
        onDismiss = {
            fieldUiModel.optionSetConfiguration?.onSearch?.invoke("")
        },
    )
}
