package org.dhis2.usescases.settings.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.dhis2.R

@Composable
internal fun ExportDatabaseSettingsSettingItem(
    isOpened: Boolean,
    displayProgress: Boolean,
    onClick: () -> Unit,
    onShare: () -> Unit,
    onDownload: () -> Unit,
) {
    SettingItem(
        title = stringResource(id = R.string.settingsExportDB),
        subtitle = stringResource(R.string.settingsExportDBMessage),
        icon = Icons.Outlined.Storage,
        extraActions = {
            ExportOption(
                onDownload = onDownload,
                onShare = onShare,
                displayProgress = displayProgress,
            )
        },
        showExtraActions = isOpened,
        onClick = onClick,
    )
}
