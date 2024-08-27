package org.dhis2.composetable.ui

import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import org.dhis2.composetable.model.DropdownOption

@Composable
fun DropDownOptions(
    expanded: Boolean,
    options: List<DropdownOption>,
    onDismiss: () -> Unit,
    onSelected: (code: String, label: String) -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
    ) {
        options.forEach { option ->
            DropdownMenuItem(
                onClick = {
                    onSelected.invoke(option.code, option.name)
                },
            ) {
                Text(text = option.name)
            }
        }
    }
}
