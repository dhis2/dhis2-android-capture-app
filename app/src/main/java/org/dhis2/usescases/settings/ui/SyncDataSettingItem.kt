package org.dhis2.usescases.settings.ui

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ImageSpan
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import org.dhis2.R
import org.dhis2.bindings.EVERY_12_HOUR
import org.dhis2.bindings.EVERY_24_HOUR
import org.dhis2.bindings.EVERY_30_MIN
import org.dhis2.bindings.EVERY_6_HOUR
import org.dhis2.bindings.EVERY_HOUR
import org.dhis2.commons.Constants
import org.dhis2.data.service.SyncResult
import org.dhis2.usescases.settings.SettingItem
import org.dhis2.usescases.settings.models.DataSettingsViewModel
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.DropdownItem
import org.hisp.dhis.mobile.ui.designsystem.component.InputDropDown
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState

@Composable
internal fun SyncDataSettingItem(
    dataSettings: DataSettingsViewModel,
    canInitSync: Boolean,
    isOpened: Boolean,
    onClick: () -> Unit,
    onSyncDataClick: () -> Unit,
    onSyncDataPeriodChanged: (Int) -> Unit,
) {
    val context = LocalContext.current
    SettingItem(
        modifier =
            Modifier.semantics {
                testTag = SettingItem.DATA_SYNC.name
            },
        title = stringResource(id = R.string.settingsSyncData),
        subtitle =
            buildAnnotatedString {
                val currentDataSyncPeriod = syncPeriodLabel(dataSettings.dataSyncPeriod)
                when {
                    dataSettings.syncInProgress -> {
                        append(currentDataSyncPeriod + "\n" + stringResource(R.string.syncing_data))
                    }

                    dataSettings.dataHasErrors -> {
                        val src =
                            currentDataSyncPeriod + "\n" + stringResource(R.string.data_sync_error)
                        val str = SpannableString(src)
                        val wIndex = src.indexOf('@')
                        val eIndex = src.indexOf('$')
                        str.setSpan(
                            ImageSpan(context, R.drawable.ic_sync_warning),
                            wIndex,
                            wIndex + 1,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                        )
                        str.setSpan(
                            ImageSpan(context, R.drawable.ic_sync_problem_red),
                            eIndex,
                            eIndex + 1,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                        )
                        append(str)
                        addStyle(
                            style = SpanStyle(color = colorResource(R.color.red_060)),
                            start = 0,
                            end = str.length,
                        )
                    }

                    dataSettings.dataHasWarnings -> {
                        val src =
                            currentDataSyncPeriod + "\n" + stringResource(R.string.data_sync_warning)
                        val str = SpannableString(src)
                        val wIndex = src.indexOf('@')
                        str.setSpan(
                            ImageSpan(context, R.drawable.ic_sync_warning),
                            wIndex,
                            wIndex + 1,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                        )
                        append(str)
                        addStyle(
                            style = SpanStyle(color = colorResource(R.color.colorPrimaryOrange)),
                            start = 0,
                            end = str.length,
                        )
                    }

                    dataSettings.syncHasErrors && dataSettings.syncResult == SyncResult.INCOMPLETE -> {
                        append(stringResource(R.string.sync_incomplete_error_text))
                    }

                    dataSettings.syncHasErrors && dataSettings.syncResult == SyncResult.ERROR -> {
                        append(stringResource(R.string.sync_error_text))
                        addStyle(
                            style = SpanStyle(color = colorResource(R.color.red_060)),
                            start = 0,
                            end = stringResource(R.string.sync_error_text).length,
                        )
                    }

                    else -> {
                        append(
                            currentDataSyncPeriod + "\n" +
                                String.format(
                                    stringResource(R.string.last_data_sync_date),
                                    dataSettings.lastDataSync,
                                ),
                        )
                    }
                }
            },
        icon = Icons.Outlined.Update,
        extraActions = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = spacedBy(8.dp),
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
