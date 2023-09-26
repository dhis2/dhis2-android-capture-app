package org.dhis2.ui.dialogs.alert

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun DescriptionDialog(labelText: String, descriptionText: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = labelText) },
        text = { Text(text = descriptionText) },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(text = "Close")
            }
        },
    )
}
