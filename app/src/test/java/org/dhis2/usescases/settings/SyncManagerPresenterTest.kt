package org.dhis2.usescases.settings

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkInfo
import io.reactivex.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import org.dhis2.commons.Constants.DATA_NOW
import org.dhis2.commons.Constants.META_NOW
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.data.server.UserManager
import org.dhis2.data.service.VersionRepository
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.data.service.workManager.WorkerItem
import org.dhis2.data.service.workManager.WorkerType
import org.dhis2.usescases.settings.models.DataSettingsViewModel
import org.dhis2.usescases.settings.models.ErrorModelMapper
import org.dhis2.usescases.settings.models.MetadataSettingsViewModel
import org.dhis2.usescases.settings.models.ReservedValueSettingsViewModel
import org.dhis2.usescases.settings.models.SMSSettingsViewModel
import org.dhis2.usescases.settings.models.SyncParametersViewModel
import org.dhis2.utils.analytics.AnalyticsHelper
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.settings.LimitScope
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SyncManagerPresenterTest {

    private lateinit var presenter: SyncManagerPresenter
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val schedulers = TrampolineSchedulerProvider()
    private val gatewayValidator: GatewayValidator = mock()
    private val preferencesProvider: PreferenceProvider = mock()
    private val workManagerController: WorkManagerController = mock()
    private val settingsRepository: SettingsRepository = mock()
    private val userManager: UserManager =
        Mockito.mock(UserManager::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val view: SyncManagerContracts.View = mock()
    private val analyticsHelper: AnalyticsHelper = mock()
    private val errorMapper: ErrorModelMapper = mock()
    private val matomoAnalyticsController: MatomoAnalyticsController = mock()
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
        presenter = SyncManagerPresenter(
            d2,
            schedulers,
            gatewayValidator,
            preferencesProvider,
            workManagerController,
            settingsRepository,
            userManager,
            view,
            analyticsHelper,
            errorMapper,
            matomoAnalyticsController,
            resourcesManager,
            versionRepository,
            dispatcherProvider
        )
    }

    @Test
    fun `Should init settings values`() {
        whenever(resourcesManager.getString(any())) doReturn ""
        presenter.init()
        whenever(
            settingsRepository.metaSync(userManager)
        ) doReturn Single.just(mockedMetaViewModel())
        whenever(settingsRepository.dataSync()) doReturn Single.just(mockedDataViewModel())
        whenever(settingsRepository.syncParameters()) doReturn Single.just(mockedParamsViewModel())
        whenever(settingsRepository.reservedValues()) doReturn Single.just(
            mockedReservecValuesViewModel()
        )
        whenever(settingsRepository.sms()) doReturn Single.just(mockedSMSViewModel())

        presenter.init()

        verify(view).setMetadataSettings(mockedMetaViewModel())
        verify(view).setDataSettings(mockedDataViewModel())
        verify(view).setParameterSettings(mockedParamsViewModel())
        verify(view).setReservedValuesSettings(mockedReservecValuesViewModel())
        verify(view).setSMSSettings(mockedSMSViewModel())
    }

    @Test
    fun `should call work in progress`() {
        presenter.onWorkStatusesUpdate(WorkInfo.State.ENQUEUED, META_NOW)
        presenter.onWorkStatusesUpdate(WorkInfo.State.RUNNING, META_NOW)
        presenter.onWorkStatusesUpdate(WorkInfo.State.BLOCKED, META_NOW)

        presenter.onWorkStatusesUpdate(WorkInfo.State.ENQUEUED, DATA_NOW)
        presenter.onWorkStatusesUpdate(WorkInfo.State.RUNNING, DATA_NOW)
        presenter.onWorkStatusesUpdate(WorkInfo.State.BLOCKED, DATA_NOW)

        verify(view, times(3)).onMetadataSyncInProgress()
        verify(view, times(3)).onDataSyncInProgress()
    }

    @Test
    fun `should call work finished`() {
        presenter.onWorkStatusesUpdate(null, META_NOW)
        presenter.onWorkStatusesUpdate(WorkInfo.State.SUCCEEDED, META_NOW)
        presenter.onWorkStatusesUpdate(WorkInfo.State.FAILED, META_NOW)
        presenter.onWorkStatusesUpdate(WorkInfo.State.CANCELLED, META_NOW)

        presenter.onWorkStatusesUpdate(null, DATA_NOW)
        presenter.onWorkStatusesUpdate(WorkInfo.State.SUCCEEDED, DATA_NOW)
        presenter.onWorkStatusesUpdate(WorkInfo.State.FAILED, DATA_NOW)
        presenter.onWorkStatusesUpdate(WorkInfo.State.CANCELLED, DATA_NOW)

        verify(view, times(4)).onMetadataFinished()
        verify(view, times(4)).onDataFinished()
    }

    private fun mockedMetaViewModel(): MetadataSettingsViewModel {
        return MetadataSettingsViewModel(
            100,
            "test",
            false,
            canEdit = false
        )
    }

    private fun mockedDataViewModel(): DataSettingsViewModel {
        return DataSettingsViewModel(
            100,
            "test",
            false,
            false,
            false,
            false
        )
    }

    private fun mockedParamsViewModel(): SyncParametersViewModel {
        return SyncParametersViewModel(
            100,
            100,
            100,
            100,
            LimitScope.GLOBAL,
            false,
            false,
            false,
            5
        )
    }

    private fun mockedReservecValuesViewModel(): ReservedValueSettingsViewModel {
        return ReservedValueSettingsViewModel(20, true)
    }

    private fun mockedSMSViewModel(): SMSSettingsViewModel {
        return SMSSettingsViewModel(
            true,
            "test",
            "test",
            10,
            false,
            false,
            false
        )
    }

    @Test
    fun `Should return metadata period setting`() {
        whenever(settingsRepository.metaSync(userManager)) doReturn Single.just(
            MetadataSettingsViewModel(
                100,
                "last date",
                hasErrors = false,
                canEdit = true
            )
        )
        val period = presenter.metadataPeriodSetting
        assert(period == 100)
    }

    @Test
    fun `Should return data period setting`() {
        whenever(settingsRepository.dataSync()) doReturn Single.just(
            DataSettingsViewModel(
                100,
                "last date",
                false,
                dataHasErrors = true,
                dataHasWarnings = true,
                canEdit = true
            )
        )
        val period = presenter.dataPeriodSetting
        assert(period == 100)
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
    fun `Should save gateway if validation passes`() {
        val gatewayNumberTest = "+11111111111"
        whenever(gatewayValidator.validate(gatewayNumberTest)) doReturn true
        presenter.saveGatewayNumber(gatewayNumberTest)
        verify(settingsRepository, times(1)).saveGatewayNumber(gatewayNumberTest)
    }

    @Test
    fun `Should not save gateway if validation fails`() {
        val gatewayNumberTest = "+111"
        presenter.saveGatewayNumber(gatewayNumberTest)
        verify(settingsRepository, times(0)).saveGatewayNumber(gatewayNumberTest)
    }

    @Test
    fun `Should save sms result sender`() {
        presenter.saveSmsResultSender("test")
        verify(settingsRepository, times(1)).saveSmsResultSender("test")
    }

    @Test
    fun `Should save timeout`() {
        presenter.saveSmsResponseTimeout(any())
        verify(settingsRepository, times(1)).saveSmsResponseTimeout(any())
    }

    @Test
    fun `Should save wait for response`() {
        presenter.saveWaitForSmsResponse(any())
        verify(settingsRepository, times(1)).saveWaitForSmsResponse(any())
    }

    @Test
    fun `Should sync data`() {
        presenter.syncData(10, "tag")
        verify(workManagerController, times(1)).cancelUniqueWork("tag")
        verify(workManagerController, times(1)).enqueuePeriodicWork(
            WorkerItem(
                "tag",
                WorkerType.DATA,
                10,
                null,
                null,
                ExistingPeriodicWorkPolicy.REPLACE
            )
        )
    }

    @Test
    fun `Should sync metadata`() {
        presenter.syncMeta(10, "tag")
        verify(workManagerController, times(1)).cancelUniqueWork("tag")
        verify(workManagerController, times(1)).enqueuePeriodicWork(
            WorkerItem(
                "tag",
                WorkerType.METADATA,
                10,
                null,
                null,
                ExistingPeriodicWorkPolicy.REPLACE
            )
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
        presenter.cancelPendingWork("tag")
        verify(workManagerController, times(1)).cancelUniqueWork("tag")
    }

    @Test
    fun `Should reset parameters to default`() {
        presenter.resetSyncParameters()
        verify(preferencesProvider, times(4)).setValue(any(), any())
    }

    @Test
    fun `Should open clicked item`() {
        presenter.onItemClick(SettingItem.DATA_SYNC)
        verify(view).openItem(SettingItem.DATA_SYNC)
    }

    @Test
    fun `Should enabled sms settings when gateway and timeout are correctly filled`() {
        whenever(view.isGatewayValid) doReturn true
        whenever(view.isResultTimeoutValid) doReturn true
        presenter.setSmsSettingsViewModel(mockedSMSViewModel())

        presenter.checkGatewayAndTimeoutAreValid()
        verify(view).isGatewayValid
        verify(view).isResultTimeoutValid
        verify(view).enabledSMSSwitchAndSender(mockedSMSViewModel())
    }

    @Test
    fun `Should not enabled sms settings when gateway and timeout are missing`() {
        whenever(view.isGatewayValid) doReturn false
        whenever(view.isResultTimeoutValid) doReturn false
        presenter.checkGatewayAndTimeoutAreValid()
        verify(view).isGatewayValid
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `Should not enabled sms settings when gateway has an error and timeout is filled`() {
        whenever(view.isGatewayValid) doReturn false
        whenever(view.isResultTimeoutValid) doReturn true
        presenter.checkGatewayAndTimeoutAreValid()
        verify(view).isGatewayValid
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `Should not enabled sms settings when gateway is correctly filled and timeout is empty`() {
        whenever(view.isGatewayValid) doReturn true
        whenever(view.isResultTimeoutValid) doReturn false
        presenter.checkGatewayAndTimeoutAreValid()
        verify(view).isGatewayValid
        verify(view).isResultTimeoutValid
        verifyNoMoreInteractions(view)
    }
}
