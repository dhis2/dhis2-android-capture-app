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
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextData
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextState

@Composable
internal fun ProvideCategorySelectorInput(
    modifier: Modifier = Modifier,
    fieldUiModel: FieldUiModel,
    inputStyle: InputStyle,
) {
    val selectedCategoryOptions: Map<String, EventCategoryOption?> by remember(
        fieldUiModel.value,
        fieldUiModel.eventCategories,
    ) {
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

/**
 * The supporting texts of one category: why its stored option is no longer valid, until the user
 * picks or clears an option, the field error (e.g. mandatory) only while it is empty, and why it
 * has no options.
 */
private fun categorySupportingText(
    fieldUiModel: FieldUiModel,
    category: EventCategory,
    showCategoryError: Boolean,
    isEmpty: Boolean,
): List<SupportingTextData>? {
    val error =
        (category.error?.takeIf { showCategoryError } ?: fieldUiModel.error).takeIf { isEmpty }
    return listOfNotNull(
        error?.let { SupportingTextData(it, SupportingTextState.ERROR) },
        category.noOptionsReason?.let { SupportingTextData(it, SupportingTextState.DEFAULT) },
    ).ifEmpty { null }
}

private fun List<SupportingTextData>?.hasError() = this?.any { it.state == SupportingTextState.ERROR } == true

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

    var showCategoryError by remember(category.error) { mutableStateOf(true) }
    val supportingText =
        categorySupportingText(
            fieldUiModel = fieldUiModel,
            category = category,
            showCategoryError = showCategoryError,
            isEmpty = selectedCategoryOption == null,
        )

    if (category.options.isNotEmpty()) {
        var dropdownItems by remember(category.options) {
            mutableStateOf(
                category.options.map { DropdownItem(it.name) },
            )
        }

        InputDropDown(
            modifier = modifier,
            title = category.name,
            state =
                if (supportingText.hasError() && fieldUiModel.editable) {
                    InputShellState.ERROR
                } else {
                    getInputState(fieldUiModel.inputState(), selectedItem.label.isEmpty())
                },
            inputStyle = inputStyle,
            selectedItem = selectedItem,
            supportingTextData = supportingText,
            onResetButtonClicked = {
                showCategoryError = false
                onCategoryOptionSelected(null)
            },
            onItemSelected = { _, newSelectedItem ->
                showCategoryError = false
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
            isRequiredField = fieldUiModel.mandatory,
            supportingTextData = supportingText,
            state =
                when {
                    !fieldUiModel.editable -> InputShellState.DISABLED
                    supportingText.hasError() -> InputShellState.ERROR
                    else -> InputShellState.UNFOCUSED
                },
        )
    }
}

@Composable
fun ProvideEmptyCategorySelector(
    modifier: Modifier = Modifier,
    name: String,
    inputStyle: InputStyle,
    isRequiredField: Boolean = false,
    supportingTextData: List<SupportingTextData>? = null,
    state: InputShellState = InputShellState.UNFOCUSED,
) {
    val emptyItems = listOf(DropdownItem(stringResource(id = R.string.no_options)))

    InputDropDown(
        modifier = modifier,
        title = name,
        state = state,
        inputStyle = inputStyle,
        selectedItem = DropdownItem(""),
        supportingTextData = supportingTextData,
        onResetButtonClicked = { /*no-op*/ },
        onItemSelected = { _, _ -> },
        fetchItem = { index ->
            emptyItems[index]
        },
        itemCount = 1,
        onSearchOption = { /*no-op*/ },
        isRequiredField = isRequiredField,
        loadOptions = {
        },
    )
}

internal fun getInputState(
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
