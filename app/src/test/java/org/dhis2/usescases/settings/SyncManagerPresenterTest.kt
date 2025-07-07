package org.dhis2.usescases.settings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.work.ExistingPeriodicWorkPolicy
import app.cash.turbine.test
import io.reactivex.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.commons.Constants
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.service.VersionRepository
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.data.service.workManager.WorkerItem
import org.dhis2.data.service.workManager.WorkerType
import org.dhis2.mobile.commons.files.FileHandler
import org.dhis2.usescases.settings.models.DataSettingsViewModel
import org.dhis2.usescases.settings.models.ErrorModelMapper
import org.dhis2.usescases.settings.models.MetadataSettingsViewModel
import org.dhis2.usescases.settings.models.ReservedValueSettingsViewModel
import org.dhis2.usescases.settings.models.SMSSettingsViewModel
import org.dhis2.usescases.settings.models.SyncParametersViewModel
import org.dhis2.utils.analytics.AnalyticsHelper
import org.hisp.dhis.android.core.settings.LimitScope
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class SyncManagerPresenterTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var presenter: SyncManagerPresenter
    private val gatewayValidator: GatewayValidator = mock()
    private val preferencesProvider: PreferenceProvider = mock()
    private val workManagerController: WorkManagerController = mock()
    private val settingsRepository: SettingsRepository = mock()
    private val analyticsHelper: AnalyticsHelper = mock()
    private val errorMapper: ErrorModelMapper = mock()
    private val fileHandler: FileHandler = mock()
    private val networkUtils: NetworkUtils = mock()
    private val resourcesManager: ResourceManager = mock()
    private val versionRepository: VersionRepository = mock()
    private val testingDispatcher = UnconfinedTestDispatcher()
    private val dispatcherProvider: DispatcherProvider = mock {
        on { io() } doReturn testingDispatcher
        on { ui() } doReturn testingDispatcher
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testingDispatcher)
        whenever(versionRepository.newAppVersion) doReturn MutableSharedFlow()
        whenever(workManagerController.getWorkInfosByTagLiveData(any()))doReturn MutableLiveData()
        presenter = SyncManagerPresenter(
            gatewayValidator = gatewayValidator,
            preferenceProvider = preferencesProvider,
            workManagerController = workManagerController,
            settingsRepository = settingsRepository,
            analyticsHelper = analyticsHelper,
            errorMapper = errorMapper,
            resourceManager = resourcesManager,
            versionRepository = versionRepository,
            dispatcherProvider = dispatcherProvider,
            networkUtils = networkUtils,
            fileHandler = fileHandler,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Should init settings values`() {
        whenever(resourcesManager.getString(any())) doReturn ""
        whenever(networkUtils.connectionStatus)doReturn MutableStateFlow(true)
        presenter.init()
        whenever(
            settingsRepository.metaSync(),
        ) doReturn Single.just(mockedMetaViewModel())
        whenever(settingsRepository.dataSync()) doReturn Single.just(mockedDataViewModel())
        whenever(settingsRepository.syncParameters()) doReturn Single.just(mockedParamsViewModel())
        whenever(settingsRepository.reservedValues()) doReturn Single.just(
            mockedReservecValuesViewModel(),
        )
        whenever(settingsRepository.sms()) doReturn Single.just(mockedSMSViewModel())

        presenter.init()

       /* verify(view).setMetadataSettings(mockedMetaViewModel())
        verify(view).setDataSettings(mockedDataViewModel())
        verify(view).setParameterSettings(mockedParamsViewModel())
        verify(view).setReservedValuesSettings(mockedReservecValuesViewModel())
        verify(view).setSMSSettings(mockedSMSViewModel())*/
    }

    @Ignore
    @Test
    fun `should call work in progress`() {
        /* presenter.onWorkStatusesUpdate(WorkInfo.State.ENQUEUED, META_NOW)
        presenter.onWorkStatusesUpdate(WorkInfo.State.RUNNING, META_NOW)
        presenter.onWorkStatusesUpdate(WorkInfo.State.BLOCKED, META_NOW)

        presenter.onWorkStatusesUpdate(WorkInfo.State.ENQUEUED, DATA_NOW)
        presenter.onWorkStatusesUpdate(WorkInfo.State.RUNNING, DATA_NOW)
        presenter.onWorkStatusesUpdate(WorkInfo.State.BLOCKED, DATA_NOW)

       verify(view, times(3)).onMetadataSyncInProgress()
        verify(view, times(3)).onDataSyncInProgress()*/
    }

    @Ignore
    @Test
    fun `should call work finished`() {
        /*presenter.onWorkStatusesUpdate(null, META_NOW)
        presenter.onWorkStatusesUpdate(WorkInfo.State.SUCCEEDED, META_NOW)
        presenter.onWorkStatusesUpdate(WorkInfo.State.FAILED, META_NOW)
        presenter.onWorkStatusesUpdate(WorkInfo.State.CANCELLED, META_NOW)

        presenter.onWorkStatusesUpdate(null, DATA_NOW)
        presenter.onWorkStatusesUpdate(WorkInfo.State.SUCCEEDED, DATA_NOW)
        presenter.onWorkStatusesUpdate(WorkInfo.State.FAILED, DATA_NOW)
        presenter.onWorkStatusesUpdate(WorkInfo.State.CANCELLED, DATA_NOW)

        verify(view, times(4)).onMetadataFinished()
        verify(view, times(4)).onDataFinished()*/
    }

    private fun mockedMetaViewModel(): MetadataSettingsViewModel {
        return MetadataSettingsViewModel(
            metadataSyncPeriod = 100,
            lastMetadataSync = "test",
            hasErrors = false,
            canEdit = false,
            syncInProgress = false,
        )
    }

    private fun mockedDataViewModel(): DataSettingsViewModel {
        return DataSettingsViewModel(
            dataSyncPeriod = 100,
            lastDataSync = "test",
            syncHasErrors = false,
            dataHasErrors = false,
            dataHasWarnings = false,
            canEdit = false,
            syncInProgress = false,
        )
    }

    private fun mockedParamsViewModel(): SyncParametersViewModel {
        return SyncParametersViewModel(
            numberOfTeiToDownload = 100,
            numberOfEventsToDownload = 100,
            currentTeiCount = 100,
            currentEventCount = 100,
            limitScope = LimitScope.GLOBAL,
            teiNumberIsEditable = false,
            eventNumberIsEditable = false,
            limitScopeIsEditable = false,
            hasSpecificProgramSettings = 5,
        )
    }

    private fun mockedReservecValuesViewModel(): ReservedValueSettingsViewModel {
        return ReservedValueSettingsViewModel(20, true)
    }

    private fun mockedSMSViewModel(): SMSSettingsViewModel {
        return SMSSettingsViewModel(
            isEnabled = true,
            gatewayNumber = "test",
            responseNumber = "test",
            responseTimeout = 10,
            isGatewayNumberEditable = false,
            isResponseNumberEditable = false,
            waitingForResponse = false,
            gatewayValidationResult = GatewayValidator.GatewayValidationResult.Valid,
            resultSenderValidationResult = GatewayValidator.GatewayValidationResult.Valid,
        )
    }

    @Ignore
    @Test
    fun `Should return metadata period setting`() {
        /*whenever(settingsRepository.metaSync()) doReturn Single.just(
            MetadataSettingsViewModel(
                100,
                "last date",
                hasErrors = false,
                canEdit = true,
                syncInProgress = true,
            ),
        )
        val period = presenter.metadataPeriodSetting
        assert(period == 100)*/
    }

    @Ignore
    @Test
    fun `Should return data period setting`() {
        /*whenever(settingsRepository.dataSync()) doReturn Single.just(
            DataSettingsViewModel(
                100,
                "last date",
                false,
                dataHasErrors = true,
                dataHasWarnings = true,
                canEdit = true,
                syncInProgress = true,
            ),
        )
        val period = presenter.dataPeriodSetting
        assert(period == 100)*/
    }

    @Test
    fun `Should save limit scope`() {
        presenter.saveLimitScope(LimitScope.GLOBAL)
        verify(settingsRepository, times(1)).saveLimitScope(LimitScope.GLOBAL)
    }

    @Test
    fun `Should save event max count`() {
        presenter.saveEventMaxCount(200)
        verify(settingsRepository, times(1)).saveEventsToDownload(200)
    }

    @Test
    fun `Should save tei max count`() {
        presenter.saveTeiMaxCount(100)
        verify(settingsRepository, times(1)).saveTeiToDownload(100)
    }

    @Test
    fun `Should save reserved values to download`() {
        presenter.saveReservedValues(50)
        verify(settingsRepository, times(1)).saveReservedValuesToDownload(50)
    }

    @Test
    fun `Should save gateway and timeout if validation passes`() {
        val gatewayNumberTest = "+11111111111"
        whenever(gatewayValidator(gatewayNumberTest)) doReturn GatewayValidator.GatewayValidationResult.Valid
        presenter.enableSmsModule(true, gatewayNumberTest, 1)
        verify(settingsRepository, times(1)).saveGatewayNumber(gatewayNumberTest)
        verify(settingsRepository, times(1)).saveSmsResponseTimeout(any())
        verify(settingsRepository, times(1)).enableSmsModule(true)
    }

    @Test
    fun `Should not save gateway if validation fails`() {
        val gatewayNumberTest = "+111"
        whenever(gatewayValidator(gatewayNumberTest)) doReturn GatewayValidator.GatewayValidationResult.Invalid
        presenter.enableSmsModule(true, gatewayNumberTest, 0)
        verify(settingsRepository, times(0)).saveGatewayNumber(gatewayNumberTest)
        verify(settingsRepository, times(0)).saveSmsResponseTimeout(any())
        verify(settingsRepository, times(0)).enableSmsModule(true)
    }

    @Test
    fun `Should save sms result sender`() {
        val smsResultSender = "test"
        whenever(gatewayValidator(smsResultSender)) doReturn GatewayValidator.GatewayValidationResult.Valid
        presenter.saveWaitForSmsResponse(true, smsResultSender)
        verify(settingsRepository, times(1)).saveSmsResultSender(smsResultSender)
        verify(settingsRepository, times(1)).saveWaitForSmsResponse(any())
    }

    @Test
    fun `Should sync data`() {
        presenter.onSyncDataPeriodChanged(10)
        verify(workManagerController, times(1)).cancelUniqueWork("tag")
        verify(workManagerController, times(1)).enqueuePeriodicWork(
            WorkerItem(
                "tag",
                WorkerType.DATA,
                10,
                null,
                null,
                ExistingPeriodicWorkPolicy.REPLACE,
            ),
        )
    }

    @Test
    fun `Should sync metadata`() {
        presenter.onSyncMetaPeriodChanged(10)
        verify(workManagerController, times(1)).cancelUniqueWork(Constants.META)
        verify(workManagerController, times(1)).enqueuePeriodicWork(
            WorkerItem(
                Constants.META,
                WorkerType.METADATA,
                10,
                null,
                null,
                ExistingPeriodicWorkPolicy.REPLACE,
            ),
        )
    }

    @Test
    fun `Should sync metadata now`() {
        presenter.syncMeta()
        verify(workManagerController, times(1)).syncDataForWorker(any())
    }

    @Test
    fun `Should sync data now`() {
        presenter.syncData()
        verify(workManagerController, times(1)).syncDataForWorker(any())
    }

    @Test
    fun `Should cancel pending work`() {
        presenter.onSyncDataPeriodChanged(Constants.TIME_MANUAL)
        verify(workManagerController, times(1)).cancelUniqueWork(Constants.DATA)
    }

    @Test
    fun `Should reset parameters to default`() {
        presenter.resetSyncParameters()
        verify(preferencesProvider, times(4)).setValue(any(), any())
    }

    @Test
    fun `Should open clicked item`() = runTest {
        presenter.settingsState.test {
            awaitItem()
            presenter.onItemClick(SettingItem.DATA_SYNC)
            val item = awaitItem()
            assertTrue(item?.openedItem == SettingItem.DATA_SYNC)
        }
    }

    @Test
    fun `Should export database`() = runTest {
        val mockedFile: File = mock()
        whenever(settingsRepository.exportDatabase())doReturn mockedFile
        whenever(resourcesManager.getString(any()))doReturn "Database exported"
        presenter.onExportAndShareDB()
        presenter.messageChannel.test {
            val item = awaitItem()
            assertTrue(item == "Database exported")
        }
    }

    @Test
    fun `Should display export database error`() = runTest {
        whenever(settingsRepository.exportDatabase())doThrow RuntimeException("Testing exception")
        whenever(resourcesManager.parseD2Error(any()))doReturn "Testing exception"
        presenter.onExportAndShareDB()
        presenter.messageChannel.test {
            val item = awaitItem()
            assertTrue(item == "Testing exception")
        }
    }
}
