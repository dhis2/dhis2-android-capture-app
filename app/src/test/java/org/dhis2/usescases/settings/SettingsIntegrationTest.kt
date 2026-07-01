package org.dhis2.usescases.settings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import io.reactivex.Single
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.usescases.settings.GatewayValidator.GatewayValidationResult
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
import org.dhis2.usescases.settings.models.MetadataSettingsViewModel
import org.dhis2.usescases.settings.models.ReservedValueSettingsViewModel
import org.dhis2.usescases.settings.models.SMSSettingsViewModel
import org.dhis2.usescases.settings.models.SyncParametersViewModel
import org.dhis2.utils.MainCoroutineScopeRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class SettingsIntegrationTest {
    private val settingsMessages: SettingsMessages = mock()
    private val networkUtils: NetworkUtils = mock()
    private val launchSync: LaunchSync = mock()
    private val checkVersionUpdate: CheckVersionUpdate = mock()
    private val exportDatabase: ExportDatabase = mock()
    private val deleteLocalData: DeleteLocalData = mock()
    private val updateSmsModule: UpdateSmsModule = mock()
    private val getSyncErrors: GetSyncErrors = mock()
    private val updateSmsResponse: UpdateSmsResponse = mock()
    private val updateSyncSettings: UpdateSyncSettings = mock()
    private val metadataSettingsViewModel: MetadataSettingsViewModel = mock()
    private val dataSettingsViewModel: DataSettingsViewModel = mock()
    private val syncParametersViewModel: SyncParametersViewModel = mock()
    private val reservedValueSettingsViewModel: ReservedValueSettingsViewModel = mock()
    private val smsSettingsViewModel: SMSSettingsViewModel =
        mock {
            on { gatewayNumber } doReturn ""
        }
    private val settingsRepository: SettingsRepository =
        mock {
            on { metaSync() } doReturn Single.just(metadataSettingsViewModel)
            on { dataSync() } doReturn Single.just(dataSettingsViewModel)
            on { syncParameters() } doReturn Single.just(syncParametersViewModel)
            on { reservedValues() } doReturn Single.just(reservedValueSettingsViewModel)
            on { sms() } doReturn Single.just(smsSettingsViewModel)
            on { getVersionName() } doReturn "1.0"
        }
    private val gatewayValidator: GatewayValidator =
        mock {
            on { invoke("") } doReturn GatewayValidationResult.Empty
        }
    private val getSettingsState: GetSettingsState =
        GetSettingsState(settingsRepository, gatewayValidator)

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testingDispatcher = StandardTestDispatcher()

    private lateinit var syncManagerPresenter: SyncManagerPresenter

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(testingDispatcher)

        whenever(
            settingsRepository.metaSync().blockingGet().copy(syncInProgress = false),
        ) doReturn metadataSettingsViewModel
        whenever(
            settingsRepository.dataSync().blockingGet().copy(syncInProgress = false),
        ) doReturn dataSettingsViewModel
        whenever(
            with(settingsRepository.sms().blockingGet()) {
                copy(
                    gatewayValidationResult = gatewayValidator(this.gatewayNumber),
                )
            },
        ) doReturn smsSettingsViewModel
        whenever(networkUtils.connectionStatus) doReturn flowOf(false)
        whenever(launchSync.syncWorkInfo) doReturn mock()
    }

    private fun buildPresenter() {
        syncManagerPresenter =
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
                dispatcherProvider =
                    object : DispatcherProvider {
                        override fun io(): CoroutineDispatcher = testingDispatcher

                        override fun computation(): CoroutineDispatcher = testingDispatcher

                        override fun ui(): CoroutineDispatcher = testingDispatcher
                    },
                networkUtils = networkUtils,
                settingsMessages = settingsMessages,
            )
    }

    @Test
    fun `should display TFA when it is configured`() =
        runTest {
            // Given TFA configured
            whenever(settingsRepository.isTwoFAConfigured()) doReturn true

            // When set settings config
            buildPresenter()

            // Then TFA should be displayed
            syncManagerPresenter.settingsState.test {
                assert(awaitItem() == null)
                assert(awaitItem()?.isTwoFAConfigured == true)
            }
        }

    @Test
    fun `should not display TFA when it is not configured`() =
        runTest {
            // Given TFA not configured
            whenever(settingsRepository.isTwoFAConfigured()) doReturn false

            // When set settings config
            buildPresenter()

            // Then TFA should not be displayed
            syncManagerPresenter.settingsState.test {
                assert(awaitItem() == null)
                assert(awaitItem()?.isTwoFAConfigured == false)
            }
        }
}
