package org.dhis2.usescases.settings.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import org.dhis2.BuildConfig
import org.dhis2.R
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle

@Composable
internal fun AppUpdateSettingItem(
    isOpened: Boolean,
    onClick: () -> Unit,
    onCheckVersionUpdate: () -> Unit,
) {
    SettingItem(
        title = stringResource(id = R.string.settingsCheckVersion),
        subtitle =
            buildAnnotatedString {
                val description = "${stringResource(R.string.app_version)} ${BuildConfig.VERSION_NAME}"
                append(description)
                addStyle(
                    style = SpanStyle(MaterialTheme.colorScheme.primary),
                    start = description.indexOf(BuildConfig.VERSION_NAME),
                    end = description.length,
                )
            },
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
