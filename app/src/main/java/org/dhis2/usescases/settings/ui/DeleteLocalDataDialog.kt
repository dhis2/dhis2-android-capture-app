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
fun DeleteLocalDataDialog(
    isDeletingLocalData: Boolean,
    onDeleteLocalData: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.delete_local_data),
                textAlign = TextAlign.Start,
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.delete_local_data_message_v2),
                )
                Spacer(modifier = Modifier.size(16.dp))
                if (isDeletingLocalData) {
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
                            text = stringResource(R.string.delete_data),
                            textColor = MaterialTheme.colorScheme.onPrimary,
                            style = ButtonStyle.FILLED,
                            colorStyle = ColorStyle.ERROR,
                        ),
                    onClick = onDeleteLocalData,
                )
            }
        },
    )
}
