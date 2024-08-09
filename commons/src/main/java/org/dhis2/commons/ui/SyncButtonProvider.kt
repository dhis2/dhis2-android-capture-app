package org.dhis2.commons.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.dhis2.commons.R
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor

@Composable
fun SyncButtonProvider(
    state: State,
    syncActionLabel: String = stringResource(id = R.string.sync),
    syncRetryActionLabel: String = stringResource(id = R.string.sync_retry),
    onSyncIconClick: () -> Unit,
) {
    val buttonText = when (state) {
        State.TO_POST,
        State.TO_UPDATE,
        -> syncActionLabel

        State.ERROR,
        State.WARNING,
        -> syncRetryActionLabel

        else -> null
    }
    buttonText?.let {
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
            onClick = { onSyncIconClick() },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
