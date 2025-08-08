package org.dhis2.usescases.settings.ui

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SyncDisabled
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import org.dhis2.R
import org.dhis2.bindings.EVERY_12_HOUR
import org.dhis2.bindings.EVERY_24_HOUR
import org.dhis2.bindings.EVERY_30_MIN
import org.dhis2.bindings.EVERY_6_HOUR
import org.dhis2.bindings.EVERY_HOUR
import org.dhis2.commons.Constants
import org.dhis2.data.service.SyncResult
import org.dhis2.usescases.settings.models.DataSettingsViewModel
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItemColor
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.DropdownItem
import org.hisp.dhis.mobile.ui.designsystem.component.InputDropDown
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import org.dhis2.usescases.settings.SettingItem as SettingItemType

@Composable
internal fun SyncDataSettingItem(
    dataSettings: DataSettingsViewModel,
    canInitSync: Boolean,
    isOpened: Boolean,
    onClick: () -> Unit,
    onSyncDataClick: () -> Unit,
    onSyncDataPeriodChanged: (Int) -> Unit,
) {
    val additionalInfoList = when {
        dataSettings.syncInProgress -> {
            listOf(
                AdditionalInfoItem(
                    key = stringResource(R.string.settings_sync_period),
                    value = syncPeriodLabel(dataSettings.dataSyncPeriod),
                    isConstantItem = true,
                ),
                    AdditionalInfoItem(
                        value = stringResource(R.string.syncing_data),
                    isConstantItem = true,
                ),
            )
        }
        dataSettings.dataHasErrors -> {
            listOf(
                AdditionalInfoItem(
                    key = stringResource(R.string.settings_sync_period),
                        value = syncPeriodLabel(dataSettings.dataSyncPeriod),
                        isConstantItem = true,
                ),
                AdditionalInfoItem(
                    value = stringResource(R.string.data_sync_error),
                    isConstantItem = true,
                    icon = {
                            Icon(
                            imageVector = Icons.Outlined.SyncDisabled,
                            contentDescription = "SYNC ERROR",
                            tint = AdditionalInfoItemColor.ERROR.color,
                        )
                        },
                    color = AdditionalInfoItemColor.ERROR.color,
                ),
            )
        }
        dataSettings.dataHasWarnings -> {
            listOf(
                AdditionalInfoItem(
                    key = stringResource(R.string.settings_sync_period),
                    value = syncPeriodLabel(dataSettings.dataSyncPeriod),
                        isConstantItem = true,
                ),
                AdditionalInfoItem(
                    value = stringResource(R.string.data_sync_warning),
                    isConstantItem = true,
                    truncate = false,
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.SyncDisabled,
                            contentDescription = "SYNC WARNING",
                            tint = AdditionalInfoItemColor.WARNING.color,
                        )
                    },
                    color = AdditionalInfoItemColor.WARNING.color,
                ),
            )
        }
        dataSettings.syncHasErrors && dataSettings.syncResult == SyncResult.INCOMPLETE -> {
            listOf(
                AdditionalInfoItem(
                    key = stringResource(R.string.settings_sync_period),
                    value = syncPeriodLabel(dataSettings.dataSyncPeriod),
                    isConstantItem = true,
                ),
                AdditionalInfoItem(
                    value = stringResource(R.string.sync_incomplete_error_text),
                    isConstantItem = true,
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.SyncDisabled,
                            contentDescription = "SYNC INCOMPLETE",
                            tint = AdditionalInfoItemColor.DISABLED.color,
                        )
                    },
                    color = AdditionalInfoItemColor.DISABLED.color,
                    truncate = false,
                ),
            )
        }
        dataSettings.syncHasErrors && dataSettings.syncResult == SyncResult.ERROR -> {
            listOf(
                AdditionalInfoItem(
                    key = stringResource(R.string.settings_sync_period),
                    value = syncPeriodLabel(dataSettings.dataSyncPeriod),
                        isConstantItem = true,
                ),
                AdditionalInfoItem(
                    value = stringResource(R.string.sync_error_text),
                    isConstantItem = true,
                    color = AdditionalInfoItemColor.ERROR.color,
                ),
            )
        }
        else -> {
            listOf(
                AdditionalInfoItem(
                    key = stringResource(R.string.settings_sync_period),
                    value = syncPeriodLabel(dataSettings.dataSyncPeriod),
                ),
                AdditionalInfoItem(
                    key = stringResource(R.string.last_data_sync),
                    value = dataSettings.lastDataSync,
                    color = TextColor.OnSurface,
                ),
            )
        }
    }

    SettingItem(
        modifier = Modifier.semantics {
            testTag = SettingItemType.DATA_SYNC.name
        },
        title = stringResource(id = R.string.settingsSyncData),
            additionalInfoList = additionalInfoList,
        icon = Icons.Outlined.Update,
        extraActions = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = spacedBy(Spacing.Spacing8),
            ) {
                if (dataSettings.canEdit) {
                    val dataSyncPeriods =
                        listOf(
                            stringResource(R.string.thirty_minutes),
                            stringResource(R.string.a_hour),
                            stringResource(R.string.every_6_hours),
                            stringResource(R.string.every_12_hours),
                            stringResource(R.string.a_day),
                            stringResource(R.string.Manual),
                        )
                    InputDropDown(
                        modifier = Modifier.testTag(TEST_TAG_DATA_PERIOD),
                        title = stringResource(R.string.settings_sync_period),
                        state = InputShellState.FOCUSED,
                        itemCount = dataSyncPeriods.size,
                        onSearchOption = {},
                        fetchItem = { index ->
                            DropdownItem(dataSyncPeriods[index])
                        },
                        selectedItem =
                            DropdownItem(
                                label = syncPeriodLabel(dataSettings.dataSyncPeriod),
                            ),
                        onResetButtonClicked = { },
                        onItemSelected = { index, _ ->
                            when (index) {
                                0 -> onSyncDataPeriodChanged(EVERY_30_MIN)
                                1 -> onSyncDataPeriodChanged(EVERY_HOUR)
                                2 -> onSyncDataPeriodChanged(EVERY_6_HOUR)
                                3 -> onSyncDataPeriodChanged(EVERY_12_HOUR)
                                4 -> onSyncDataPeriodChanged(EVERY_24_HOUR)
                                5 -> onSyncDataPeriodChanged(Constants.TIME_MANUAL)
                                else -> {
                                    // do nothing
                                }
                            }
                        },
                        showSearchBar = false,
                        loadOptions = {},
                    )
                } else {
                    Text(
                        text = stringResource(R.string.syncing_period_not_editable),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    text =
                        stringResource(R.string.SYNC_DATA)
                            .lowercase()
                            .capitalize(Locale.current),
                    style = ButtonStyle.TONAL,
                    enabled = canInitSync,
                    onClick = onSyncDataClick,
                )
            }
        },
        showExtraActions = isOpened,
        onClick = onClick,
    )
}
