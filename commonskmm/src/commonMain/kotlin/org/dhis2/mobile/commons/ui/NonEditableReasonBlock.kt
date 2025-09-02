package org.dhis2.mobile.commons.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.dhis2.mobile.commons.resources.Res
import org.dhis2.mobile.commons.resources.re_open_to_edit
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import org.jetbrains.compose.resources.stringResource

@Composable
fun NonEditableReasonBlock(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues =
        PaddingValues(
            start = Spacing.Spacing16,
            top = Spacing.Spacing8,
            end = Spacing.Spacing16,
            bottom = Spacing.Spacing8,
        ),
    reason: String,
    canBeReopened: Boolean,
    onReopenClick: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.Spacing10),
        modifier =
            modifier
                .background(SurfaceColor.Container)
                .padding(paddingValues),
    ) {
        if (reason.isNotEmpty()) {
            Text(
                text = reason,
                color = TextColor.OnSurfaceVariant,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp,
            )
        }
        if (canBeReopened) {
            Button(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .testTag("REOPEN_BUTTON"),
                text = stringResource(Res.string.re_open_to_edit),
                style = ButtonStyle.FILLED,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.LockOpen,
                        contentDescription = stringResource(Res.string.re_open_to_edit),
                        tint = TextColor.OnPrimary,
                    )
                },
                paddingValues =
                    PaddingValues(
                        start = Spacing.Spacing16,
                        end = Spacing.Spacing24,
                    ),
            ) {
                onReopenClick()
            }
        }
    }
}
