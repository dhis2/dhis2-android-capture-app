package org.dhis2.usescases.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.dhis2.usescases.settings.SettingItem
import org.dhis2.usescases.settings.SyncManagerPresenter
import org.dhis2.usescases.settings.models.DeleteDataState
import org.dhis2.usescases.settings.models.ErrorViewModel
import org.dhis2.usescases.settings.models.SettingsState
import org.dhis2.usescases.settings.models.SettingsUiAction
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import org.hisp.dhis.mobile.ui.designsystem.theme.dropShadow
import java.io.File

const val TEST_TAG_DATA_PERIOD = "TestTag_DataPeriod"
const val TEST_TAG_META_PERIOD = "TestTag_MetaPeriod"
const val TEST_TAG_SYNC_PARAMETERS_LIMIT_SCOPE = "TestTag_SyncParameters_LimitScope"
const val TEST_TAG_SYNC_PARAMETERS_EVENT_MAX_COUNT = "TestTag_SyncParameters_EventMaxCount"
const val TEST_TAG_SYNC_PARAMETERS_TEI_MAX_COUNT = "TestTag_SyncParameters_TeiMaxCount"

@Composable
fun SettingsScreen(
    viewmodel: SyncManagerPresenter,
    checkProgramSpecificSettings: () -> Unit,
    manageReserveValues: () -> Unit,
    showErrorLogs: (List<ErrorViewModel>) -> Unit,
    showShareActions: (file: File) -> Unit,
    display2FASettingsScreen: () -> Unit,
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
        modifier = Modifier.fillMaxSize(),
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
                modifier = Modifier,
                contentPadding = paddingValues,
                settingsUIModel = it,
                exportingDatabase = exportingDatabase,
                onSettingsUiAction = { uiAction ->
                    when (uiAction) {
                        is SettingsUiAction.OnItemClick -> viewmodel.onItemClick(uiAction.settingItem)
                        SettingsUiAction.SyncData -> viewmodel.syncData()
                        SettingsUiAction.SyncMetadata -> viewmodel.syncMeta()
                        is SettingsUiAction.OnSaveLimitScope -> viewmodel.saveLimitScope(uiAction.limitScope)
                        is SettingsUiAction.OnSaveEventMaxCount ->
                            viewmodel.saveEventMaxCount(
                                uiAction.count,
                            )

                        is SettingsUiAction.OnSaveTeiMaxCount ->
                            viewmodel.saveTeiMaxCount(
                                uiAction.count,
                            )

                        SettingsUiAction.OnSpecificProgramSettingsClick -> checkProgramSpecificSettings()
                        SettingsUiAction.OnManageReserveValues -> manageReserveValues()
                        SettingsUiAction.OnOpenErrorLog -> viewmodel.checkSyncErrors()
                        SettingsUiAction.OnOpenTwoFASettings -> {
                            viewmodel.onItemClick(SettingItem.TWO_FACTOR_AUTH)
                            display2FASettingsScreen()
                        }

                        is SettingsUiAction.OnSaveReservedValuesToDownload ->
                            viewmodel.saveReservedValues(uiAction.count)

                        SettingsUiAction.OnDownload -> viewmodel.onExportAndDownloadDB()
                        SettingsUiAction.OnShare -> viewmodel.onExportAndShareDB()
                        SettingsUiAction.OnCheckVersionUpdates -> viewmodel.onCheckVersionUpdate()
                        SettingsUiAction.OnDeleteLocalData -> viewmodel.onDeleteLocalData()
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

                        is SettingsUiAction.SaveGateway ->
                            viewmodel.saveGatewayNumber(uiAction.gatewayNumber)

                        is SettingsUiAction.SaveResultSender ->
                            viewmodel.saveResultSender(uiAction.resultSender)

                        is SettingsUiAction.SaveTimeout ->
                            viewmodel.saveTimeout(uiAction.timeout)
                    }
                },
            )

            if (it.deleteDataState !is DeleteDataState.None) {
                DeleteLocalDataDialog(
                    isDeletingLocalData = it.deleteDataState is DeleteDataState.Deleting,
                    onDeleteLocalData = viewmodel::deleteLocalData,
                    onDismissRequest = viewmodel::onDismissLocalData,
                )
            }
        }
    }
}

@Composable
private fun SettingItemList(
    modifier: Modifier,
    contentPadding: PaddingValues,
    settingsUIModel: SettingsState,
    exportingDatabase: Boolean,
    onSettingsUiAction: (SettingsUiAction) -> Unit,
) {
    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .imePadding()
                .background(MaterialTheme.colorScheme.primary)
                .background(Color.White, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = spacedBy(Spacing.Spacing4),
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

        if (settingsUIModel.isTwoFAConfigured) {
            item {
                TwoFASettingItem(
                    onClick = { onSettingsUiAction(SettingsUiAction.OnOpenTwoFASettings) },
                )
            }
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
                saveGatewayNumber = { gatewayNumber ->
                    onSettingsUiAction(SettingsUiAction.SaveGateway(gatewayNumber))
                },
                saveTimeout = { timeout ->
                    onSettingsUiAction(SettingsUiAction.SaveTimeout(timeout))
                },
                disableSms = {
                    onSettingsUiAction(SettingsUiAction.DisableSMS)
                },
                saveResultSender = { resultSender ->
                    onSettingsUiAction(SettingsUiAction.SaveResultSender(resultSender))
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
                versionName = settingsUIModel.versionName,
                isOpened = settingsUIModel.openedItem == SettingItem.VERSION_UPDATE,
                onClick = { onSettingsUiAction(SettingsUiAction.OnItemClick(SettingItem.VERSION_UPDATE)) },
                onCheckVersionUpdate = { onSettingsUiAction(SettingsUiAction.OnCheckVersionUpdates) },
            )
        }
    }
}
