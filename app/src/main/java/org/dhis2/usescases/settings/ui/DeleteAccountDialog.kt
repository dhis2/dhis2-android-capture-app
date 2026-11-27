package org.dhis2.usescases.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.dhis2.R
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonState
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.ColorStyle
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicator
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicatorType

private const val CONFIRM_BUTTON_TAG = "CONFIRM_BUTTON_TAG"

@Composable
fun DeleteAccountDialog(
    isDeletingAccount: Boolean,
    user: String,
    onDeleteAccount: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.remove_account_from_device_question),
                textAlign = TextAlign.Start,
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (user.isNotBlank()) {
                    Text(
                        text = user,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }
                Text(
                    text = stringResource(R.string.delete_account_message),
                    textAlign = TextAlign.Start,
                )
                Spacer(modifier = Modifier.size(16.dp))
                if (isDeletingAccount) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ProgressIndicator(
                            modifier =
                                Modifier
                                    .width(100.dp)
                                    .height(100.dp),
                            type = ProgressIndicatorType.CIRCULAR,
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    state =
                        ButtonState(
                            text = stringResource(R.string.cancel),
                            style = ButtonStyle.OUTLINED,
                        ),
                    onClick = onDismissRequest,
                )
                Button(
                    modifier = Modifier.testTag(CONFIRM_BUTTON_TAG),
                    state =
                        ButtonState(
                            text = stringResource(R.string.remove_account),
                            textColor = MaterialTheme.colorScheme.onPrimary,
                            style = ButtonStyle.FILLED,
                            colorStyle = ColorStyle.ERROR,
                        ),
                    onClick = onDeleteAccount,
                )
            }
        },
    )
}
