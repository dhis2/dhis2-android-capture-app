package org.dhis2.composetable.ui

import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
fun DropDownOptions(
    expanded: Boolean,
    options: List<String>,
    onDismiss: () -> Unit,
    onSelected: (code: String, label: String) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        options.forEach { option ->
            val code = option.split("_")[0]
            val label = option.split("_")[1]
            DropdownMenuItem(
                onClick = {
                    onSelected.invoke(code, label)
                }
            ) {
                Text(text = label)
            }
        }
    }
}
