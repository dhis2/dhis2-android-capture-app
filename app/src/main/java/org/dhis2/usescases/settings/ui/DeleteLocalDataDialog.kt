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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import org.dhis2.R
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicator
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicatorType

private const val CONFIRM_BUTTON_TAG = "CONFIRM_BUTTON_TAG"

@Composable
fun DeleteLocalDataDialog(
    isDeletingLocalData: Boolean,
    onDeleteLocalData: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.warning),
    )
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = stringResource(R.string.delete_local_data),
                textAlign = TextAlign.Center,
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.delete_local_data_message),
                )
                Spacer(modifier = Modifier.size(16.dp))
                if (!isDeletingLocalData) {
                    LottieAnimation(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                        composition = composition,
                        iterations = LottieConstants.IterateForever,
                    )
                } else {
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
            Button(
                text = stringResource(R.string.delete_local_data),
                modifier = Modifier.testTag(CONFIRM_BUTTON_TAG),
                onClick = onDeleteLocalData,
            )
        },
        dismissButton = {
            Button(
                text = stringResource(R.string.cancel),
                onClick = onDismissRequest,
            )
        },
    )
}
