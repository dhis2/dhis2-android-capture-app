package org.dhis2.composetable.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.dhis2.composetable.R
import org.dhis2.composetable.model.DropdownOption
import org.dhis2.composetable.model.TableCell
import org.hisp.dhis.mobile.ui.designsystem.component.CheckBoxData
import org.hisp.dhis.mobile.ui.designsystem.component.MultiSelectBottomSheet

@Composable
fun MultiOptionSelector(
    options: List<DropdownOption>,
    cell: TableCell,
    title: String,
    onSave: (String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    MultiSelectBottomSheet(
        items = options.map { option ->
            CheckBoxData(
                uid = option.code,
                checked = cell.value?.split(", ")?.contains(option.name) == true,
                enabled = cell.editable,
                textInput = option.name,
            )
        },
        title = title,
        noResultsFoundString = stringResource(R.string.no_results_found),
        searchToFindMoreString = stringResource(id = R.string.search_to_see_more),
        doneButtonText = stringResource(id = R.string.done),
        onItemsSelected = { checkBoxes ->
            val checkedCodes = checkBoxes
                .filter { item -> item.checked }
                .joinToString(", ") { it.uid }
            val checkedValues = checkBoxes
                .filter { item -> item.checked }
                .joinToString(", ") { it.textInput?.text.orEmpty() }
            onSave(checkedCodes, checkedValues)
        },
        onDismiss = onDismiss,
    )
}
