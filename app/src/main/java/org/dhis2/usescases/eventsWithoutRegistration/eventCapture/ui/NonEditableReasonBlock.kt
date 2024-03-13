package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import org.dhis2.R
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor

@Composable
fun NonEditableReasonBlock(
    reason: String,
    canBeReopened: Boolean,
    onReopenClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .background(SurfaceColor.Container),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.Spacing10),
            modifier = Modifier
                .padding(
                    start = Spacing.Spacing16,
                    top = Spacing.Spacing8,
                    end = Spacing.Spacing16,
                    bottom = Spacing.Spacing8,
                ),
        ) {
            Text(
                text = reason,
                color = TextColor.OnSurfaceVariant,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp,
            )
            if (canBeReopened) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = stringResource(id = R.string.re_open_to_edit),
                    style = ButtonStyle.FILLED,
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.LockOpen,
                            contentDescription = stringResource(id = R.string.re_open_to_edit),
                            tint = TextColor.OnPrimary,
                        )
                    },
                ) {
                    onReopenClick()
                }
            }
        }
    }
}

@Preview
@Composable
fun NonEditableReasonBlockPreview() {
    NonEditableReasonBlock(
        reason = "This data is not editable because it is marked as completed.",
        canBeReopened = true,
        onReopenClick = {
        },
    )
}

fun showNonEditableReasonMessage(
    composeView: ComposeView,
    reason: String,
    canBeReopened: Boolean,
    onReopenClick: () -> Unit,
) {
    composeView.setContent {
        NonEditableReasonBlock(
            reason = reason,
            canBeReopened = canBeReopened,
            onReopenClick = {
                onReopenClick()
            },
        )
    }
}
