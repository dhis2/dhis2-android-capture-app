package org.dhis2.usescases.settings.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LockPerson
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import org.dhis2.R
import org.dhis2.usescases.settings.SettingItem
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem

@Composable
fun TwoFASettingItem(onClick: () -> Unit) {
    SettingItem(
        modifier = Modifier.testTag(SettingItem.TWO_FACTOR_AUTH.name),
        title = stringResource(id = R.string.settingTwoFA),
        additionalInfoList =
            listOf(
                // TODO: Replace "Unknown" with actual status when available
                AdditionalInfoItem(
                    key = stringResource(R.string.settingsTwoFAStatus),
                    value = "Unknown",
                ),
                AdditionalInfoItem(
                    value = stringResource(R.string.settingTwoFADescr),
                ),
            ),
        icon = Icons.Outlined.LockPerson,
        extraActions = {},
        showExtraActions = false,
        onClick = onClick,
    )
}
