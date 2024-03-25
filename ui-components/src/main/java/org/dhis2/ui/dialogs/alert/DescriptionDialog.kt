package org.dhis2.ui.dialogs.alert

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.dhis2.ui.R
import org.hisp.dhis.mobile.ui.designsystem.component.Button

@Composable
fun DescriptionDialog(labelText: String, descriptionText: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = labelText) },
        text = { Text(text = descriptionText) },
        confirmButton = {
            Button(
                text = stringResource(id = R.string.action_close),
                onClick = onDismiss,
            )
        },
    )
}
