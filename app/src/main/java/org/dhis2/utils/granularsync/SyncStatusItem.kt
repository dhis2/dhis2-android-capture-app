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
import org.dhis2.ui.R

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
