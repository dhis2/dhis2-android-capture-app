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
import org.dhis2.form.model.FieldUiModel
import org.hisp.dhis.android.core.arch.helpers.UidsHelper.getUidsList
import org.hisp.dhis.android.core.category.CategoryOption
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
    val selectedCategoryOptions: Map<String, CategoryOption?> by remember(fieldUiModel.value) {
        mutableStateOf(
            fieldUiModel.eventCatCombo?.categoryOptions?.entries?.associate { entry ->
                val selectedCategoryOption = fieldUiModel.eventCatCombo?.categories?.find {
                    it.uid == entry.key
                }?.options?.find { fieldUiModel.value?.split(",")?.contains(it.uid()) == true }
                entry.key to selectedCategoryOption
            } ?: emptyMap(),
        )
    }

    fieldUiModel.eventCatCombo?.let { eventCatCombo ->
        if (eventCatCombo.categories.isNotEmpty()) {
            eventCatCombo.categories.forEach { category ->
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
                                getUidsList(filteredList).joinToString(",")
                            },
                        )
                    },
                )
            }
        } else {
            ProvideEmptyCategorySelector(
                modifier = modifier,
                name = fieldUiModel.label,
                inputStyle = inputStyle,
            )
        }
    }
}

@Composable
private fun ProvideCategorySelector(
    modifier: Modifier = Modifier,
    fieldUiModel: FieldUiModel,
    inputStyle: InputStyle,
    category: EventCategory,
    onCategoryOptionSelected: (CategoryOption?) -> Unit,
    selectedCategoryOption: CategoryOption?,
) {
    val selectedItem by remember(selectedCategoryOption) {
        mutableStateOf(
            DropdownItem(selectedCategoryOption?.displayName() ?: ""),
        )
    }

    val dropdownItems =
        category.options.map { DropdownItem(it.displayName() ?: it.code() ?: "") }

    if (category.options.isNotEmpty()) {
        InputDropDown(
            modifier = modifier,
            title = category.name,
            state = getInputState(fieldUiModel.inputState(), selectedItem.label.isEmpty()),
            inputStyle = inputStyle,
            selectedItem = selectedItem,
            onResetButtonClicked = {
                onCategoryOptionSelected(null)
            },
            onItemSelected = { newSelectedItem ->
                onCategoryOptionSelected(
                    category.options.firstOrNull {
                        it.displayName() == newSelectedItem.label
                    },
                )
            },
            dropdownItems = dropdownItems,
            isRequiredField = fieldUiModel.mandatory,
            supportingTextData = getSupportingText(
                fieldUiModel,
                selectedItem.label.isEmpty(),
            ),
            legendData = fieldUiModel.legend(),
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

    InputDropDown(
        modifier = modifier,
        title = name,
        state = InputShellState.UNFOCUSED,
        inputStyle = inputStyle,
        selectedItem = DropdownItem(selectedItem),
        onResetButtonClicked = {
            selectedItem = ""
        },
        onItemSelected = { newSelectedDropdownItem ->
            selectedItem = newSelectedDropdownItem.label
        },
        dropdownItems = listOf(DropdownItem(stringResource(id = R.string.no_options))),
        isRequiredField = false,
    )
}

private fun getInputState(
    inputState: InputShellState,
    isEmpty: Boolean,
): InputShellState {
    return when (inputState) {
        InputShellState.ERROR -> {
            if (isEmpty) {
                inputState
            } else {
                InputShellState.UNFOCUSED
            }
        }

        else -> inputState
    }
}

private fun getSupportingText(
    fieldUiModel: FieldUiModel,
    isEmpty: Boolean,
): List<SupportingTextData>? = listOfNotNull(
    fieldUiModel.error?.let {
        if (isEmpty) {
            SupportingTextData(
                it,
                SupportingTextState.ERROR,
            )
        } else {
            null
        }
    },
    fieldUiModel.warning?.let {
        SupportingTextData(
            it,
            SupportingTextState.WARNING,
        )
    },
    fieldUiModel.description?.let {
        SupportingTextData(
            it,
            SupportingTextState.DEFAULT,
        )
    },
).ifEmpty { null }
