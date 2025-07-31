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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import org.dhis2.R
import org.dhis2.bindings.EVERY_24_HOUR
import org.dhis2.bindings.EVERY_7_DAYS
import org.dhis2.commons.Constants
import org.dhis2.usescases.settings.SettingItem
import org.dhis2.usescases.settings.models.MetadataSettingsViewModel
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.DropdownItem
import org.hisp.dhis.mobile.ui.designsystem.component.InputDropDown
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState

@Composable
internal fun SyncMetadataSettingItem(
    metadataSettings: MetadataSettingsViewModel,
    canInitSync: Boolean,
    isOpened: Boolean,
    onClick: () -> Unit,
    onSyncMetadataClick: () -> Unit,
    onSyncMetaPeriodChanged: (Int) -> Unit,
) {
    SettingItem(
        modifier = Modifier.testTag(SettingItem.META_SYNC.name),
        title = stringResource(id = R.string.settingsSyncMetadata),
        subtitle = buildAnnotatedString {
            val currentMetadataSyncPeriod = syncPeriodLabel(metadataSettings.metadataSyncPeriod)
            when {
                metadataSettings.syncInProgress -> {
                    append(
                        currentMetadataSyncPeriod + "\n" + stringResource(R.string.syncing_configuration),
                    )
                }

                metadataSettings.hasErrors -> {
                    val message =
                        currentMetadataSyncPeriod + "\n" + stringResource(R.string.metadata_sync_error)
                    append(message)
                    addStyle(
                        style = SpanStyle(color = colorResource(R.color.red_060)),
                        start = 0,
                        end = message.length,
                    )
                }

                else -> {
                    append(
                        currentMetadataSyncPeriod + "\n" + String.format(
                            stringResource(R.string.last_data_sync_date),
                            metadataSettings.lastMetadataSync,
                        ),
                    )
                }
            }
        },
        icon = Icons.Outlined.CloudSync,
        extraActions = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = spacedBy(8.dp),
            ) {
                if (metadataSettings.canEdit) {
                    val metaSyncPeriods = listOf(
                        stringResource(R.string.a_day),
                        stringResource(R.string.a_week),
                        stringResource(R.string.Manual),
                    )

                    InputDropDown(
                        modifier = Modifier.testTag(TestTag_MetaPeriod),
                        title = "Title",
                        state = InputShellState.FOCUSED,
                        itemCount = metaSyncPeriods.size,
                        onSearchOption = {},
                        fetchItem = { index ->
                            DropdownItem(metaSyncPeriods[index])
                        },
                        selectedItem = DropdownItem(
                            label = syncPeriodLabel(metadataSettings.metadataSyncPeriod),
                        ),
                        onResetButtonClicked = { },
                        onItemSelected = { index, _ ->
                            when (index) {
                                0 -> onSyncMetaPeriodChanged(EVERY_24_HOUR)
                                1 -> onSyncMetaPeriodChanged(EVERY_7_DAYS)
                                2 -> onSyncMetaPeriodChanged(Constants.TIME_MANUAL)
                                else -> {
                                    /*do nothing*/
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
                    text = stringResource(R.string.SYNC_META)
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
