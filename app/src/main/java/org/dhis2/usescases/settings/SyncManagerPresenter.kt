package org.dhis2.usescases.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import com.google.common.annotations.VisibleForTesting
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.dhis2.R
import org.dhis2.commons.Constants
import org.dhis2.commons.matomo.Actions
import org.dhis2.commons.matomo.Categories
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.server.UserManager
import org.dhis2.data.service.VersionRepository
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.data.service.workManager.WorkerItem
import org.dhis2.data.service.workManager.WorkerType
import org.dhis2.ui.model.ButtonUiModel
import org.dhis2.usescases.reservedValue.ReservedValueActivity
import org.dhis2.usescases.settings.GatewayValidator.Companion.max_size
import org.dhis2.usescases.settings.models.DataSettingsViewModel
import org.dhis2.usescases.settings.models.ErrorModelMapper
import org.dhis2.usescases.settings.models.ErrorViewModel
import org.dhis2.usescases.settings.models.MetadataSettingsViewModel
import org.dhis2.usescases.settings.models.ReservedValueSettingsViewModel
import org.dhis2.usescases.settings.models.SMSSettingsViewModel
import org.dhis2.usescases.settings.models.SettingsViewModel
import org.dhis2.usescases.settings.models.SyncParametersViewModel
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.SYNC_DATA_NOW
import org.dhis2.utils.analytics.SYNC_METADATA_NOW
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.settings.LimitScope
import timber.log.Timber
import java.util.Locale
import kotlin.collections.ArrayList
import kotlin.coroutines.CoroutineContext

class SyncManagerPresenter internal constructor(
    private val d2: D2,
    private val schedulerProvider: SchedulerProvider,
    private val gatewayValidator: GatewayValidator,
    private val preferenceProvider: PreferenceProvider,
    private val workManagerController: WorkManagerController,
    private val settingsRepository: SettingsRepository,
    private val userManager: UserManager,
    private val view: SyncManagerContracts.View,
    private val analyticsHelper: AnalyticsHelper,
    private val errorMapper: ErrorModelMapper,
    private val matomoAnalyticsController: MatomoAnalyticsController,
    private val resourceManager: ResourceManager,
    private val versionRepository: VersionRepository,
    private val dispatcherProvider: DispatcherProvider,
) : CoroutineScope {

    private var job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + dispatcherProvider.io()

    private val compositeDisposable: CompositeDisposable
    private val checkData: FlowableProcessor<Boolean>
    private var smsSettingsViewModel: SMSSettingsViewModel? = null

    private val _syncDataButton = MutableLiveData<ButtonUiModel>()
    val syncDataButton: LiveData<ButtonUiModel> = _syncDataButton
    private val _syncMetaDataButton = MutableLiveData<ButtonUiModel>()
    val syncMetaDataButton: LiveData<ButtonUiModel> = _syncMetaDataButton
    private val _checkVersionsButton = MutableLiveData<ButtonUiModel?>()
    val checkVersionsButton: LiveData<ButtonUiModel?> = _checkVersionsButton
    private val _updatesLoading = MutableLiveData<Boolean>()
    val updatesLoading: LiveData<Boolean> = _updatesLoading
    val versionToUpdate: LiveData<String?> =
        versionRepository.newAppVersion.asLiveData(coroutineContext)

    init {
        checkData = PublishProcessor.create()
        compositeDisposable = CompositeDisposable()
    }

    fun onItemClick(settingsItem: SettingItem?) {
        view.openItem(settingsItem)
    }

    fun init() {
        compositeDisposable.add(
            checkData.startWith(true)
                .flatMapSingle {
                    Single.zip(
                        settingsRepository.metaSync(userManager),
                        settingsRepository.dataSync(),
                        settingsRepository.syncParameters(),
                        settingsRepository.reservedValues(),
                        settingsRepository.sms(),
                    ) { metadataSettingsViewModel: MetadataSettingsViewModel?,
                        dataSettingsViewModel: DataSettingsViewModel?,
                        syncParametersViewModel: SyncParametersViewModel?,
                        reservedValueSettingsViewModel: ReservedValueSettingsViewModel?,
                        smsSettingsViewModel: SMSSettingsViewModel?,
                        ->
                        SettingsViewModel(
                            metadataSettingsViewModel!!,
                            dataSettingsViewModel!!,
                            syncParametersViewModel!!,
                            reservedValueSettingsViewModel!!,
                            smsSettingsViewModel!!,
                        )
                    }
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { (
                        metadataSettingsViewModel,
                        dataSettingsViewModel,
                        syncParametersViewModel,
                        reservedValueSettingsViewModel,
                        smsSettingsViewModel1,
                    ): SettingsViewModel,
                        ->
                        view.setMetadataSettings(
                            metadataSettingsViewModel,
                        )
                        view.setDataSettings(dataSettingsViewModel)
                        view.setParameterSettings(syncParametersViewModel)
                        view.setReservedValuesSettings(reservedValueSettingsViewModel)
                        view.setSMSSettings(smsSettingsViewModel1)
                        smsSettingsViewModel = smsSettingsViewModel1
                    },
                ) { t: Throwable? -> Timber.e(t) },
        )

        _syncDataButton.postValue(
            ButtonUiModel(
                resourceManager.getString(R.string.SYNC_DATA).uppercase(Locale.getDefault()),
                true,
            ) {
                syncData()
            },
        )
        _syncMetaDataButton.postValue(
            ButtonUiModel(
                resourceManager.getString(R.string.SYNC_META).uppercase(Locale.getDefault()),
                true,
            ) { syncMeta() },
        )
        _checkVersionsButton.postValue(
            ButtonUiModel(
                resourceManager.getString(R.string.check_for_updates),
                true,
            ) {
                _updatesLoading.value = true
                launch {
                    versionRepository.downloadLatestVersionInfo()
                }
            },
        )
    }

    val metadataPeriodSetting: Int
        get() = settingsRepository.metaSync(userManager)
            .blockingGet()
            .metadataSyncPeriod
    val dataPeriodSetting: Int
        get() = settingsRepository.dataSync()
            .blockingGet()
            .dataSyncPeriod

    fun validateGatewayObservable(gateway: String) {
        if (plusIsMissingOrIsTooLong(gateway)) {
            view.showInvalidGatewayError()
        } else if (gateway.isEmpty()) {
            view.requestNoEmptySMSGateway()
        } else if (isValidGateway(gateway)) {
            view.hideGatewayError()
        }
    }

    private fun isValidGateway(gateway: String): Boolean =
        (gatewayValidator.validate(gateway) || gateway.startsWith("+")) && gateway.length == 1

    private fun plusIsMissingOrIsTooLong(gateway: String): Boolean {
        return !gateway.startsWith("+") && gateway.length == 1 || gateway.length >= max_size
    }

    fun isGatewaySetAndValid(gateway: String): Boolean {
        if (gateway.isEmpty()) {
            view.requestNoEmptySMSGateway()
            return false
        } else if (!gatewayValidator.validate(gateway)) {
            view.showInvalidGatewayError()
            return false
        }
        view.hideGatewayError()
        return true
    }

    fun saveLimitScope(limitScope: LimitScope?) {
        val syncParam = "sync_limitScope_save"
        matomoAnalyticsController.trackEvent(Categories.SETTINGS, syncParam, CLICK)
        settingsRepository.saveLimitScope(limitScope!!)
        checkData.onNext(true)
    }

    fun saveEventMaxCount(eventsNumber: Int?) {
        val syncParam = "sync_eventMaxCount_save"
        matomoAnalyticsController.trackEvent(Categories.SETTINGS, syncParam, CLICK)
        settingsRepository.saveEventsToDownload(eventsNumber!!)
        checkData.onNext(true)
    }

    fun saveTeiMaxCount(teiNumber: Int?) {
        val syncParam = "sync_teiMaxCoung_save"
        matomoAnalyticsController.trackEvent(Categories.SETTINGS, syncParam, CLICK)
        settingsRepository.saveTeiToDownload(teiNumber!!)
        checkData.onNext(true)
    }

    fun saveReservedValues(reservedValuesCount: Int?) {
        val syncParam = "sync_reservedValues_save"
        matomoAnalyticsController.trackEvent(Categories.SETTINGS, syncParam, CLICK)
        settingsRepository.saveReservedValuesToDownload(reservedValuesCount!!)
        checkData.onNext(true)
    }

    fun saveGatewayNumber(gatewayNumber: String) {
        if (isGatewaySetAndValid(gatewayNumber)) {
            settingsRepository.saveGatewayNumber(gatewayNumber)
        }
    }

    fun saveSmsResultSender(smsResultSender: String?) {
        settingsRepository.saveSmsResultSender(smsResultSender!!)
    }

    fun saveSmsResponseTimeout(smsResponseTimeout: Int?) {
        settingsRepository.saveSmsResponseTimeout(smsResponseTimeout!!)
    }

    fun saveWaitForSmsResponse(shouldWait: Boolean) {
        settingsRepository.saveWaitForSmsResponse(shouldWait)
    }

    fun enableSmsModule(enableSms: Boolean) {
        if (enableSms) {
            view.displaySMSRefreshingData()
        }
        compositeDisposable.add(
            settingsRepository.enableSmsModule(enableSms)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.displaySMSEnabled(enableSms) },
                ) { error: Throwable? ->
                    Timber.e(error)
                    view.displaySmsEnableError()
                },
        )
    }

    fun onWorkStatusesUpdate(workState: WorkInfo.State?, workerTag: String) {
        if (workState != null) {
            when (workState) {
                WorkInfo.State.ENQUEUED,
                WorkInfo.State.RUNNING,
                WorkInfo.State.BLOCKED,
                -> when (workerTag) {
                    Constants.META_NOW -> view.onMetadataSyncInProgress()
                    Constants.DATA_NOW -> view.onDataSyncInProgress()
                }
                else -> when (workerTag) {
                    Constants.META_NOW -> view.onMetadataFinished()
                    Constants.DATA_NOW -> view.onDataFinished()
                }
            }
        } else {
            when (workerTag) {
                Constants.META_NOW -> view.onMetadataFinished()
                Constants.DATA_NOW -> view.onDataFinished()
            }
        }
    }

    fun syncData(seconds: Int, scheduleTag: String) {
        preferenceProvider.setValue(Constants.TIME_DATA, seconds)
        workManagerController.cancelUniqueWork(scheduleTag)
        val workerItem = WorkerItem(
            scheduleTag,
            WorkerType.DATA,
            seconds.toLong(),
            null,
            null,
            ExistingPeriodicWorkPolicy.REPLACE,
        )
        workManagerController.enqueuePeriodicWork(workerItem)
        checkData()
    }

    fun syncMeta(seconds: Int, scheduleTag: String) {
        matomoAnalyticsController.trackEvent(Categories.SETTINGS, Actions.SYNC_DATA, CLICK)
        preferenceProvider.setValue(Constants.TIME_META, seconds)
        workManagerController.cancelUniqueWork(scheduleTag)
        val workerItem = WorkerItem(
            scheduleTag,
            WorkerType.METADATA,
            seconds.toLong(),
            null,
            null,
            ExistingPeriodicWorkPolicy.REPLACE,
        )
        workManagerController.enqueuePeriodicWork(workerItem)
        checkData()
    }

    fun syncData() {
        matomoAnalyticsController.trackEvent(Categories.SETTINGS, Actions.SYNC_CONFIG, CLICK)
        view.syncData()
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
        checkData()
    }

    fun syncMeta() {
        view.syncMeta()
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

    fun cancelPendingWork(tag: String) {
        preferenceProvider.setValue(
            when (tag) {
                Constants.DATA -> Constants.TIME_DATA
                else -> Constants.TIME_META
            },
            0,
        )
        workManagerController.cancelUniqueWork(tag)
        checkData()
    }

    fun dispose() {
        compositeDisposable.clear()
    }

    fun resetSyncParameters() {
        preferenceProvider.setValue(Constants.EVENT_MAX, Constants.EVENT_MAX_DEFAULT)
        preferenceProvider.setValue(Constants.TEI_MAX, Constants.TEI_MAX_DEFAULT)
        preferenceProvider.setValue(Constants.LIMIT_BY_ORG_UNIT, false)
        preferenceProvider.setValue(Constants.LIMIT_BY_PROGRAM, false)
        checkData.onNext(true)
    }

    fun onDeleteLocalData() {
        view.deleteLocalData()
    }

    fun deleteLocalData() {
        var error = false
        try {
            d2.wipeModule().wipeData()
        } catch (e: D2Error) {
            Timber.e(e)
            error = true
        }
        view.showLocalDataDeleted(error)
    }

    fun onReservedValues() {
        view.startActivity(ReservedValueActivity::class.java, null, false, false, null)
    }

    fun checkSyncErrors() {
        compositeDisposable.add(
            Single.fromCallable<List<ErrorViewModel>> {
                val errors: MutableList<ErrorViewModel> = ArrayList()
                errors.addAll(
                    errorMapper.mapD2Error(d2.maintenanceModule().d2Errors().blockingGet()),
                )
                errors.addAll(
                    errorMapper.mapConflict(
                        d2.importModule().trackerImportConflicts().blockingGet(),
                    ),
                )
                errors.addAll(
                    errorMapper.mapFKViolation(
                        d2.maintenanceModule().foreignKeyViolations().blockingGet(),
                    ),
                )
                errors
            }
                .map { errors: List<ErrorViewModel> ->
                    errors.sortedBy { it.creationDate }
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { data: List<ErrorViewModel> -> view.showSyncErrors(data) },
                ) { t: Throwable? -> Timber.e(t) },
        )
    }

    fun checkData() {
        checkData.onNext(true)
    }

    fun checkGatewayAndTimeoutAreValid() {
        if (view.isGatewayValid && view.isResultTimeoutValid) {
            view.enabledSMSSwitchAndSender(smsSettingsViewModel)
        }
    }

    @VisibleForTesting
    fun setSmsSettingsViewModel(settingsViewModel: SMSSettingsViewModel?) {
        smsSettingsViewModel = settingsViewModel
    }

    fun updateSyncDataButton(canBeClicked: Boolean) {
        _syncDataButton.postValue(
            ButtonUiModel(
                resourceManager.getString(R.string.SYNC_DATA).uppercase(Locale.getDefault()),
                canBeClicked,
            ) {
                syncData()
            },
        )
    }

    fun updateSyncMetaDataButton(canBeClicked: Boolean) {
        _syncMetaDataButton.postValue(
            ButtonUiModel(
                resourceManager.getString(R.string.SYNC_META).uppercase(Locale.getDefault()),
                canBeClicked,
            ) {
                syncMeta()
            },
        )
    }
}
