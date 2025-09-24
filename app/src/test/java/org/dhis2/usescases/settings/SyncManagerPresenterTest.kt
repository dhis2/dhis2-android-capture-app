package org.dhis2.usescases.settings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.bindings.toDate
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
import org.dhis2.usescases.settings.models.DataSettingsViewModel
import org.dhis2.usescases.settings.models.ErrorViewModel
import org.dhis2.usescases.settings.models.MetadataSettingsViewModel
import org.dhis2.usescases.settings.models.ReservedValueSettingsViewModel
import org.dhis2.usescases.settings.models.SMSSettingsViewModel
import org.dhis2.usescases.settings.models.SettingsState
import org.dhis2.usescases.settings.models.SyncParametersViewModel
import org.hisp.dhis.android.core.settings.LimitScope
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doReturnConsecutively
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
    private val networkUtils: NetworkUtils = mock()
    private val testingDispatcher = UnconfinedTestDispatcher()
    private val dispatcherProvider: DispatcherProvider =
        mock {
            on { io() } doReturn testingDispatcher
            on { ui() } doReturn testingDispatcher
        }

    private val getSettingsState: GetSettingsState = mock()
    private val updateSyncSettings: UpdateSyncSettings = mock()
    private val updateSmsResponse: UpdateSmsResponse = mock()
    private val getSyncErrors: GetSyncErrors = mock()
    private val settingMessages: SettingsMessages =
        mock {
            on { messageChannel } doReturn Channel<String>().receiveAsFlow()
        }
    private val updateSmsModule: UpdateSmsModule = mock()
    private val deleteLocalData: DeleteLocalData = mock()
    private val exportDatabase: ExportDatabase = mock()
    private val checkVersionUpdate: CheckVersionUpdate = mock()
    private val launchSync: LaunchSync = mock()

    private val mockedSyncedStatusProgress =
        MutableStateFlow(
            LaunchSync.SyncStatusProgress(
                LaunchSync.SyncStatus.None,
                LaunchSync.SyncStatus.None,
            ),
        )

    @Before
    fun setUp() {
        Dispatchers.setMain(testingDispatcher)
        whenever(launchSync.syncWorkInfo) doReturn mockedSyncedStatusProgress
        whenever(networkUtils.connectionStatus) doReturn MutableStateFlow(true)

        presenter =
            SyncManagerPresenter(
                getSettingsState = getSettingsState,
                updateSyncSettings = updateSyncSettings,
                updateSmsResponse = updateSmsResponse,
                getSyncErrors = getSyncErrors,
                updateSmsModule = updateSmsModule,
                deleteLocalData = deleteLocalData,
                exportDatabase = exportDatabase,
                checkVersionUpdate = checkVersionUpdate,
                launchSync = launchSync,
                dispatcherProvider = dispatcherProvider,
                networkUtils = networkUtils,
                settingsMessages = settingMessages,
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `should check version update`() =
        runTest {
            presenter.onCheckVersionUpdate()
            verify(checkVersionUpdate, times(1)).invoke()
        }

    @Test
    fun `Should delete local data`() =
        runTest {
            presenter.deleteLocalData()
            verify(deleteLocalData, times(1)).invoke()
        }

    private fun mockedMetaViewModel(): MetadataSettingsViewModel =
        MetadataSettingsViewModel(
            metadataSyncPeriod = 100,
            lastMetadataSync = "test",
            hasErrors = false,
            canEdit = false,
            syncInProgress = false,
        )

    private fun mockedDataViewModel(): DataSettingsViewModel =
        DataSettingsViewModel(
            dataSyncPeriod = 100,
            lastDataSync = "test",
            syncHasErrors = false,
            dataHasErrors = false,
            dataHasWarnings = false,
            canEdit = false,
            syncInProgress = false,
        )

    private fun mockedParamsViewModel(): SyncParametersViewModel =
        SyncParametersViewModel(
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

    private fun mockedReservecValuesViewModel(): ReservedValueSettingsViewModel = ReservedValueSettingsViewModel(20, true)

    private fun mockedSMSViewModel(): SMSSettingsViewModel =
        SMSSettingsViewModel(
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

    @Test
    fun `Should save limit scope`() =
        runTest {
            presenter.saveLimitScope(LimitScope.GLOBAL)
            verify(
                updateSyncSettings,
                times(1),
            ).invoke(UpdateSyncSettings.SyncSettings.Scope(LimitScope.GLOBAL))
        }

    @Test
    fun `Should save event max count`() =
        runTest {
            presenter.saveEventMaxCount(200)
            verify(updateSyncSettings, times(1)).invoke(
                UpdateSyncSettings.SyncSettings.EventMaxCount(
                    200,
                ),
            )
        }

    @Test
    fun `Should save tei max count`() =
        runTest {
            presenter.saveTeiMaxCount(100)
            verify(
                updateSyncSettings,
                times(1),
            ).invoke(UpdateSyncSettings.SyncSettings.TeiMaxCount(100))
        }

    @Test
    fun `Should save reserved values to download`() =
        runTest {
            presenter.saveReservedValues(50)
            verify(updateSyncSettings, times(1)).invoke(
                UpdateSyncSettings.SyncSettings.ReservedValues(
                    50,
                ),
            )
        }

    @Test
    fun `Should save gateway and timeout if validation passes`() =
        runTest {
            val gatewayNumberTest = "+11111111111"
            val timeoutTest = 1
            whenever(
                getSettingsState.invoke(
                    anyOrNull(),
                    any(),
                    any(),
                    any(),
                ),
            ) doReturnConsecutively
                listOf(
                    mockedSettingState(),
                    mockedSettingState().copy(
                        smsSettingsViewModel =
                            mockedSMSViewModel().copy(
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
    fun `Should not save gateway if validation fails`() =
        runTest {
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
            ) doReturn
                UpdateSmsModule.EnableSmsResult.ValidationError(
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
    fun `Should save sms result sender`() =
        runTest {
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
    fun `Should set validation error message in result sender`() =
        runTest {
            val smsResultSender = "test"
            whenever(
                getSettingsState.invoke(
                    anyOrNull(),
                    any(),
                    any(),
                    any(),
                ),
            ) doReturn mockedSettingState()
            whenever(updateSmsResponse(any())) doReturn
                UpdateSmsResponse.UpdateSmsResponseResult.ValidationError(
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
    fun `Should sync data`() =
        runTest {
            presenter.onSyncDataPeriodChanged(10)
            verify(launchSync, times(1)).invoke(LaunchSync.SyncAction.UpdateSyncDataPeriod(10))
        }

    @Test
    fun `Should sync metadata`() =
        runTest {
            presenter.onSyncMetaPeriodChanged(10)
            verify(launchSync, times(1)).invoke(LaunchSync.SyncAction.UpdateSyncMetadataPeriod(10))
        }

    @Test
    fun `Should sync metadata now`() =
        runTest {
            presenter.syncMeta()
            verify(launchSync, times(1)).invoke(LaunchSync.SyncAction.SyncMetadata)
        }

    @Test
    fun `Should sync data now`() =
        runTest {
            presenter.syncData()
            verify(launchSync, times(1)).invoke(LaunchSync.SyncAction.SyncData)
        }

    @Test
    fun `Should load data when setting manual trigger`() =
        runTest {
            whenever(
                getSettingsState.invoke(
                    anyOrNull(),
                    any(),
                    any(),
                    any(),
                ),
            ) doReturnConsecutively
                listOf(
                    mockedSettingState(),
                    mockedSettingState().copy(
                        dataSettingsViewModel = mockedDataViewModel().copy(dataSyncPeriod = 0),
                    ),
                )

            presenter.settingsState.test {
                awaitItem()
                presenter.onSyncDataPeriodChanged(Constants.TIME_MANUAL)
                verify(
                    launchSync,
                    times(1),
                ).invoke(LaunchSync.SyncAction.UpdateSyncDataPeriod(Constants.TIME_MANUAL))
                val item = awaitItem()
                assertTrue(item?.dataSettingsViewModel?.dataSyncPeriod == 0)
                ensureAllEventsConsumed()
            }
        }

    @Test
    fun `Should reset parameters to default`() =
        runTest {
            presenter.resetSyncParameters()
            verify(updateSyncSettings, times(1)).invoke(UpdateSyncSettings.SyncSettings.Reset)
        }

    @Test
    fun `Should open clicked item`() =
        runTest {
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
    fun `Should share database`() =
        runTest {
            val mockedFile: File = mock()
            whenever(
                exportDatabase(ExportDatabase.ExportType.Share),
            ) doReturn
                ExportDatabase.ExportResult.Share(
                    mockedFile,
                )

            presenter.fileToShareChannel.test {
                presenter.onExportAndShareDB()
                val item = awaitItem()
                assertTrue(item == mockedFile)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `Should download database`() =
        runTest {
            presenter.onExportAndDownloadDB()
            verify(exportDatabase, times(1)).invoke()
        }

    @Test
    fun `Should load sync errors`() =
        runTest {
            val testingList =
                listOf(
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

    @Test
    fun shouldUpdateSyncStatus() =
        runTest {
            whenever(
                getSettingsState.invoke(
                    anyOrNull(),
                    any(),
                    any(),
                    any(),
                ),
            ) doReturn mockedSettingState()

            val dataSyncStartedProgress =
                LaunchSync.SyncStatusProgress(
                    metadataSyncProgress = LaunchSync.SyncStatus.InProgress,
                    dataSyncProgress = LaunchSync.SyncStatus.InProgress,
                )

            presenter.settingsState.test {
                awaitItem()
                mockedSyncedStatusProgress.emit(dataSyncStartedProgress)
                val item = awaitItem()
                assertTrue(
                    item?.metadataSettingsViewModel?.syncInProgress == true,
                )
                assertTrue(
                    item?.dataSettingsViewModel?.syncInProgress == true,
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

    private fun mockedSettingState() =
        SettingsState(
            openedItem = null,
            hasConnection = true,
            metadataSettingsViewModel = mockedMetaViewModel(),
            dataSettingsViewModel = mockedDataViewModel(),
            syncParametersViewModel = mockedParamsViewModel(),
            reservedValueSettingsViewModel = mockedReservecValuesViewModel(),
            smsSettingsViewModel = mockedSMSViewModel(),
            isTwoFAConfigured = true,
            versionName = "1.0.0",
        )
}
