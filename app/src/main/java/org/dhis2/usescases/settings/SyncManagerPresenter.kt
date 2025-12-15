package org.dhis2.usescases.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dhis2.commons.Constants
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.usescases.settings.domain.CheckVersionUpdate
import org.dhis2.usescases.settings.domain.DeleteLocalData
import org.dhis2.usescases.settings.domain.ExportDatabase
import org.dhis2.usescases.settings.domain.GetSettingsState
import org.dhis2.usescases.settings.domain.GetSyncErrors
import org.dhis2.usescases.settings.domain.LaunchSync
import org.dhis2.usescases.settings.domain.SettingsMessages
import org.dhis2.usescases.settings.domain.UpdateSmsModule
import org.dhis2.usescases.settings.domain.UpdateSmsResponse
import org.dhis2.usescases.settings.domain.UpdateSyncSettings
import org.dhis2.usescases.settings.models.DeleteDataState
import org.dhis2.usescases.settings.models.ErrorViewModel
import org.dhis2.usescases.settings.models.SettingsState
import org.hisp.dhis.android.core.settings.LimitScope
import java.io.File

class SyncManagerPresenter(
    private val getSettingsState: GetSettingsState,
    private val updateSyncSettings: UpdateSyncSettings,
    private val updateSmsResponse: UpdateSmsResponse,
    private val getSyncErrors: GetSyncErrors,
    private val updateSmsModule: UpdateSmsModule,
    private val deleteLocalData: DeleteLocalData,
    private val exportDatabase: ExportDatabase,
    private val checkVersionUpdate: CheckVersionUpdate,
    private val launchSync: LaunchSync,
    private val dispatcherProvider: DispatcherProvider,
    private val networkUtils: NetworkUtils,
    private val settingsMessages: SettingsMessages,
) : ViewModel() {
    val exporting = exportDatabase.exporting

    private val connectionStatus = networkUtils.connectionStatus

    private val _settingsState = MutableStateFlow<SettingsState?>(null)
    val settingsState =
        _settingsState
            .onStart {
                loadData()
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(),
                null,
            )

    val messageChannel = settingsMessages.messageChannel

    private val _errorLogChannel = Channel<List<ErrorViewModel>>(Channel.RENDEZVOUS)
    val errorLogChannel = _errorLogChannel.receiveAsFlow()

    private val _fileToShareChannel = Channel<File>()
    val fileToShareChannel = _fileToShareChannel.receiveAsFlow()

    private val syncWorkInfo =
        launchSync.syncWorkInfo.stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue =
                LaunchSync.SyncStatusProgress(
                    metadataSyncProgress = LaunchSync.SyncStatus.None,
                    dataSyncProgress = LaunchSync.SyncStatus.None,
                ),
        )

    init {
        connectionStatus
            .onEach { hasConnection ->
                _settingsState.update { it?.copy(hasConnection = hasConnection) }
            }.launchIn(viewModelScope)

        syncWorkInfo
            .onEach { syncStatusProgress ->

                val shouldLoadData =
                    syncStatusProgress.hasSyncFinished(
                        _settingsState.value?.metadataSettingsViewModel?.syncInProgress == true,
                        _settingsState.value?.dataSettingsViewModel?.syncInProgress == true,
                    )

                _settingsState.update {
                    it?.copy(
                        metadataSettingsViewModel =
                            it.metadataSettingsViewModel.copy(
                                syncInProgress = syncStatusProgress.metadataSyncProgress is LaunchSync.SyncStatus.InProgress,
                            ),
                        dataSettingsViewModel =
                            it.dataSettingsViewModel.copy(
                                syncInProgress = syncStatusProgress.dataSyncProgress is LaunchSync.SyncStatus.InProgress,
                            ),
                    )
                }

                if (shouldLoadData) {
                    loadData()
                }
            }.launchIn(viewModelScope)
    }

    private suspend fun loadData() {
        val settingsState =
            getSettingsState(
                openedItem = _settingsState.value?.openedItem,
                hasConnection = _settingsState.value?.hasConnection == true,
                metadataSyncInProgress = syncWorkInfo.value.metadataSyncProgress == LaunchSync.SyncStatus.InProgress,
                dataSyncInProgress = syncWorkInfo.value.dataSyncProgress == LaunchSync.SyncStatus.InProgress,
            )
        _settingsState.update { settingsState }
    }

    fun onItemClick(settingsItem: SettingItem) {
        viewModelScope.launch(dispatcherProvider.io()) {
            _settingsState.update {
                it?.copy(
                    openedItem = if (settingsItem == it.openedItem) null else settingsItem,
                )
            }
        }
    }

    fun onCheckVersionUpdate() {
        viewModelScope.launch(dispatcherProvider.io()) {
            checkVersionUpdate()
        }
    }

    fun onDeleteLocalData() {
        viewModelScope.launch {
            _settingsState.update {
                it?.copy(
                    deleteDataState = DeleteDataState.Opened,
                )
            }
        }
    }

    fun onDismissLocalData() {
        viewModelScope.launch {
            _settingsState.update {
                it?.copy(
                    deleteDataState = DeleteDataState.None,
                )
            }
        }
    }

    fun saveLimitScope(limitScope: LimitScope?) {
        viewModelScope.launch(dispatcherProvider.io()) {
            updateSyncSettings(UpdateSyncSettings.SyncSettings.Scope(limitScope))
            loadData()
        }
    }

    fun saveEventMaxCount(eventsNumber: Int?) {
        viewModelScope.launch(dispatcherProvider.io()) {
            updateSyncSettings(UpdateSyncSettings.SyncSettings.EventMaxCount(eventsNumber))
            loadData()
        }
    }

    fun saveTeiMaxCount(teiNumber: Int?) {
        viewModelScope.launch(dispatcherProvider.io()) {
            updateSyncSettings(UpdateSyncSettings.SyncSettings.TeiMaxCount(teiNumber))
            loadData()
        }
    }

    fun saveReservedValues(reservedValuesCount: Int?) {
        viewModelScope.launch(dispatcherProvider.io()) {
            updateSyncSettings(UpdateSyncSettings.SyncSettings.ReservedValues(reservedValuesCount))
            loadData()
        }
    }

    fun saveWaitForSmsResponse(
        shouldWait: Boolean,
        resultSender: String,
    ) {
        viewModelScope.launch(dispatcherProvider.io()) {
            val setting =
                if (shouldWait) {
                    UpdateSmsResponse.ResponseSetting.Enable(resultSender)
                } else {
                    UpdateSmsResponse.ResponseSetting.Disable
                }

            when (val result = updateSmsResponse(setting)) {
                UpdateSmsResponse.UpdateSmsResponseResult.Success ->
                    loadData()

                is UpdateSmsResponse.UpdateSmsResponseResult.ValidationError ->
                    _settingsState.update {
                        it?.copy(
                            smsSettingsViewModel =
                                it.smsSettingsViewModel.copy(
                                    resultSenderValidationResult = result.validationResult,
                                ),
                        )
                    }
            }
        }
    }

    fun saveGatewayNumber(gatewayNumber: String) {
        viewModelScope.launch(dispatcherProvider.io()) {
            val result =
                updateSmsModule(UpdateSmsModule.SmsSetting.SaveGatewayNumber(gatewayNumber))
            when (result) {
                UpdateSmsModule.EnableSmsResult.Success,
                UpdateSmsModule.EnableSmsResult.Error,
                ->
                    loadData()

                is UpdateSmsModule.EnableSmsResult.ValidationError ->
                    _settingsState.update {
                        it?.copy(
                            smsSettingsViewModel =
                                it.smsSettingsViewModel.copy(
                                    gatewayValidationResult = result.validationResult,
                                ),
                        )
                    }
            }
        }
    }

    fun saveResultSender(resultSender: String) {
        viewModelScope.launch(dispatcherProvider.io()) {
            val result = updateSmsModule(UpdateSmsModule.SmsSetting.SaveResultNumber(resultSender))
            when (result) {
                UpdateSmsModule.EnableSmsResult.Success,
                UpdateSmsModule.EnableSmsResult.Error,
                ->
                    loadData()

                is UpdateSmsModule.EnableSmsResult.ValidationError ->
                    _settingsState.update {
                        it?.copy(
                            smsSettingsViewModel =
                                it.smsSettingsViewModel.copy(
                                    resultSenderValidationResult = result.validationResult,
                                ),
                        )
                    }
            }
        }
    }

    fun saveTimeout(timeout: Int) {
        viewModelScope.launch(dispatcherProvider.io()) {
            val result = updateSmsModule(UpdateSmsModule.SmsSetting.SaveTimeout(timeout))
        }
    }

    fun enableSmsModule(
        enableSms: Boolean,
        smsGateway: String,
        timeout: Int,
    ) {
        viewModelScope.launch(dispatcherProvider.io()) {
            val result =
                updateSmsModule(
                    if (enableSms) {
                        UpdateSmsModule.SmsSetting.Enable(smsGateway, timeout)
                    } else {
                        UpdateSmsModule.SmsSetting.Disable
                    },
                )
            when (result) {
                UpdateSmsModule.EnableSmsResult.Success,
                UpdateSmsModule.EnableSmsResult.Error,
                ->
                    loadData()

                is UpdateSmsModule.EnableSmsResult.ValidationError ->
                    _settingsState.update {
                        it?.copy(
                            smsSettingsViewModel =
                                it.smsSettingsViewModel.copy(
                                    gatewayValidationResult = result.validationResult,
                                ),
                        )
                    }
            }
        }
    }

    fun syncData() {
        viewModelScope.launch(dispatcherProvider.io()) {
            launchSync(LaunchSync.SyncAction.SyncData)
        }
    }

    fun syncMeta() {
        viewModelScope.launch(dispatcherProvider.io()) {
            launchSync(LaunchSync.SyncAction.SyncMetadata)
        }
    }

    fun resetSyncParameters() {
        viewModelScope.launch(dispatcherProvider.io()) {
            updateSyncSettings(UpdateSyncSettings.SyncSettings.Reset)
            loadData()
        }
    }

    fun deleteLocalData() {
        viewModelScope.launch(dispatcherProvider.io()) {
            _settingsState.update {
                it?.copy(
                    deleteDataState = DeleteDataState.Deleting,
                )
            }
            deleteLocalData.invoke()
            loadData()
        }
    }

    fun checkSyncErrors() {
        onItemClick(SettingItem.ERROR_LOG)
        viewModelScope.launch(dispatcherProvider.io()) {
            _errorLogChannel.send(getSyncErrors())
        }
    }

    fun onExportAndShareDB() {
        exportDB(ExportDatabase.ExportType.Share)
    }

    fun onExportAndDownloadDB() {
        exportDB(ExportDatabase.ExportType.Download)
    }

    private fun exportDB(exportType: ExportDatabase.ExportType) {
        viewModelScope.launch(context = dispatcherProvider.io()) {
            when (val result = exportDatabase(exportType)) {
                is ExportDatabase.ExportResult.Share -> _fileToShareChannel.send(result.db)
                else -> {
                    // do nothing
                }
            }
        }
    }

    fun onSyncDataPeriodChanged(period: Int) {
        viewModelScope.launch(dispatcherProvider.io()) {
            launchSync(LaunchSync.SyncAction.UpdateSyncDataPeriod(period))
            if (period == Constants.TIME_MANUAL) {
                loadData()
            }
        }
    }

    fun onSyncMetaPeriodChanged(period: Int) {
        viewModelScope.launch(dispatcherProvider.io()) {
            launchSync(LaunchSync.SyncAction.UpdateSyncMetadataPeriod(period))
            if (period == Constants.TIME_MANUAL) {
                loadData()
            }
        }
    }

    fun closeChannel() {
        settingsMessages.close()
        _errorLogChannel.close()
    }
}
