package org.dhis2.usescases.settings.ui

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import org.dhis2.R
import org.dhis2.bindings.EVERY_24_HOUR
import org.dhis2.bindings.EVERY_7_DAYS
import org.dhis2.commons.Constants
import org.dhis2.usescases.settings.SettingItem
import org.dhis2.usescases.settings.models.MetadataSettingsViewModel
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItemColor
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.DropdownItem
import org.hisp.dhis.mobile.ui.designsystem.component.InputDropDown
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor

@Composable
internal fun SyncMetadataSettingItem(
    metadataSettings: MetadataSettingsViewModel,
    canInitSync: Boolean,
    isOpened: Boolean,
    onClick: () -> Unit,
    onSyncMetadataClick: () -> Unit,
    onSyncMetaPeriodChanged: (Int) -> Unit,
) {
    val additionalInfoList = when {
        metadataSettings.syncInProgress -> {
            listOf(
                AdditionalInfoItem(
                    key = stringResource(R.string.settings_sync_period_v2),
                    value = syncPeriodLabel(metadataSettings.metadataSyncPeriod),
                    isConstantItem = true,
                    ),
                AdditionalInfoItem(
                    value = stringResource(R.string.syncing_configuration),
                    isConstantItem = true,
                ),
            )
        }

        metadataSettings.hasErrors -> {
            listOf(
                AdditionalInfoItem(
                    key = stringResource(R.string.settings_sync_period_v2),
                        value = syncPeriodLabel(metadataSettings.metadataSyncPeriod),
                        isConstantItem = true,
                ),
                AdditionalInfoItem(
                    value = stringResource(R.string.metadata_sync_error),
                    isConstantItem = true,
                    color = AdditionalInfoItemColor.ERROR.color,
                ),
            )
        }

        else -> {
            listOf(
                AdditionalInfoItem(
                    key = stringResource(R.string.settings_sync_period_v2),
                    value = syncPeriodLabel(metadataSettings.metadataSyncPeriod),
                ),
                AdditionalInfoItem(
                    key = stringResource(R.string.last_data_sync),
                    value = metadataSettings.lastMetadataSync,
                    color = TextColor.OnSurface,
                ),
            )
        }
    }

    SettingItem(
        modifier = Modifier.testTag(SettingItem.META_SYNC.name),
        title = stringResource(id = R.string.settingsSyncMetadata),
            additionalInfoList = additionalInfoList,
        icon = Icons.Outlined.CloudSync,
        extraActions = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = spacedBy(8.dp),
            ) {
                if (metadataSettings.canEdit) {
                    val metaSyncPeriods =
                        listOf(
                            stringResource(R.string.a_day),
                            stringResource(R.string.a_week),
                            stringResource(R.string.Manual),
                        )

                    InputDropDown(
                        modifier = Modifier.testTag(TEST_TAG_META_PERIOD),
                        title = "Title",
                        state = InputShellState.FOCUSED,
                        itemCount = metaSyncPeriods.size,
                        onSearchOption = {},
                        fetchItem = { index ->
                            DropdownItem(metaSyncPeriods[index])
                        },
                        selectedItem =
                            DropdownItem(
                                label = syncPeriodLabel(metadataSettings.metadataSyncPeriod),
                            ),
                        onResetButtonClicked = { },
                        onItemSelected = { index, _ ->
                            when (index) {
                                0 -> onSyncMetaPeriodChanged(EVERY_24_HOUR)
                                1 -> onSyncMetaPeriodChanged(EVERY_7_DAYS)
                                2 -> onSyncMetaPeriodChanged(Constants.TIME_MANUAL)
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
                        stringResource(R.string.SYNC_META)
                            .lowercase()
                            .capitalize(Locale.current),
                    style = ButtonStyle.TONAL,
                    enabled = canInitSync,
                    onClick = onSyncMetadataClick,
                )
            }
        },
        showExtraActions = isOpened,
        onClick = onClick,
    )
}
