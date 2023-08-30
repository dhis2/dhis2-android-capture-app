package org.dhis2.composetable.ui

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.dhis2.composetable.R
import org.dhis2.composetable.model.TableDialogModel

@Composable
fun TableDialog(
    dialogModel: TableDialogModel,
    onDismiss: () -> Unit,
    onPrimaryButtonClick: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(dialogModel.title)
        },
        text = {
            Text(dialogModel.message)
        },
        confirmButton = {
            Button(onClick = onPrimaryButtonClick) {
                Text(stringResource(R.string.dialog_option_accept))
            }
        },
    )
}
