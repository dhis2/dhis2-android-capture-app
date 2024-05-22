package org.dhis2.composetable.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.dhis2.composetable.R
import org.dhis2.composetable.model.TableDialogModel
import org.hisp.dhis.mobile.ui.designsystem.component.Button

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
            Button(
                modifier = Modifier.padding(bottom = 16.dp, end = 16.dp),
                text = stringResource(R.string.dialog_option_accept),
                onClick = onPrimaryButtonClick,
            )
        },
    )
}
