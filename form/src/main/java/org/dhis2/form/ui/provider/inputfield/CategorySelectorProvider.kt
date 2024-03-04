package org.dhis2.form.ui.provider.inputfield

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.dhis2.commons.extensions.inDateRange
import org.dhis2.commons.extensions.inOrgUnit
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.R
import org.dhis2.form.extensions.inputState
import org.dhis2.form.extensions.legend
import org.dhis2.form.model.EventCategory
import org.dhis2.form.model.EventCategoryCombo
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
    resources: ResourceManager,
) {
    var selectedCategoryOptions by remember {
        mutableStateOf(
            fieldUiModel.eventCatCombo?.selectedCategoryOptions?.toMap() ?: emptyMap(),
        )
    }

    fieldUiModel.eventCatCombo?.let { eventCatCombo ->
        if (eventCatCombo.categories.isNotEmpty()) {
            eventCatCombo.categories.forEach { category ->
                ProvideCategorySelector(
                    modifier = modifier,
                    fieldUiModel = fieldUiModel,
                    inputStyle = inputStyle,
                    eventCatCombo = eventCatCombo,
                    category = category,
                    resources = resources,
                    onCategoryOptionSelected = { categoryOption ->
                        selectedCategoryOptions += (category.uid to categoryOption)
                        fieldUiModel.onSave(
                            getUidsList(
                                selectedCategoryOptions.values.filterNotNull(),
                            ).joinToString(","),
                        )
                    },
                )
            }
        } else {
            ProvideEmptyCategorySelector(
                modifier = modifier,
                name = eventCatCombo.displayName ?: resources.getString(R.string.cat_combo),
                option = resources.getString(R.string.no_options),
                inputStyle = inputStyle,
            )
        }
    }
}

@Composable
fun ProvideCategorySelector(
    modifier: Modifier = Modifier,
    fieldUiModel: FieldUiModel,
    inputStyle: InputStyle,
    eventCatCombo: EventCategoryCombo,
    category: EventCategory,
    resources: ResourceManager,
    onCategoryOptionSelected: (CategoryOption?) -> Unit,
) {
    var selectedItem by remember {
        mutableStateOf(
            DropdownItem(
                eventCatCombo.selectedCategoryOptions[category.uid]?.displayName()
                    ?: eventCatCombo.categoryOptions?.get(category.uid)?.displayName() ?: "",
            ),
        )
    }

    val selectableOptions = category.options
        .filter { option ->
            option.access().data().write()
        }.filter { option ->
            option.inDateRange(eventCatCombo.date)
        }.filter { option ->
            option.inOrgUnit(eventCatCombo.orgUnitUID)
        }

    val dropdownItems =
        selectableOptions.map { DropdownItem(it.displayName() ?: it.code() ?: "") }

    if (selectableOptions.isNotEmpty()) {
        InputDropDown(
            modifier = modifier,
            title = category.name,
            state = getInputState(fieldUiModel.inputState(), selectedItem.label.isEmpty()),
            inputStyle = inputStyle,
            selectedItem = selectedItem,
            onResetButtonClicked = {
                selectedItem = DropdownItem("")
                onCategoryOptionSelected(null)
            },
            onItemSelected = { newSelectedItem ->
                selectedItem = newSelectedItem
                onCategoryOptionSelected(
                    selectableOptions.firstOrNull {
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
            option = resources.getString(R.string.no_options),
            inputStyle = inputStyle,
        )
    }
}

@Composable
fun ProvideEmptyCategorySelector(
    modifier: Modifier = Modifier,
    name: String,
    option: String,
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
        dropdownItems = listOf(DropdownItem(option)),
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
