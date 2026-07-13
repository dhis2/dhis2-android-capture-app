package org.dhis2.usescases.settings.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LockPerson
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import org.dhis2.R
import org.dhis2.mobile.login.authentication.domain.model.TwoFAStatus
import org.dhis2.usescases.settings.SettingItem
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem

@Composable
fun TwoFASettingItem(
    status: TwoFAStatus,
    onClick: () -> Unit,
) {
    val additionalInfoList =
        buildList {
            if (status !is TwoFAStatus.NoConnection) {
                add(
                    AdditionalInfoItem(
                        key = stringResource(R.string.settingsTwoFAStatus),
                        value =
                            when (status) {
                                is TwoFAStatus.Enabled ->
                                    stringResource(R.string.settingsTwoFAEnabled)
                                is TwoFAStatus.Disabled ->
                                    stringResource(R.string.settingsTwoFADisabled)
                            },
                    ),
                )
                add(
                    AdditionalInfoItem(
                        value = stringResource(R.string.settingTwoFADescr),
                    ),
                )
            }
        }
    SettingItem(
        modifier = Modifier.testTag(SettingItem.TWO_FACTOR_AUTH.name),
        title = stringResource(id = R.string.settingTwoFA),
        additionalInfoList = additionalInfoList,
        icon = Icons.Outlined.LockPerson,
        extraActions = {},
        showExtraActions = false,
        onClick = onClick,
    )
}
