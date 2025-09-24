package org.dhis2.mobile.login.main.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.IconButton
import org.hisp.dhis.mobile.ui.designsystem.component.IconButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicator
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicatorType
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing

@Composable
fun TaskExecutorButton(
    modifier: Modifier,
    taskRunning: Boolean,
    actionText: String,
    taskRunningText: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    onCancel: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .animateContentSize(),
        horizontalArrangement = spacedBy(Spacing.Spacing16),
    ) {
        Button(
            modifier =
                modifier
                    .weight(1f),
            enabled = false,
            style = ButtonStyle.FILLED,
            icon = {
                if (taskRunning) {
                    ProgressIndicator(
                        type = ProgressIndicatorType.CIRCULAR_SMALL,
                    )
                } else {
                    icon()
                }
            },
            text =
                if (taskRunning) {
                    taskRunningText
                } else {
                    actionText
                },
            onClick = onClick,
        )

        AnimatedVisibility(
            visible = taskRunning,
        ) {
            IconButton(
                style = IconButtonStyle.TONAL,
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Clear",
                    )
                },
                onClick = onCancel,
            )
        }
    }
}
