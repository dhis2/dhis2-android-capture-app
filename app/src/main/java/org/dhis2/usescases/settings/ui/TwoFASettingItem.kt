package org.dhis2.usescases.settings.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LockPerson
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import org.dhis2.R
import org.dhis2.usescases.settings.SettingItem

@Composable
fun TwoFASettingItem(
    onClick: () -> Unit,
) {
    SettingItem(
        modifier = Modifier.testTag(SettingItem.TWO_FACTOR_AUTH.name),
        title = stringResource(id = R.string.settingTwoFA),
        subtitle = buildAnnotatedString {
            append(
                String.format(
                    stringResource(R.string.settingsTwoFAStatus),
                    "Unknown",
                ),
            )
            append("\n")
            append(stringResource(R.string.settingTwoFADescr))
        },
        icon = Icons.Outlined.LockPerson,
        extraActions = {},
        showExtraActions = false,
        onClick = onClick,
    )
}
