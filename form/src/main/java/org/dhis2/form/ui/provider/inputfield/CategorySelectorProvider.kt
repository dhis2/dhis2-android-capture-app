package org.dhis2.form.ui.provider.inputfield

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.dhis2.form.R
import org.dhis2.form.extensions.inputState
import org.dhis2.form.extensions.legend
import org.dhis2.form.model.EventCategory
import org.dhis2.form.model.EventCategoryOption
import org.dhis2.form.model.FieldUiModel
import org.hisp.dhis.mobile.ui.designsystem.component.DropdownItem
import org.hisp.dhis.mobile.ui.designsystem.component.InputDropDown
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.component.InputStyle

@Composable
internal fun ProvideCategorySelectorInput(
    modifier: Modifier = Modifier,
    fieldUiModel: FieldUiModel,
    inputStyle: InputStyle,
) {
    val selectedCategoryOptions: Map<String, EventCategoryOption?> by remember(fieldUiModel.value) {
        mutableStateOf(
            fieldUiModel.eventCategories?.associate { category ->
                category.options
                    .find { option ->
                        fieldUiModel.value?.split(",")?.contains(option.uid) == true
                    }?.let {
                        category.uid to it
                    } ?: (category.uid to null)
            } ?: emptyMap(),
        )
    }

    fieldUiModel.eventCategories?.forEach { category ->
        ProvideCategorySelector(
            modifier = modifier,
            fieldUiModel = fieldUiModel,
            inputStyle = inputStyle,
            category = category,
            selectedCategoryOption = selectedCategoryOptions[category.uid],
            onCategoryOptionSelected = { categoryOption ->
                val updatedCategoryOptions = selectedCategoryOptions.toMutableMap()
                updatedCategoryOptions[category.uid] = categoryOption
                val filteredList = updatedCategoryOptions.values.filterNotNull()
                fieldUiModel.onSave(
                    if (filteredList.isEmpty()) {
                        null
                    } else {
                        filteredList.joinToString(",") {
                            it.uid
                        }
                    },
                )
            },
        )
    } ?: ProvideEmptyCategorySelector(
        modifier = modifier,
        name = fieldUiModel.label,
        inputStyle = inputStyle,
    )
}

@Composable
private fun ProvideCategorySelector(
    modifier: Modifier = Modifier,
    fieldUiModel: FieldUiModel,
    inputStyle: InputStyle,
    category: EventCategory,
    onCategoryOptionSelected: (EventCategoryOption?) -> Unit,
    selectedCategoryOption: EventCategoryOption?,
) {
    val selectedItem by remember(selectedCategoryOption) {
        mutableStateOf(
            DropdownItem(selectedCategoryOption?.name ?: ""),
        )
    }

    if (category.options.isNotEmpty()) {
        var dropdownItems by remember {
            mutableStateOf(
                category.options.map { DropdownItem(it.name) },
            )
        }

        InputDropDown(
            modifier = modifier,
            title = category.name,
            state = getInputState(fieldUiModel.inputState(), selectedItem.label.isEmpty()),
            inputStyle = inputStyle,
            selectedItem = selectedItem,
            onResetButtonClicked = {
                onCategoryOptionSelected(null)
            },
            onItemSelected = { _, newSelectedItem ->
                onCategoryOptionSelected(
                    category.options.firstOrNull {
                        it.name == newSelectedItem.label
                    },
                )
            },
            fetchItem = { index -> dropdownItems[index] },
            itemCount = dropdownItems.size,
            onSearchOption = { query ->
                dropdownItems =
                    if (query.isNotEmpty()) {
                        dropdownItems.filter { it.label.contains(query) }
                    } else {
                        category.options.map { DropdownItem(it.name) }
                    }
            },
            isRequiredField = fieldUiModel.mandatory,
            legendData = fieldUiModel.legend(),
            loadOptions = {
            },
        )
    } else {
        ProvideEmptyCategorySelector(
            modifier = modifier,
            name = category.name,
            inputStyle = inputStyle,
        )
    }
}

@Composable
fun ProvideEmptyCategorySelector(
    modifier: Modifier = Modifier,
    name: String,
    inputStyle: InputStyle,
) {
    var selectedItem by remember {
        mutableStateOf("")
    }

    val emptyItems = listOf(DropdownItem(stringResource(id = R.string.no_options)))

    InputDropDown(
        modifier = modifier,
        title = name,
        state = InputShellState.UNFOCUSED,
        inputStyle = inputStyle,
        selectedItem = DropdownItem(selectedItem),
        onResetButtonClicked = {
            selectedItem = ""
        },
        onItemSelected = { _, newSelectedDropdownItem ->
            selectedItem = newSelectedDropdownItem.label
        },
        fetchItem = { index ->
            emptyItems[index]
        },
        itemCount = 1,
        onSearchOption = { /*no-op*/ },
        isRequiredField = false,
        loadOptions = {
        },
    )
}

private fun getInputState(
    inputState: InputShellState,
    isEmpty: Boolean,
): InputShellState =
    when (inputState) {
        InputShellState.ERROR -> {
            if (isEmpty) {
                inputState
            } else {
                InputShellState.UNFOCUSED
            }
        }

        else -> inputState
    }
