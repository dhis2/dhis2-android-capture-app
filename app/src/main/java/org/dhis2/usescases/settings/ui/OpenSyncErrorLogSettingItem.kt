package org.dhis2.usescases.settings.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import org.dhis2.R
import org.dhis2.usescases.settings.SettingItem

@Composable
internal fun OpenSyncErrorLogSettingItem(onClick: () -> Unit) {
    SettingItem(
        modifier = Modifier.testTag(SettingItem.ERROR_LOG.name),
        title = stringResource(id = R.string.settingsErrorLog),
        subtitle = stringResource(R.string.settingsErrorLog_descr),
        icon = Icons.Outlined.ErrorOutline,
        extraActions = {
            // no extra actions
        },
        showExtraActions = false,
        onClick = onClick,
    )
}
