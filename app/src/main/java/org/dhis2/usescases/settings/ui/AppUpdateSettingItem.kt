package org.dhis2.usescases.settings.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.dhis2.R
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle

@Composable
internal fun AppUpdateSettingItem(
    versionName: String,
    isOpened: Boolean,
    onClick: () -> Unit,
    onCheckVersionUpdate: () -> Unit,
) {
    SettingItem(
        title = stringResource(id = R.string.settingsCheckVersion),
        additionalInfoList =
            listOf(
                AdditionalInfoItem(
                    key = stringResource(R.string.app_version),
                    value = versionName,
                ),
            ),
        icon = Icons.Outlined.SystemUpdate,
        extraActions = {
            Button(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.check_for_updates),
                style = ButtonStyle.TONAL,
                enabled = true,
                onClick = onCheckVersionUpdate,
            )
        },
        showExtraActions = isOpened,
        onClick = onClick,
    )
}
