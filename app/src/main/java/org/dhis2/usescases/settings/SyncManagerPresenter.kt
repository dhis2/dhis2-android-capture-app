package org.dhis2.usescases.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dhis2.R
import org.dhis2.commons.Constants
import org.dhis2.commons.matomo.Actions
import org.dhis2.commons.matomo.Categories
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.service.VersionRepository
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.data.service.workManager.WorkerItem
import org.dhis2.data.service.workManager.WorkerType
import org.dhis2.mobile.commons.files.FileHandler
import org.dhis2.usescases.settings.domain.GetSettingsState
import org.dhis2.usescases.settings.domain.UpdateSmsResponse
import org.dhis2.usescases.settings.domain.UpdateSyncSettings
import org.dhis2.usescases.settings.models.ErrorModelMapper
import org.dhis2.usescases.settings.models.ErrorViewModel
import org.dhis2.usescases.settings.models.SettingsState
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.CONFIRM_DELETE_LOCAL_DATA
import org.dhis2.utils.analytics.SYNC_DATA_NOW
import org.dhis2.utils.analytics.SYNC_METADATA_NOW
import org.hisp.dhis.android.core.settings.LimitScope
import timber.log.Timber
import java.io.File

class SyncManagerPresenter(
    private val getSettingsState: GetSettingsState,
    private val updateSyncSettings: UpdateSyncSettings,
    private val updateSmsResponse: UpdateSmsResponse,
    private val gatewayValidator: GatewayValidator,
    private val preferenceProvider: PreferenceProvider,
    private val workManagerController: WorkManagerController,
    private val settingsRepository: SettingsRepository,
    private val analyticsHelper: AnalyticsHelper,
    private val errorMapper: ErrorModelMapper,
    private val resourceManager: ResourceManager,
    private val versionRepository: VersionRepository,
    private val dispatcherProvider: DispatcherProvider,
    private val networkUtils: NetworkUtils,
    private val fileHandler: FileHandler,
) : ViewModel() {
    private val _updatesLoading = MutableLiveData<Boolean>()
    private val _exporting = MutableLiveData(false)
    val exporting: LiveData<Boolean> = _exporting

    private val connectionStatus = networkUtils.connectionStatus

    private val _settingsState = MutableStateFlow<SettingsState?>(null)
    val settingsState = _settingsState
        .onStart {
            loadData()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            null,
        )

    private val _messageChannel = Channel<String>(Channel.BUFFERED)
    val messageChannel = _messageChannel.receiveAsFlow()

    private val _errorLogChannel = Channel<List<ErrorViewModel>>(Channel.RENDEZVOUS)
    val errorLogChannel = _errorLogChannel.receiveAsFlow()

    private val _fileToShareChannel = Channel<File>()
    val fileToShareChannel = _fileToShareChannel.receiveAsFlow()

    enum class SyncStatusProgress {
        NONE,
        SYNC_DATA_IN_PROGRESS,
        SYNC_DATA_FINISHED,
        SYNC_METADATA_IN_PROGRESS,
        SYNC_METADATA_FINISHED,
    }

    private val _metadataWorkInfo =
        workManagerController.getWorkInfosByTagLiveData(Constants.META_NOW)
            .asFlow()
            .map { workStatuses ->
                var workState: WorkInfo.State? = workStatuses.getOrNull(0)?.state
                onWorkStatusesUpdate(workState, Constants.META_NOW)
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                SyncStatusProgress.NONE,
            )

    private val _dataWorkInfo =
        workManagerController.getWorkInfosByTagLiveData(Constants.DATA_NOW)
            .asFlow()
            .map { workStatuses ->
                var workState: WorkInfo.State? = workStatuses.getOrNull(0)?.state
                onWorkStatusesUpdate(workState, Constants.DATA_NOW)
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                SyncStatusProgress.NONE,
            )

    private val _startWorkInfo = MutableStateFlow(SyncStatusProgress.NONE)

    private val syncWorkInfo = merge(_metadataWorkInfo, _dataWorkInfo, _startWorkInfo)

    init {
        connectionStatus
            .onEach { hasConnection ->
                _settingsState.update { it?.copy(hasConnection = hasConnection) }
            }
            .launchIn(viewModelScope)

        syncWorkInfo
            .onEach { syncStatusProgress ->
                when (syncStatusProgress) {
                    SyncStatusProgress.NONE -> {}
                    SyncStatusProgress.SYNC_DATA_IN_PROGRESS ->
                        _settingsState.update {
                            it?.copy(
                                dataSettingsViewModel = it.dataSettingsViewModel.copy(
                                    syncInProgress = true,
                                ),
                            )
                        }

                    SyncStatusProgress.SYNC_METADATA_IN_PROGRESS ->
                        _settingsState.update {
                            it?.copy(
                                metadataSettingsViewModel = it.metadataSettingsViewModel.copy(
                                    syncInProgress = true,
                                ),
                            )
                        }

                    SyncStatusProgress.SYNC_DATA_FINISHED,
                    SyncStatusProgress.SYNC_METADATA_FINISHED,
                    ->
                        loadData()
                }
            }
            .launchIn(viewModelScope)
    }

    private suspend fun loadData() {
        val settingsState = getSettingsState(
            openedItem = _settingsState.value?.openedItem,
            hasConnection = connectionStatus.value,
            metadataSyncInProgress = _metadataWorkInfo.value == SyncStatusProgress.SYNC_METADATA_IN_PROGRESS,
            dataSyncInProgress = _dataWorkInfo.value == SyncStatusProgress.SYNC_DATA_IN_PROGRESS,
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

    fun init() {
        networkUtils.registerNetworkCallback()
    }

    fun checkVersionUpdate() {
        viewModelScope.launch(dispatcherProvider.io()) {
            _updatesLoading.postValue(true)
            val newVersion = versionRepository.getLatestVersionInfo()
            if (newVersion != null) {
                versionRepository.checkVersionUpdates()
            } else {
                _messageChannel.send(resourceManager.getString(R.string.no_updates))
            }
            _updatesLoading.postValue(false)
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

    fun saveWaitForSmsResponse(shouldWait: Boolean, resultSender: String) {
        viewModelScope.launch(dispatcherProvider.io()) {
            val setting = if (shouldWait) {
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
                            smsSettingsViewModel = it.smsSettingsViewModel.copy(
                                resultSenderValidationResult = result.validationResult,
                            ),
                        )
                    }
            }
        }
    }

    fun enableSmsModule(enableSms: Boolean, smsGateway: String, timeout: Int) {
        viewModelScope.launch(dispatcherProvider.io()) {
            if (enableSms) {
                when (val validation = gatewayValidator(smsGateway)) {
                    GatewayValidator.GatewayValidationResult.Empty,
                    GatewayValidator.GatewayValidationResult.Invalid,
                    ->
                        _settingsState.update {
                            it?.copy(
                                smsSettingsViewModel = it.smsSettingsViewModel.copy(
                                    gatewayValidationResult = validation,
                                ),
                            )
                        }

                    GatewayValidator.GatewayValidationResult.Valid -> {
                        _messageChannel.send(resourceManager.getString(R.string.sms_downloading_data))
                        settingsRepository.saveGatewayNumber(smsGateway)
                        settingsRepository.saveSmsResponseTimeout(timeout)
                        updateSmsModule(true)
                    }
                }
            } else {
                updateSmsModule(false)
            }
        }
    }

    private suspend fun updateSmsModule(enableSms: Boolean) {
        try {
            settingsRepository.enableSmsModule(enableSms)
            _messageChannel.send(
                if (enableSms) {
                    resourceManager.getString(R.string.sms_enabled)
                } else {
                    resourceManager.getString(R.string.sms_disabled)
                },
            )
        } catch (e: Exception) {
            Timber.e(e)
            _messageChannel.send(resourceManager.getString(R.string.sms_disabled))
        } finally {
            loadData()
        }
    }

    private fun onWorkStatusesUpdate(
        workState: WorkInfo.State?,
        workerTag: String,
    ): SyncStatusProgress {
        return if (workState != null) {
            when (workState) {
                WorkInfo.State.ENQUEUED,
                WorkInfo.State.RUNNING,
                WorkInfo.State.BLOCKED,
                -> when (workerTag) {
                    Constants.META_NOW -> SyncStatusProgress.SYNC_METADATA_IN_PROGRESS
                    Constants.DATA_NOW -> SyncStatusProgress.SYNC_DATA_IN_PROGRESS
                    else -> SyncStatusProgress.NONE
                }

                else -> when (workerTag) {
                    Constants.META_NOW -> SyncStatusProgress.SYNC_METADATA_FINISHED
                    Constants.DATA_NOW -> SyncStatusProgress.SYNC_DATA_FINISHED
                    else -> SyncStatusProgress.NONE
                }
            }
        } else {
            when (workerTag) {
                Constants.META_NOW -> SyncStatusProgress.SYNC_METADATA_FINISHED
                Constants.DATA_NOW -> SyncStatusProgress.SYNC_DATA_FINISHED
                else -> SyncStatusProgress.NONE
            }
        }
    }

    fun syncData() {
        viewModelScope.launch(dispatcherProvider.io()) {
            _startWorkInfo.value = SyncStatusProgress.SYNC_DATA_IN_PROGRESS
            analyticsHelper.trackMatomoEvent(Categories.SETTINGS, Actions.SYNC_CONFIG, CLICK)
            analyticsHelper.setEvent(SYNC_DATA_NOW, CLICK, SYNC_DATA_NOW)
            val workerItem = WorkerItem(
                Constants.DATA_NOW,
                WorkerType.DATA,
                null,
                null,
                ExistingWorkPolicy.KEEP,
                null,
            )
            workManagerController.syncDataForWorker(workerItem)
            loadData()
        }
    }

    fun syncMeta() {
        viewModelScope.launch(dispatcherProvider.io()) {
            _startWorkInfo.value = SyncStatusProgress.SYNC_DATA_IN_PROGRESS
            analyticsHelper.setEvent(SYNC_METADATA_NOW, CLICK, SYNC_METADATA_NOW)
            val workerItem = WorkerItem(
                Constants.META_NOW,
                WorkerType.METADATA,
                null,
                null,
                ExistingWorkPolicy.KEEP,
                null,
            )
            workManagerController.syncDataForWorker(workerItem)
        }
    }

    private fun cancelPendingWork(tag: String) {
        viewModelScope.launch(dispatcherProvider.io()) {
            preferenceProvider.setValue(
                when (tag) {
                    Constants.DATA -> Constants.TIME_DATA
                    else -> Constants.TIME_META
                },
                0,
            )
            workManagerController.cancelUniqueWork(tag)
            loadData()
        }
    }

    fun dispose() {
        networkUtils.unregisterNetworkCallback()
        _messageChannel.close()
        _errorLogChannel.close()
    }

    fun resetSyncParameters() {
        viewModelScope.launch(dispatcherProvider.io()) {
            preferenceProvider.setValue(Constants.EVENT_MAX, Constants.EVENT_MAX_DEFAULT)
            preferenceProvider.setValue(Constants.TEI_MAX, Constants.TEI_MAX_DEFAULT)
            preferenceProvider.setValue(Constants.LIMIT_BY_ORG_UNIT, false)
            preferenceProvider.setValue(Constants.LIMIT_BY_PROGRAM, false)
            loadData()
        }
    }

    fun deleteLocalData() {
        viewModelScope.launch(dispatcherProvider.io()) {
            analyticsHelper.setEvent(
                CONFIRM_DELETE_LOCAL_DATA,
                CLICK,
                CONFIRM_DELETE_LOCAL_DATA,
            )
            var error = false
            try {
                settingsRepository.deleteLocalData()
            } catch (e: Exception) {
                Timber.e(e)
                error = true
            }
            if (error) {
                _messageChannel.send(
                    resourceManager.getString(
                        R.string.delete_local_data_error,
                    ),
                )
            } else {
                _messageChannel.send(
                    resourceManager.getString(
                        R.string.delete_local_data_done,
                    ),
                )
            }

            loadData()
        }
    }

    fun checkSyncErrors() {
        viewModelScope.launch(dispatcherProvider.io()) {
            val errors: MutableList<ErrorViewModel> = ArrayList()
            errors.addAll(
                errorMapper.mapD2Error(settingsRepository.d2Errors()),
            )
            errors.addAll(
                errorMapper.mapConflict(settingsRepository.trackerImportConflicts()),
            )
            errors.addAll(
                errorMapper.mapFKViolation(settingsRepository.foreignKeyViolations()),
            )
            _errorLogChannel.send(errors.sortedBy { it.creationDate })
        }
    }

    fun onExportAndShareDB() {
        exportDB(download = true)
    }

    fun onExportAndDownloadDB() {
        exportDB(download = false)
    }

    private fun exportDB(download: Boolean) {
        _exporting.value = true
        viewModelScope.launch(context = dispatcherProvider.ui()) {
            try {
                val db = settingsRepository.exportDatabase()
                fileHandler.copyAndOpen(db) {}
                if (download) {
                    _messageChannel.send(resourceManager.getString(R.string.database_export_downloaded))
                } else {
                    _fileToShareChannel.send(db)
                }
            } catch (e: Exception) {
                _messageChannel.send(resourceManager.parseD2Error(e) ?: "")
            } finally {
                _exporting.postValue(false)
            }
        }
    }

    fun onSyncDataPeriodChanged(period: Int) {
        if (period != Constants.TIME_MANUAL) {
            syncData(period)
        } else {
            cancelPendingWork(Constants.DATA)
        }
    }

    private fun syncData(seconds: Int) {
        preferenceProvider.setValue(Constants.TIME_DATA, seconds)
        workManagerController.cancelUniqueWork(Constants.DATA)
        val workerItem = WorkerItem(
            Constants.DATA,
            WorkerType.DATA,
            seconds.toLong(),
            null,
            null,
            ExistingPeriodicWorkPolicy.REPLACE,
        )
        workManagerController.enqueuePeriodicWork(workerItem)
    }

    fun onSyncMetaPeriodChanged(period: Int) {
        if (period != Constants.TIME_MANUAL) {
            syncMeta(period)
        } else {
            cancelPendingWork(Constants.META)
        }
    }

    private fun syncMeta(seconds: Int) {
        analyticsHelper.trackMatomoEvent(Categories.SETTINGS, Actions.SYNC_DATA, CLICK)
        preferenceProvider.setValue(Constants.TIME_META, seconds)
        workManagerController.cancelUniqueWork(Constants.META)
        val workerItem = WorkerItem(
            Constants.META,
            WorkerType.METADATA,
            seconds.toLong(),
            null,
            null,
            ExistingPeriodicWorkPolicy.REPLACE,
        )
        workManagerController.enqueuePeriodicWork(workerItem)
    }
}
