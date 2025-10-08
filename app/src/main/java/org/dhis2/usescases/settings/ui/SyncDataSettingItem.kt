package org.dhis2.usescases.settings.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SyncDisabled
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    context: Context = LocalContext.current,
) {
    val additionalInfoList =
        when {
            dataSettings.syncInProgress -> {
                provideSyncInProgressInfo(dataSettings.dataSyncPeriod, context)
            }

            dataSettings.dataHasErrors -> {
                provideDataErrorItems(dataSettings.dataSyncPeriod, context)
            }

            dataSettings.dataHasWarnings -> {
                provideDataWarningItems(dataSettings.dataSyncPeriod, context)
            }

            dataSettings.syncHasErrors && dataSettings.syncResult == SyncResult.INCOMPLETE -> {
                provideIncompleteSyncInfo(dataSettings, context)
            }

            dataSettings.syncHasErrors && dataSettings.syncResult == SyncResult.ERROR -> {
                provideSyncErrorInfo(dataSettings.dataSyncPeriod, context)
            }

            else -> {
                provideDefaultInfoItems(dataSettings.dataSyncPeriod, dataSettings.lastDataSync, context)
            }
        }

    SettingItem(
        modifier =
            Modifier.semantics {
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
                    var selectedItem by
                        remember {
                            mutableStateOf(
                                DropdownItem(
                                    label = syncPeriodLabel(dataSettings.dataSyncPeriod, context),
                                ),
                            )
                        }
                    val dataSyncPeriods =
                        listOf(
                            stringResource(R.string.thirty_minutes),
                            stringResource(R.string.a_hour),
                            stringResource(R.string.every_6_hours),
                            stringResource(R.string.every_12_hours),
                            stringResource(R.string.a_day),
                            stringResource(R.string.Manual),
                        )
                    val dropdownItems = dataSyncPeriods.map { DropdownItem(it) }
                    var inputSyncPeriodState =
                        remember {
                            InputShellState.UNFOCUSED
                        }
                    InputDropDown(
                        modifier = Modifier.testTag(TEST_TAG_DATA_PERIOD),
                        title = stringResource(R.string.settings_sync_period_v2),
                        state = inputSyncPeriodState,
                        itemCount = dataSyncPeriods.size,
                        onSearchOption = {},
                        fetchItem = { index ->
                            dropdownItems[index]
                        },
                        selectedItem = selectedItem,
                        onResetButtonClicked = { },
                        onItemSelected = { index, newItem ->
                            selectedItem = newItem
                            inputSyncPeriodState = InputShellState.UNFOCUSED
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
                        showDeleteButton = false,
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

@Composable
private fun provideDefaultInfoItems(
    dataSyncPeriod: Int,
    lastDataSync: String,
    context: Context,
): List<AdditionalInfoItem> =
    listOf(
        AdditionalInfoItem(
            key = stringResource(R.string.settings_sync_period_v2),
            value = syncPeriodLabel(dataSyncPeriod, context),
        ),
        AdditionalInfoItem(
            key = stringResource(R.string.last_data_sync),
            value = lastDataSync,
            color = TextColor.OnSurface,
        ),
    )

@Composable
private fun provideSyncErrorInfo(
    dataSyncPeriod: Int,
    context: Context,
) = listOf(
    AdditionalInfoItem(
        key = stringResource(R.string.settings_sync_period_v2),
        value = syncPeriodLabel(dataSyncPeriod, context),
        isConstantItem = true,
    ),
    AdditionalInfoItem(
        value = stringResource(R.string.sync_error_text),
        isConstantItem = true,
        color = AdditionalInfoItemColor.ERROR.color,
    ),
)

@Composable
private fun provideIncompleteSyncInfo(
    dataSettings: DataSettingsViewModel,
    context: Context,
): List<AdditionalInfoItem> =
    listOf(
        AdditionalInfoItem(
            key = stringResource(R.string.settings_sync_period_v2),
            value = syncPeriodLabel(dataSettings.dataSyncPeriod, context),
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

@Composable
private fun provideDataWarningItems(
    dataSyncPeriod: Int,
    context: Context,
) = listOf(
    AdditionalInfoItem(
        key = stringResource(R.string.settings_sync_period_v2),
        value = syncPeriodLabel(dataSyncPeriod, context),
        isConstantItem = true,
    ),
    AdditionalInfoItem(
        value = stringResource(R.string.data_sync_warning_v2),
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

@Composable
private fun provideDataErrorItems(
    dataSyncPeriod: Int,
    context: Context,
) = listOf(
    AdditionalInfoItem(
        key = stringResource(R.string.settings_sync_period_v2),
        value = syncPeriodLabel(dataSyncPeriod, context),
        isConstantItem = true,
    ),
    AdditionalInfoItem(
        value = stringResource(R.string.data_sync_error_v2),
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

@Composable
private fun provideSyncInProgressInfo(
    dataSyncPeriod: Int,
    context: Context,
) = listOf(
    AdditionalInfoItem(
        key = stringResource(R.string.settings_sync_period_v2),
        value = syncPeriodLabel(dataSyncPeriod, context),
        isConstantItem = true,
    ),
    AdditionalInfoItem(
        value = stringResource(R.string.syncing_data),
        isConstantItem = true,
    ),
)
