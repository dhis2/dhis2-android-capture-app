package org.dhis2.usescases.settings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.work.ExistingPeriodicWorkPolicy
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.R
import org.dhis2.bindings.toDate
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
import org.dhis2.usescases.settings.domain.GetSettingsState
import org.dhis2.usescases.settings.domain.GetSyncErrors
import org.dhis2.usescases.settings.domain.SettingsMessages
import org.dhis2.usescases.settings.domain.UpdateSmsModule
import org.dhis2.usescases.settings.domain.UpdateSmsResponse
import org.dhis2.usescases.settings.domain.UpdateSyncSettings
import org.dhis2.usescases.settings.models.DataSettingsViewModel
import org.dhis2.usescases.settings.models.ErrorViewModel
import org.dhis2.usescases.settings.models.MetadataSettingsViewModel
import org.dhis2.usescases.settings.models.ReservedValueSettingsViewModel
import org.dhis2.usescases.settings.models.SMSSettingsViewModel
import org.dhis2.usescases.settings.models.SettingsState
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
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doReturnConsecutively
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
    private val preferencesProvider: PreferenceProvider = mock()
    private val workManagerController: WorkManagerController = mock()
    private val settingsRepository: SettingsRepository = mock()
    private val analyticsHelper: AnalyticsHelper = mock()
    private val fileHandler: FileHandler = mock()
    private val networkUtils: NetworkUtils = mock()
    private val resourcesManager: ResourceManager = mock()
    private val versionRepository: VersionRepository = mock()
    private val testingDispatcher = UnconfinedTestDispatcher()
    private val dispatcherProvider: DispatcherProvider = mock {
        on { io() } doReturn testingDispatcher
        on { ui() } doReturn testingDispatcher
    }

    private val getSettingsState: GetSettingsState = mock()
    private val updateSyncSettings: UpdateSyncSettings = mock()
    private val updateSmsResponse: UpdateSmsResponse = mock()
    private val getSyncErrors: GetSyncErrors = mock()
    private val settingMessages: SettingsMessages = mock {
        on { messageChannel } doReturn Channel<String>().receiveAsFlow()
    }
    private val updateSmsModule: UpdateSmsModule = mock()

    @Before
    fun setUp() {
        Dispatchers.setMain(testingDispatcher)
        whenever(versionRepository.newAppVersion) doReturn MutableSharedFlow()
        whenever(workManagerController.getWorkInfosByTagLiveData(any())) doReturn MutableLiveData()
        whenever(networkUtils.connectionStatus) doReturn MutableStateFlow(true)

        presenter = SyncManagerPresenter(
            getSettingsState = getSettingsState,
            updateSyncSettings = updateSyncSettings,
            updateSmsResponse = updateSmsResponse,
            getSyncErrors = getSyncErrors,
            updateSmsModule = updateSmsModule,
            preferenceProvider = preferencesProvider,
            workManagerController = workManagerController,
            settingsRepository = settingsRepository,
            analyticsHelper = analyticsHelper,
            resourceManager = resourcesManager,
            versionRepository = versionRepository,
            dispatcherProvider = dispatcherProvider,
            networkUtils = networkUtils,
            fileHandler = fileHandler,
            settingsMessages = settingMessages,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Should init settings values`() {
        presenter.init()
        verify(networkUtils).registerNetworkCallback()
    }

    @Test
    fun `Should unregister callbacks`() {
        presenter.dispose()
        verify(networkUtils).unregisterNetworkCallback()
    }

    @Test
    fun `should check version update`() = runTest {
        whenever(versionRepository.getLatestVersionInfo()) doReturn "new.version.name"
        presenter.checkVersionUpdate()
        verify(versionRepository, times(1)).checkVersionUpdates()
    }

    @Test
    fun `should send no new version message`() = runTest {
        whenever(versionRepository.getLatestVersionInfo()) doReturn null
        whenever(resourcesManager.getString(any())) doReturn "No updates"
        presenter.checkVersionUpdate()
        verify(settingMessages, times(1)).sendMessage("No updates")
        verify(versionRepository, times(0)).checkVersionUpdates()
    }

    @Test
    fun `Should delete local data`() = runTest {
        whenever(resourcesManager.getString(any())) doReturn "Local data deleted"
        presenter.deleteLocalData()
        verify(settingMessages, times(1)).sendMessage("Local data deleted")
    }

    @Test
    fun `Should display message if local data deletion fails`() = runTest {
        whenever(resourcesManager.getString(R.string.delete_local_data_error)) doReturn "Error while deleting local data"
        whenever(settingsRepository.deleteLocalData()) doThrow RuntimeException("Simulated error")
        presenter.deleteLocalData()
        verify(settingMessages, times(1)).sendMessage("Error while deleting local data")
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

    @Test
    fun `Should save limit scope`() = runTest {
        presenter.saveLimitScope(LimitScope.GLOBAL)
        verify(
            updateSyncSettings,
            times(1),
        ).invoke(UpdateSyncSettings.SyncSettings.Scope(LimitScope.GLOBAL))
    }

    @Test
    fun `Should save event max count`() = runTest {
        presenter.saveEventMaxCount(200)
        verify(updateSyncSettings, times(1)).invoke(
            UpdateSyncSettings.SyncSettings.EventMaxCount(
                200,
            ),
        )
    }

    @Test
    fun `Should save tei max count`() = runTest {
        presenter.saveTeiMaxCount(100)
        verify(
            updateSyncSettings,
            times(1),
        ).invoke(UpdateSyncSettings.SyncSettings.TeiMaxCount(100))
    }

    @Test
    fun `Should save reserved values to download`() = runTest {
        presenter.saveReservedValues(50)
        verify(updateSyncSettings, times(1)).invoke(
            UpdateSyncSettings.SyncSettings.ReservedValues(
                50,
            ),
        )
    }

    @Test
    fun `Should save gateway and timeout if validation passes`() = runTest {
        val gatewayNumberTest = "+11111111111"
        val timeoutTest = 1
        whenever(
            getSettingsState.invoke(
                anyOrNull(),
                any(),
                any(),
                any(),
            ),
        ) doReturnConsecutively listOf(
            mockedSettingState(),
            mockedSettingState().copy(
                smsSettingsViewModel = mockedSMSViewModel().copy(
                    isEnabled = true,
                    gatewayNumber = gatewayNumberTest,
                    responseTimeout = timeoutTest,
                ),
            ),
        )

        whenever(
            updateSmsModule(
                UpdateSmsModule.SmsSetting.Enable(
                    gatewayNumberTest,
                    timeoutTest,
                ),
            ),
        ) doReturn UpdateSmsModule.EnableSmsResult.Success

        presenter.settingsState.test {
            awaitItem()
            presenter.enableSmsModule(true, gatewayNumberTest, timeoutTest)
            awaitItem()
            verify(getSettingsState, times(2)).invoke(anyOrNull(), any(), any(), any())
        }
    }

    @Test
    fun `Should not save gateway if validation fails`() = runTest {
        whenever(
            getSettingsState.invoke(
                anyOrNull(),
                any(),
                any(),
                any(),
            ),
        ) doReturn mockedSettingState()

        val gatewayNumberTest = "+111"
        whenever(
            updateSmsModule(
                UpdateSmsModule.SmsSetting.Enable(
                    gatewayNumberTest,
                    0,
                ),
            ),
        ) doReturn UpdateSmsModule.EnableSmsResult.ValidationError(
            GatewayValidator.GatewayValidationResult.Invalid,
        )
        presenter.settingsState.test {
            awaitItem()
            presenter.enableSmsModule(true, gatewayNumberTest, 0)
            val updateState = awaitItem()
            assertTrue(updateState?.smsSettingsViewModel?.gatewayValidationResult == GatewayValidator.GatewayValidationResult.Invalid)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Should save sms result sender`() = runTest {
        val smsResultSender = "test"
        whenever(updateSmsResponse(any())) doReturn UpdateSmsResponse.UpdateSmsResponseResult.Success

        presenter.saveWaitForSmsResponse(true, smsResultSender)
        verify(updateSmsResponse, times(1)).invoke(
            UpdateSmsResponse.ResponseSetting.Enable(
                smsResultSender,
            ),
        )
    }

    @Test
    fun `Should set validation error message in result sender`() = runTest {
        val smsResultSender = "test"
        whenever(
            getSettingsState.invoke(
                anyOrNull(),
                any(),
                any(),
                any(),
            ),
        ) doReturn mockedSettingState()
        whenever(updateSmsResponse(any())) doReturn UpdateSmsResponse.UpdateSmsResponseResult.ValidationError(
            GatewayValidator.GatewayValidationResult.Invalid,
        )

        presenter.settingsState.test {
            awaitItem()
            presenter.saveWaitForSmsResponse(true, smsResultSender)
            with(awaitItem()) {
                assertTrue(this?.smsSettingsViewModel?.resultSenderValidationResult == GatewayValidator.GatewayValidationResult.Invalid)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Should sync data`() {
        presenter.onSyncDataPeriodChanged(10)
        verify(workManagerController, times(1)).cancelUniqueWork(Constants.DATA)
        verify(workManagerController, times(1)).enqueuePeriodicWork(
            WorkerItem(
                Constants.DATA,
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
        whenever(
            getSettingsState.invoke(
                anyOrNull(),
                any(),
                any(),
                any(),
            ),
        ) doReturn mockedSettingState()

        presenter.settingsState.test {
            awaitItem()
            presenter.onItemClick(SettingItem.DATA_SYNC)
            val item = awaitItem()
            assertTrue(item?.openedItem == SettingItem.DATA_SYNC)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Should export database`() = runTest {
        val mockedFile: File = mock()
        whenever(settingsRepository.exportDatabase()) doReturn mockedFile
        whenever(resourcesManager.getString(any())) doReturn "Database exported"
        presenter.onExportAndShareDB()
        verify(settingMessages, times(1)).sendMessage("Database exported")
    }

    @Test
    fun `Should display export database error`() = runTest {
        val errorMessage = "Database export failed!"
        val exceptionToThrow = RuntimeException("Simulated DB export error")
        whenever(settingsRepository.exportDatabase()) doThrow exceptionToThrow
        whenever(resourcesManager.parseD2Error(exceptionToThrow)) doReturn errorMessage
        whenever(resourcesManager.parseD2Error(any<Throwable>())) doAnswer { invocation ->
            val throwable = invocation.arguments[0] as Throwable
            if (throwable == exceptionToThrow) { // Check if it's the specific exception we threw
                errorMessage
            } else {
                "Some other error occurred" // Fallback for other potential errors
            }
        }

        presenter.onExportAndShareDB()
        verify(settingMessages, times(1)).sendMessage(errorMessage)
    }

    @Test
    fun `Should share database`() = runTest {
        val mockedFile: File = mock()
        whenever(settingsRepository.exportDatabase()) doReturn mockedFile

        presenter.onExportAndDownloadDB()
        presenter.fileToShareChannel.test {
            val item = awaitItem()
            assertTrue(item == mockedFile)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Should load sync errors`() = runTest {
        val testingList = listOf(
            ErrorViewModel(
                creationDate = "2025-03-02T00:00:00.00Z".toDate(),
                errorCode = "1",
                errorDescription = "d2 error",
                errorComponent = null,
            ),
            ErrorViewModel(
                creationDate = "2025-03-05T00:00:00.00Z".toDate(),
                errorCode = "2",
                errorDescription = "conflict",
                errorComponent = null,
            ),
            ErrorViewModel(
                creationDate = "2025-03-01T00:00:00.00Z".toDate(),
                errorCode = "3",
                errorDescription = "fk",
                errorComponent = null,
            ),
        )
        whenever(getSyncErrors()) doReturn testingList

        presenter.errorLogChannel.test {
            presenter.checkSyncErrors()
            val item = awaitItem()
            assertTrue(item == testingList)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun mockedSettingState() = SettingsState(
        openedItem = null,
        hasConnection = true,
        metadataSettingsViewModel = mockedMetaViewModel(),
        dataSettingsViewModel = mockedDataViewModel(),
        syncParametersViewModel = mockedParamsViewModel(),
        reservedValueSettingsViewModel = mockedReservecValuesViewModel(),
        smsSettingsViewModel = mockedSMSViewModel(),
    )
}
