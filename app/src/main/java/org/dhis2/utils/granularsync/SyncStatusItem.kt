package org.dhis2.utils.granularsync

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import org.dhis2.commons.R
import org.dhis2.commons.ui.icons.SyncingIcon
import org.dhis2.utils.granularsync.domain.SyncStatus
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor

@Composable
fun SyncStatusItem(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    syncStatusIcon: @Composable () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(color = Color.White)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onClick() },
        horizontalArrangement = spacedBy(8.dp),
    ) {
        syncStatusIcon()
        Column {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = title,
                style =
                    MaterialTheme.typography.bodyMedium
                        .copy(color = MaterialTheme.colorScheme.onSurface),
            )
            subtitle?.let {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = subtitle,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.54f),
                        ),
                )
            }
        }
    }
}

@Composable
fun SyncUiStatusIcon(status: SyncStatus) {
    if (status == SyncStatus.UPLOADING) {
        SyncingIcon()
    } else {
        val (imageVector, tint) = status.toIconData()
        Icon(
            imageVector = imageVector,
            tint = tint,
            contentDescription = "sync",
        )
    }
}

@Composable
private fun SyncStatus.toIconData(): Pair<ImageVector, Color> {
    val imageVector =
        when (this) {
            SyncStatus.NOT_SYNCED,
            SyncStatus.UPLOADING,
            -> ImageVector.vectorResource(id = R.drawable.ic_sync_problem_grey)

            SyncStatus.ERROR ->
                ImageVector.vectorResource(id = R.drawable.ic_sync_problem_red)

            SyncStatus.WARNING ->
                ImageVector.vectorResource(id = R.drawable.ic_sync_warning)

            SyncStatus.SENT_VIA_SMS,
            SyncStatus.SYNCED_VIA_SMS,
            -> ImageVector.vectorResource(id = R.drawable.ic_sync_sms)

            SyncStatus.SYNCED,
            SyncStatus.RELATIONSHIP,
            -> ImageVector.vectorResource(id = R.drawable.ic_status_synced)
        }
    val tint =
        when (this) {
            SyncStatus.NOT_SYNCED,
            SyncStatus.UPLOADING,
            -> Color("#333333".toColorInt())

            SyncStatus.ERROR -> Color("#E91E63".toColorInt())
            SyncStatus.WARNING -> Color("#FF9800".toColorInt())
            SyncStatus.SENT_VIA_SMS,
            SyncStatus.SYNCED_VIA_SMS,
            -> Color("#03A9F4".toColorInt())

            SyncStatus.SYNCED,
            SyncStatus.RELATIONSHIP,
            -> TextColor.OnSurfaceLight
        }
    return Pair(imageVector, tint)
}

@Preview(showBackground = true)
@Composable
fun SyncStatusItemPreview() {
    SyncStatusItem(
        title = "Education program",
        subtitle = "Sync error",
        onClick = {},
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_warning_alert),
            contentDescription = "",
        )
    }
}
