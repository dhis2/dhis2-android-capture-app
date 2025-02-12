package org.dhis2.ui.sync

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor

@Composable
private fun ProvideSyncButton(text: String?, onSyncIconClick: () -> Unit) {
    text?.let {
        Button(
            style = ButtonStyle.TONAL,
            text = it,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Sync,
                    contentDescription = it,
                    tint = TextColor.OnPrimaryContainer,
                )
            },
            onClick = onSyncIconClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
