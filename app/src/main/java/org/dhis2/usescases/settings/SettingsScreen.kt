package org.dhis2.usescases.settings

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ImageSpan
import android.view.animation.OvershootInterpolator
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import org.dhis2.BuildConfig
import org.dhis2.R
import org.dhis2.bindings.EVERY_12_HOUR
import org.dhis2.bindings.EVERY_24_HOUR
import org.dhis2.bindings.EVERY_30_MIN
import org.dhis2.bindings.EVERY_6_HOUR
import org.dhis2.bindings.EVERY_7_DAYS
import org.dhis2.bindings.EVERY_HOUR
import org.dhis2.commons.Constants
import org.dhis2.data.service.SyncResult
import org.dhis2.usescases.settings.models.DataSettingsViewModel
import org.dhis2.usescases.settings.models.ErrorViewModel
import org.dhis2.usescases.settings.models.MetadataSettingsViewModel
import org.dhis2.usescases.settings.models.ReservedValueSettingsViewModel
import org.dhis2.usescases.settings.models.SMSSettingsViewModel
import org.dhis2.usescases.settings.models.SettingsState
import org.dhis2.usescases.settings.models.SyncParametersViewModel
import org.dhis2.usescases.settings.ui.ExportOption
import org.hisp.dhis.android.core.settings.LimitScope
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.ColorStyle
import org.hisp.dhis.mobile.ui.designsystem.component.DropdownItem
import org.hisp.dhis.mobile.ui.designsystem.component.InputDropDown
import org.hisp.dhis.mobile.ui.designsystem.component.InputPhoneNumber
import org.hisp.dhis.mobile.ui.designsystem.component.InputPositiveIntegerOrZero
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.component.InputYesOnlySwitch
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextData
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextState
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import org.hisp.dhis.mobile.ui.designsystem.theme.dropShadow
import java.io.File

internal sealed class SettingsUiAction {
    data class OnItemClick(val settingItem: SettingItem) : SettingsUiAction()
    object SyncData : SettingsUiAction()
    data class OnSyncDataPeriodChanged(val periodInSeconds: Int) : SettingsUiAction()
    object SyncMetadata : SettingsUiAction()
    data class OnSyncMetaPeriodChanged(val periodInSeconds: Int) : SettingsUiAction()
    data class OnSaveLimitScope(val limitScope: LimitScope) : SettingsUiAction()
    data class OnSaveEventMaxCount(val count: Int) : SettingsUiAction()
    data class OnSaveTeiMaxCount(val count: Int) : SettingsUiAction()
    object OnSpecificProgramSettingsClick : SettingsUiAction()
    data class OnSaveReservedValuesToDownload(val count: Int) : SettingsUiAction()
    object OnManageReserveValues : SettingsUiAction()
    object OnOpenErrorLog : SettingsUiAction()
    object OnDownload : SettingsUiAction()
    object OnShare : SettingsUiAction()
    object OnDeleteLocalData : SettingsUiAction()
    object OnCheckVersionUpdates : SettingsUiAction()
    data class EnableSMS(val gateWayNumber: String, val timeout: Int) : SettingsUiAction()
    object DisableSMS : SettingsUiAction()
    data class EnableWaitForResponse(val resultSender: String) : SettingsUiAction()
    object DisableWaitForResponse : SettingsUiAction()
}

@Composable
fun SettingsScreen(
    viewmodel: SyncManagerPresenter,
    checkProgramSpecificSettings: () -> Unit,
    manageReserveValues: () -> Unit,
    showErrorLogs: (List<ErrorViewModel>) -> Unit,
    displayDeleteLocalDataWarning: () -> Unit,
    showShareActions: (file: File) -> Unit,
) {
    val settingsUIModel by viewmodel.settingsState.collectAsState()
    val exportingDatabase by viewmodel.exporting.observeAsState(false)
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewmodel.messageChannel) {
        viewmodel.messageChannel.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(viewmodel.errorLogChannel) {
        viewmodel.errorLogChannel.collect { showErrorLogs(it) }
    }

    LaunchedEffect(viewmodel.fileToShareChannel) {
        viewmodel.fileToShareChannel.collect { showShareActions(it) }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    modifier = Modifier.dropShadow(shape = SnackbarDefaults.shape),
                    snackbarData = data,
                    containerColor = SurfaceColor.SurfaceBright,
                    contentColor = TextColor.OnSurface,
                )
            }
        },
    ) { paddingValues ->

        settingsUIModel?.let {
            SettingItemList(
                modifier = Modifier.padding(paddingValues),
                settingsUIModel = it,
                exportingDatabase = exportingDatabase,
                onSettingsUiAction = { uiAction ->
                    when (uiAction) {
                        is SettingsUiAction.OnItemClick -> viewmodel.onItemClick(uiAction.settingItem)
                        SettingsUiAction.SyncData -> viewmodel.syncData()
                        SettingsUiAction.SyncMetadata -> viewmodel.syncMeta()
                        is SettingsUiAction.OnSaveLimitScope -> viewmodel.saveLimitScope(uiAction.limitScope)
                        is SettingsUiAction.OnSaveEventMaxCount -> viewmodel.saveEventMaxCount(
                            uiAction.count,
                        )

                        is SettingsUiAction.OnSaveTeiMaxCount -> viewmodel.saveTeiMaxCount(
                            uiAction.count,
                        )

                        SettingsUiAction.OnSpecificProgramSettingsClick -> checkProgramSpecificSettings()
                        SettingsUiAction.OnManageReserveValues -> manageReserveValues()
                        SettingsUiAction.OnOpenErrorLog -> viewmodel.checkSyncErrors()
                        is SettingsUiAction.OnSaveReservedValuesToDownload ->
                            viewmodel.saveReservedValues(uiAction.count)

                        SettingsUiAction.OnDownload -> viewmodel.onExportAndDownloadDB()
                        SettingsUiAction.OnShare -> viewmodel.onExportAndShareDB()
                        SettingsUiAction.OnCheckVersionUpdates -> viewmodel.checkVersionUpdate()
                        SettingsUiAction.OnDeleteLocalData -> displayDeleteLocalDataWarning()
                        is SettingsUiAction.OnSyncDataPeriodChanged ->
                            viewmodel.onSyncDataPeriodChanged(uiAction.periodInSeconds)

                        is SettingsUiAction.OnSyncMetaPeriodChanged ->
                            viewmodel.onSyncMetaPeriodChanged(uiAction.periodInSeconds)

                        SettingsUiAction.DisableSMS ->
                            viewmodel.enableSmsModule(false, "", 0)

                        SettingsUiAction.DisableWaitForResponse ->
                            viewmodel.saveWaitForSmsResponse(false, "")

                        is SettingsUiAction.EnableSMS ->
                            viewmodel.enableSmsModule(
                                true,
                                uiAction.gateWayNumber,
                                uiAction.timeout,
                            )

                        is SettingsUiAction.EnableWaitForResponse ->
                            viewmodel.saveWaitForSmsResponse(true, uiAction.resultSender)
                    }
                },
            )
        }
    }
}

@Composable
private fun SettingItemList(
    modifier: Modifier,
    settingsUIModel: SettingsState,
    exportingDatabase: Boolean,
    onSettingsUiAction: (SettingsUiAction) -> Unit,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
            .background(Color.White, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        contentPadding = PaddingValues(top = 16.dp),
    ) {
        item {
            SyncDataSettingItem(
                dataSettings = settingsUIModel.dataSettingsViewModel,
                isOpened = settingsUIModel.openedItem == SettingItem.DATA_SYNC,
                canInitSync = settingsUIModel.canInitDataSync(),
                onClick = { onSettingsUiAction(SettingsUiAction.OnItemClick(SettingItem.DATA_SYNC)) },
                onSyncDataClick = { onSettingsUiAction(SettingsUiAction.SyncData) },
                onSyncDataPeriodChanged = {
                    onSettingsUiAction(SettingsUiAction.OnSyncDataPeriodChanged(it))
                },
            )
        }

        item {
            SyncMetadataSettingItem(
                metadataSettings = settingsUIModel.metadataSettingsViewModel,
                isOpened = settingsUIModel.openedItem == SettingItem.META_SYNC,
                canInitSync = settingsUIModel.canInitMetadataSync(),
                onClick = { onSettingsUiAction(SettingsUiAction.OnItemClick(SettingItem.META_SYNC)) },
                onSyncMetadataClick = { onSettingsUiAction(SettingsUiAction.SyncMetadata) },
                onSyncMetaPeriodChanged = {
                    onSettingsUiAction(SettingsUiAction.OnSyncMetaPeriodChanged(it))
                },
            )
        }

        item {
            SyncParametersSettingItem(
                syncParametersViewModel = settingsUIModel.syncParametersViewModel,
                isOpened = settingsUIModel.openedItem == SettingItem.SYNC_PARAMETERS,
                onClick = { onSettingsUiAction(SettingsUiAction.OnItemClick(SettingItem.SYNC_PARAMETERS)) },
                onScopeLimitSelected = { onSettingsUiAction(SettingsUiAction.OnSaveLimitScope(it)) },
                onEventToDownloadLimitUpdate = {
                    onSettingsUiAction(
                        SettingsUiAction.OnSaveEventMaxCount(
                            it,
                        ),
                    )
                },
                onTeiToDownloadLimitUpdate = {
                    onSettingsUiAction(
                        SettingsUiAction.OnSaveTeiMaxCount(
                            it,
                        ),
                    )
                },
                onSpecificProgramSettingsClick = { onSettingsUiAction(SettingsUiAction.OnSpecificProgramSettingsClick) },
            )
        }

        item {
            ReservedValuesSettingItem(
                reservedValuesSettings = settingsUIModel.reservedValueSettingsViewModel,
                isOpened = settingsUIModel.openedItem == SettingItem.RESERVED_VALUES,
                onClick = { onSettingsUiAction(SettingsUiAction.OnItemClick(SettingItem.RESERVED_VALUES)) },
                onReservedValuesToDownloadUpdate = {
                    onSettingsUiAction(
                        SettingsUiAction.OnSaveReservedValuesToDownload(
                            it,
                        ),
                    )
                },
                onManageReservedValuesClick = { onSettingsUiAction(SettingsUiAction.OnManageReserveValues) },
            )
        }

        item {
            OpenSyncErrorLogSettingItem(
                onClick = { onSettingsUiAction(SettingsUiAction.OnOpenErrorLog) },
            )
        }

        item {
            ExportDatabaseSettingsSettingItem(
                displayProgress = exportingDatabase,
                isOpened = settingsUIModel.openedItem == SettingItem.EXPORT_DB,
                onClick = { onSettingsUiAction(SettingsUiAction.OnItemClick(SettingItem.EXPORT_DB)) },
                onShare = { onSettingsUiAction(SettingsUiAction.OnShare) },
                onDownload = { onSettingsUiAction(SettingsUiAction.OnDownload) },
            )
        }

        item {
            DeleteLocalDatabaseSettingItem(
                isOpened = settingsUIModel.openedItem == SettingItem.DELETE_LOCAL_DATA,
                onClick = { onSettingsUiAction(SettingsUiAction.OnItemClick(SettingItem.DELETE_LOCAL_DATA)) },
                onDeleteLocalDataClick = { onSettingsUiAction(SettingsUiAction.OnDeleteLocalData) },
            )
        }

        item {
            SMSSettingItem(
                smsSettings = settingsUIModel.smsSettingsViewModel,
                isOpened = settingsUIModel.openedItem == SettingItem.SMS,
                onClick = { onSettingsUiAction(SettingsUiAction.OnItemClick(SettingItem.SMS)) },
                enableSms = { gateWayNumber, timeout ->
                    onSettingsUiAction(SettingsUiAction.EnableSMS(gateWayNumber, timeout))
                },
                disableSms = {
                    onSettingsUiAction(SettingsUiAction.DisableSMS)
                },
                enableWaitForResponse = { resultSender ->
                    onSettingsUiAction(SettingsUiAction.EnableWaitForResponse(resultSender))
                },
                disableWaitForResponse = {
                    onSettingsUiAction(SettingsUiAction.DisableWaitForResponse)
                },
            )
        }

        item {
            AppUpdateSettingItem(
                isOpened = settingsUIModel.openedItem == SettingItem.VERSION_UPDATE,
                onClick = { onSettingsUiAction(SettingsUiAction.OnItemClick(SettingItem.VERSION_UPDATE)) },
                onCheckVersionUpdate = { onSettingsUiAction(SettingsUiAction.OnCheckVersionUpdates) },
            )
        }
    }
}

@Composable
private fun syncPeriodLabel(currentDataSyncPeriod: Int): String {
    val setting = when (currentDataSyncPeriod) {
        EVERY_30_MIN -> stringResource(R.string.thirty_minutes)
        EVERY_HOUR -> stringResource(R.string.a_hour)
        EVERY_6_HOUR -> stringResource(R.string.every_6_hours)
        EVERY_12_HOUR -> stringResource(R.string.every_12_hours)
        Constants.TIME_MANUAL -> stringResource(R.string.Manual)
        EVERY_24_HOUR -> stringResource(R.string.a_day)
        EVERY_7_DAYS -> stringResource(R.string.a_week)
        else -> stringResource(R.string.a_day)
    }

    return String.format(stringResource(R.string.settings_sync_period) + ": %s", setting)
}

@Composable
private fun SyncDataSettingItem(
    dataSettings: DataSettingsViewModel,
    canInitSync: Boolean,
    isOpened: Boolean,
    onClick: () -> Unit,
    onSyncDataClick: () -> Unit,
    onSyncDataPeriodChanged: (Int) -> Unit,
) {
    val context = LocalContext.current
    SettingItem(
        title = stringResource(id = R.string.settingsSyncData),
        subtitle = buildAnnotatedString {
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
                        currentDataSyncPeriod + "\n" + String.format(
                            stringResource(R.string.last_data_sync_date),
                            dataSettings.lastDataSync,
                        ),
                    )
                }
            }
        },
        iconId = R.drawable.ic_sync_data,
        extraActions = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = spacedBy(8.dp),
            ) {
                if (dataSettings.canEdit) {
                    val dataSyncPeriods = listOf(
                        stringResource(R.string.thirty_minutes),
                        stringResource(R.string.a_hour),
                        stringResource(R.string.every_6_hours),
                        stringResource(R.string.every_12_hours),
                        stringResource(R.string.a_day),
                        stringResource(R.string.Manual),
                    )
                    InputDropDown(
                        title = stringResource(R.string.settings_sync_period),
                        state = InputShellState.FOCUSED,
                        itemCount = dataSyncPeriods.size,
                        onSearchOption = {},
                        fetchItem = { index ->
                            DropdownItem(dataSyncPeriods[index])
                        },
                        selectedItem = DropdownItem(
                            label = syncPeriodLabel(dataSettings.dataSyncPeriod),
                        ),
                        onResetButtonClicked = { },
                        onItemSelected = { index, dropdownItem ->
                            when (index) {
                                0 -> onSyncDataPeriodChanged(EVERY_30_MIN)
                                1 -> onSyncDataPeriodChanged(EVERY_HOUR)
                                2 -> onSyncDataPeriodChanged(EVERY_6_HOUR)
                                3 -> onSyncDataPeriodChanged(EVERY_12_HOUR)
                                4 -> onSyncDataPeriodChanged(EVERY_24_HOUR)
                                5 -> onSyncDataPeriodChanged(Constants.TIME_MANUAL)
                                else -> {}
                            }
                        },
                        showSearchBar = false,
                        loadOptions = {},
                    )
                } else {
                    Text(text = stringResource(R.string.syncing_period_not_editable))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Button(
                        text = stringResource(R.string.SYNC_DATA).uppercase(),
                        enabled = canInitSync,
                        onClick = onSyncDataClick,
                    )
                }
            }
        },
        showExtraActions = isOpened,
        onClick = onClick,
    )
}

@Composable
private fun SyncMetadataSettingItem(
    metadataSettings: MetadataSettingsViewModel,
    canInitSync: Boolean,
    isOpened: Boolean,
    onClick: () -> Unit,
    onSyncMetadataClick: () -> Unit,
    onSyncMetaPeriodChanged: (Int) -> Unit,
) {
    SettingItem(
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
        iconId = R.drawable.ic_sync_configuration,
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
                        onItemSelected = { index, dropdownItem ->
                            when (index) {
                                0 -> onSyncMetaPeriodChanged(EVERY_24_HOUR)
                                1 -> onSyncMetaPeriodChanged(EVERY_7_DAYS)
                                2 -> onSyncMetaPeriodChanged(Constants.TIME_MANUAL)
                                else -> {}
                            }
                        },
                        showSearchBar = false,
                        loadOptions = {},
                    )
                } else {
                    Text(text = stringResource(R.string.syncing_period_not_editable))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Button(
                        text = stringResource(R.string.SYNC_META).uppercase(),
                        enabled = canInitSync,
                        onClick = onSyncMetadataClick,
                    )
                }
            }
        },
        showExtraActions = isOpened,
        onClick = onClick,
    )
}

@Composable
private fun SyncParametersSettingItem(
    syncParametersViewModel: SyncParametersViewModel,
    isOpened: Boolean,
    onClick: () -> Unit,
    onScopeLimitSelected: (LimitScope) -> Unit,
    onEventToDownloadLimitUpdate: (Int) -> Unit,
    onTeiToDownloadLimitUpdate: (Int) -> Unit,
    onSpecificProgramSettingsClick: () -> Unit,
) {
    val limitScopeLabel = when (syncParametersViewModel.limitScope) {
        LimitScope.ALL_ORG_UNITS,
        LimitScope.GLOBAL,
        -> stringResource(R.string.settings_limit_globally)

        LimitScope.PER_ORG_UNIT -> stringResource(R.string.settings_limit_ou)
        LimitScope.PER_PROGRAM -> stringResource(R.string.settings_limit_program)
        LimitScope.PER_OU_AND_PROGRAM -> stringResource(R.string.settings_limit_ou_program)
    }
    SettingItem(
        title = stringResource(id = R.string.settingsSyncParameters),
        subtitle = stringResource(R.string.event_tei_limits_v2).format(
            limitScopeLabel,
            syncParametersViewModel.currentEventCount,
            syncParametersViewModel.numberOfEventsToDownload,
            syncParametersViewModel.currentTeiCount,
            syncParametersViewModel.numberOfTeiToDownload,
        ),
        iconId = R.drawable.ic_sync_parameters,
        extraActions = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = spacedBy(8.dp),
            ) {
                if (syncParametersViewModel.limitScopeIsEditable) {
                    val downloadLimitScopes = listOf(
                        stringResource(R.string.settings_limit_globally),
                        stringResource(R.string.settings_limit_ou),
                        stringResource(R.string.settings_limit_program),
                        stringResource(R.string.settings_limit_ou_program),
                    )
                    InputDropDown(
                        title = stringResource(R.string.settings_limit_scope),
                        state = InputShellState.FOCUSED,
                        itemCount = downloadLimitScopes.size,
                        onSearchOption = {},
                        fetchItem = { index ->
                            DropdownItem(downloadLimitScopes[index])
                        },
                        selectedItem = DropdownItem(
                            label = when (syncParametersViewModel.limitScope) {
                                LimitScope.ALL_ORG_UNITS,
                                LimitScope.GLOBAL,
                                -> downloadLimitScopes[0]

                                LimitScope.PER_ORG_UNIT -> downloadLimitScopes[1]
                                LimitScope.PER_PROGRAM -> downloadLimitScopes[2]
                                LimitScope.PER_OU_AND_PROGRAM -> downloadLimitScopes[3]
                            },
                        ),
                        onResetButtonClicked = {
                            onScopeLimitSelected(LimitScope.GLOBAL)
                        },
                        onItemSelected = { index, dropdownItem ->
                            val selectedScope = when (index) {
                                0 -> LimitScope.GLOBAL
                                1 -> LimitScope.PER_ORG_UNIT
                                2 -> LimitScope.PER_PROGRAM
                                3 -> LimitScope.PER_OU_AND_PROGRAM
                                else -> LimitScope.GLOBAL
                            }
                            onScopeLimitSelected(selectedScope)
                        },
                        loadOptions = {},
                    )

                    InputPositiveIntegerOrZero(
                        title = stringResource(R.string.events_to_download),
                        state = InputShellState.FOCUSED,
                        inputTextFieldValue = TextFieldValue(text = syncParametersViewModel.numberOfEventsToDownload.toString()),
                        onValueChanged = { fieldValue ->
                            onEventToDownloadLimitUpdate(
                                fieldValue?.text?.toIntOrNull() ?: 0,
                            )
                        },
                        imeAction = ImeAction.Done,
                    )

                    InputPositiveIntegerOrZero(
                        title = stringResource(R.string.teis_to_download),
                        state = InputShellState.FOCUSED,
                        inputTextFieldValue = TextFieldValue(text = syncParametersViewModel.numberOfTeiToDownload.toString()),
                        onValueChanged = { fieldValue ->
                            onTeiToDownloadLimitUpdate(
                                fieldValue?.text?.toIntOrNull() ?: 0,
                            )
                        },
                        imeAction = ImeAction.Done,
                    )
                } else {
                    Text(text = stringResource(R.string.sync_parameters_not_editable))
                }
                if (syncParametersViewModel.hasSpecificProgramSettings > 0) {
                    Text(
                        text = buildAnnotatedString {
                            val specificProgramText = LocalContext.current.resources.getQuantityString(
                                R.plurals.settings_specific_programs,
                                syncParametersViewModel.hasSpecificProgramSettings,
                            ).format(syncParametersViewModel.hasSpecificProgramSettings)
                            append(
                                LocalContext.current.resources.getQuantityString(
                                    R.plurals.settings_specific_programs,
                                    syncParametersViewModel.hasSpecificProgramSettings,
                                ).format(syncParametersViewModel.hasSpecificProgramSettings),
                            )
                            val indexOfNumber =
                                specificProgramText.indexOf(syncParametersViewModel.hasSpecificProgramSettings.toString())
                            addStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    textDecoration = TextDecoration.None,
                                ),
                                start = indexOfNumber,
                                end = indexOfNumber + indexOfNumber.toString().length,
                            )
                        },
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Button(
                            text = stringResource(R.string.program_settings).uppercase(),
                            style = ButtonStyle.TEXT,
                            enabled = true,
                            onClick = onSpecificProgramSettingsClick,
                        )
                    }
                }
            }
        },
        showExtraActions = isOpened,
        onClick = onClick,
    )
}

@Composable
private fun ReservedValuesSettingItem(
    reservedValuesSettings: ReservedValueSettingsViewModel,
    isOpened: Boolean,
    onClick: () -> Unit,
    onReservedValuesToDownloadUpdate: (Int) -> Unit,
    onManageReservedValuesClick: () -> Unit,
) {
    SettingItem(
        title = stringResource(id = R.string.settingsReservedValues),
        subtitle = stringResource(id = R.string.settingsReservedValues_descr),
        iconId = R.drawable.ic_reserved_values,
        extraActions = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = spacedBy(8.dp),
            ) {
                if (!reservedValuesSettings.canBeEdited) {
                    InputPositiveIntegerOrZero(
                        title = stringResource(R.string.reserved_values_hint),
                        state = InputShellState.FOCUSED,
                        inputTextFieldValue = TextFieldValue(text = reservedValuesSettings.numberOfReservedValuesToDownload.toString()),
                        onValueChanged = { fieldValue ->
                            onReservedValuesToDownloadUpdate(
                                fieldValue?.text?.toIntOrNull() ?: 0,
                            )
                        },
                        imeAction = ImeAction.Done,
                    )
                } else {
                    Text(text = stringResource(R.string.rv_no_editable))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Button(
                        text = stringResource(R.string.manage_reserved_values_button).uppercase(),
                        style = ButtonStyle.TEXT,
                        enabled = true,
                        onClick = onManageReservedValuesClick,
                    )
                }
            }
        },
        showExtraActions = isOpened,
        onClick = onClick,
    )
}

@Composable
private fun OpenSyncErrorLogSettingItem(
    onClick: () -> Unit,
) {
    SettingItem(
        title = stringResource(id = R.string.settingsErrorLog),
        subtitle = stringResource(R.string.settingsErrorLog_descr),
        iconId = R.drawable.ic_open_sync_error_log,
        extraActions = {
            /*no extra actions*/
        },
        showExtraActions = false,
        onClick = onClick,
    )
}

@Composable
private fun ExportDatabaseSettingsSettingItem(
    isOpened: Boolean,
    displayProgress: Boolean,
    onClick: () -> Unit,
    onShare: () -> Unit,
    onDownload: () -> Unit,
) {
    SettingItem(
        title = stringResource(id = R.string.settingsExportDB),
        subtitle = stringResource(R.string.settingsExportDBMessage),
        iconId = R.drawable.ic_settings_export,
        extraActions = {
            ExportOption(onShare, onDownload, displayProgress)
        },
        showExtraActions = isOpened,
        onClick = onClick,
    )
}

@Composable
private fun DeleteLocalDatabaseSettingItem(
    isOpened: Boolean,
    onClick: () -> Unit,
    onDeleteLocalDataClick: () -> Unit,
) {
    SettingItem(
        title = stringResource(id = R.string.settingsDeleteLocalData),
        subtitle = stringResource(R.string.settingsDeleteLocalData_descr),
        iconId = R.drawable.ic_delete_local_data,
        extraActions = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Button(
                    text = stringResource(R.string.action_accept).uppercase(),
                    colorStyle = ColorStyle.ERROR,
                    style = ButtonStyle.TEXT,
                    enabled = true,
                    onClick = onDeleteLocalDataClick,
                )
            }
        },
        showExtraActions = isOpened,
        onClick = onClick,
    )
}

@Composable
private fun SMSSettingItem(
    smsSettings: SMSSettingsViewModel,
    isOpened: Boolean,
    onClick: () -> Unit,
    enableSms: (gatewayNumber: String, timeout: Int) -> Unit,
    disableSms: () -> Unit,
    enableWaitForResponse: (resultSenderNumber: String) -> Unit,
    disableWaitForResponse: () -> Unit,
) {
    var gatewayNumber by remember(smsSettings) {
        mutableStateOf(smsSettings.gatewayNumber)
    }

    var resultTimeout by remember(smsSettings) {
        mutableStateOf(smsSettings.responseTimeout)
    }

    var smsEnabled by remember(smsSettings) {
        mutableStateOf(smsSettings.isEnabled)
    }

    var resultSender by remember(smsSettings) {
        mutableStateOf(smsSettings.responseNumber)
    }

    var waitForResponse by remember(smsSettings) {
        mutableStateOf(smsSettings.waitingForResponse)
    }

    var gatewayValidation by remember(smsSettings) {
        mutableStateOf(smsSettings.gatewayValidationResult)
    }

    var gateWayState by remember(smsSettings, gatewayValidation) {
        mutableStateOf(
            when {
                gatewayValidation != GatewayValidator.GatewayValidationResult.Valid -> InputShellState.ERROR
                smsSettings.isGatewayNumberEditable -> InputShellState.FOCUSED
                else -> InputShellState.DISABLED
            },
        )
    }

    SettingItem(
        title = stringResource(id = R.string.settingsSms),
        subtitle = stringResource(R.string.settingsSms_descr),
        iconId = R.drawable.ic_setting_sms,
        extraActions = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = spacedBy(8.dp),
            ) {
                InputPhoneNumber(
                    title = stringResource(R.string.settings_sms_receiver_number),
                    onCallActionClicked = {},
                    state = gateWayState,
                    inputTextFieldValue = TextFieldValue(
                        text = gatewayNumber,
                        selection = TextRange(gatewayNumber.length),
                    ),
                    onValueChanged = {
                        gatewayValidation = GatewayValidator.GatewayValidationResult.Valid
                        gatewayNumber = it?.text ?: ""
                    },
                    imeAction = ImeAction.Done,
                    supportingText = phoneNumberValidationMessage(gatewayValidation),
                )
                InputPositiveIntegerOrZero(
                    title = stringResource(R.string.settings_sms_result_timeout),
                    state = if (smsSettings.isGatewayNumberEditable) InputShellState.FOCUSED else InputShellState.DISABLED,
                    inputTextFieldValue = TextFieldValue(
                        text = resultTimeout.toString(),
                        selection = TextRange(resultTimeout.toString().length),
                    ),
                    onValueChanged = {
                        resultTimeout = it?.text?.toIntOrNull() ?: 0
                    },
                    imeAction = ImeAction.Done,
                )
                InputYesOnlySwitch(
                    title = stringResource(R.string.settings_sms_module_switch),
                    state = if (smsSettings.isGatewayNumberEditable && gatewayNumber.isNotEmpty()) InputShellState.FOCUSED else InputShellState.DISABLED,
                    isChecked = smsEnabled,
                    onClick = {
                        if (smsEnabled) {
                            disableSms()
                        } else {
                            enableSms(gatewayNumber, resultTimeout)
                        }
                        smsEnabled = !smsEnabled
                    },
                )
                InputPhoneNumber(
                    title = stringResource(R.string.settings_sms_result_sender_number),
                    onCallActionClicked = {},
                    state = if (smsSettings.isResponseNumberEditable) InputShellState.FOCUSED else InputShellState.DISABLED,
                    inputTextFieldValue = TextFieldValue(
                        text = resultSender,
                        selection = TextRange(resultSender.length),
                    ),
                    onValueChanged = {
                        resultSender = it?.text ?: ""
                    },
                    imeAction = ImeAction.Done,
                    supportingText = phoneNumberValidationMessage(smsSettings.resultSenderValidationResult),
                )
                InputYesOnlySwitch(
                    title = stringResource(R.string.settings_sms_response_wait_switch),
                    state = if (smsSettings.isResponseNumberEditable && resultSender.isNotEmpty()) InputShellState.FOCUSED else InputShellState.DISABLED,
                    isChecked = waitForResponse,
                    onClick = {
                        if (waitForResponse) {
                            disableWaitForResponse()
                        } else {
                            enableWaitForResponse(resultSender)
                        }
                        waitForResponse = !smsSettings.waitingForResponse
                    },
                )
            }
        },
        showExtraActions = isOpened,
        onClick = onClick,
    )
}

@Composable
private fun phoneNumberValidationMessage(validation: GatewayValidator.GatewayValidationResult) =
    when (validation) {
        GatewayValidator.GatewayValidationResult.Empty ->
            listOf(
                SupportingTextData(
                    text = stringResource(R.string.sms_empty_gateway),
                    state = SupportingTextState.ERROR,
                ),
            )

        GatewayValidator.GatewayValidationResult.Invalid ->
            listOf(
                SupportingTextData(
                    text = stringResource(R.string.invalid_phone_number),
                    state = SupportingTextState.ERROR,
                ),
            )

        GatewayValidator.GatewayValidationResult.Valid -> null
    }

@Composable
private fun AppUpdateSettingItem(
    isOpened: Boolean,
    onClick: () -> Unit,
    onCheckVersionUpdate: () -> Unit,
) {
    SettingItem(
        title = stringResource(id = R.string.settingsCheckVersion),
        subtitle = buildAnnotatedString {
            val description = "${stringResource(R.string.app_version)} ${BuildConfig.VERSION_NAME}"
            append(description)
            addStyle(
                style = SpanStyle(MaterialTheme.colorScheme.primary),
                start = description.indexOf(BuildConfig.VERSION_NAME),
                end = description.length,
            )
        },
        iconId = R.drawable.ic_software_update_menu,
        extraActions = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Button(
                    text = stringResource(R.string.check_for_updates),
                    enabled = true,
                    onClick = onCheckVersionUpdate,
                )
            }
        },
        showExtraActions = isOpened,
        onClick = onClick,
    )
}

@Composable
private fun SettingItem(
    title: String,
    subtitle: AnnotatedString,
    @DrawableRes iconId: Int,
    extraActions: @Composable () -> Unit,
    showExtraActions: Boolean,
    onClick: () -> Unit,
) {
    SettingItem(
        title = title,
        subtitle = {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
            )
        },
        iconId = iconId,
        extraActions = extraActions,
        showExtraActions = showExtraActions,
        onClick = onClick,
    )
}

@Composable
private fun SettingItem(
    title: String,
    subtitle: String,
    @DrawableRes iconId: Int,
    extraActions: @Composable () -> Unit,
    showExtraActions: Boolean,
    onClick: () -> Unit,
) {
    SettingItem(
        title = title,
        subtitle = {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
            )
        },
        iconId = iconId,
        extraActions = extraActions,
        showExtraActions = showExtraActions,
        onClick = onClick,
    )
}

@Composable
private fun SettingItem(
    title: String,
    subtitle: @Composable () -> Unit,
    @DrawableRes iconId: Int,
    extraActions: @Composable () -> Unit,
    showExtraActions: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .wrapContentHeight()
            .background(Color.White),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = spacedBy(16.dp),
        ) {
            Icon(
                modifier = Modifier.size(40.dp),
                imageVector = ImageVector.vectorResource(id = iconId),
                contentDescription = null,
            )
            Column(modifier = Modifier.height(intrinsicSize = IntrinsicSize.Max)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                )
                subtitle()
            }
        }
        AnimatedVisibility(
            showExtraActions,
            enter = expandVertically(
                expandFrom = Alignment.Top,
                animationSpec = tween(
                    easing = {
                        OvershootInterpolator().getInterpolation(it)
                    },
                ),
            ),
            exit = shrinkVertically(shrinkTowards = Alignment.Top),
        ) {
            Box(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(16.dp),
                    )
                    .fillMaxWidth()
                    .padding(
                        start = 72.dp,
                        top = 8.dp,
                        bottom = 8.dp,
                        end = 16.dp,
                    ),
            ) {
                extraActions()
            }
        }
        if (!showExtraActions) {
            HorizontalDivider(modifier = Modifier.padding(start = 72.dp))
        }
    }
}
