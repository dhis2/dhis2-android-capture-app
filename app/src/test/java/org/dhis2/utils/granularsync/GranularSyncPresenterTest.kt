package org.dhis2.utils.granularsync

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.dhis2.commons.Constants
import org.dhis2.commons.sync.ConflictType
import org.dhis2.commons.sync.SyncContext
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.usescases.sms.SmsSendingService
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.dataset.DataSet
import org.hisp.dhis.android.core.dataset.DataSetElement
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class GranularSyncPresenterTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val view = Mockito.mock(GranularSyncContracts.View::class.java)
    private val repository: GranularSyncRepository = mock()
    private val trampolineSchedulerProvider = TrampolineSchedulerProvider()
    private val testingDispatcher = UnconfinedTestDispatcher()
    private val testDispatcher: DispatcherProvider =
        mock {
            on { io() } doReturn testingDispatcher
            on { ui() } doReturn testingDispatcher
        }
    private val workManager = Mockito.mock(WorkManagerController::class.java)
    private val smsSyncProvider: SMSSyncProvider = mock()
    private val context: Context = mock()
    private val syncContext: SyncContext = SyncContext.Global()

    @Before
    fun setUp() {
        Dispatchers.setMain(testingDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `should return tracker program error state`() {
        val presenter =
            GranularSyncPresenter(
                d2,
                view,
                repository,
                trampolineSchedulerProvider,
                testDispatcher,
                SyncContext.TrackerProgram("test_uid"),
                workManager,
                smsSyncProvider,
            )

        val mockedState =
            SyncUiState(
                syncState = State.ERROR,
                title = "Title",
                lastSyncDate = SyncDate(Date()),
                message = "message",
                mainActionLabel = "action 1",
                secondaryActionLabel = "action 2",
                content = emptyList(),
            )
        whenever(repository.getUiState()) doReturn mockedState

        presenter.refreshContent()
        val result = presenter.currentState.value
        assertEquals(mockedState, result)
    }

    @Test
    fun `should block sms for some conflict types`() {
        whenever(
            smsSyncProvider.isSMSEnabled(any()),
        ) doReturn true

        val syncContexts =
            listOf(
                SyncContext.Global(),
                SyncContext.GlobalTrackerProgram(""),
                SyncContext.TrackerProgram(""),
                SyncContext.TrackerProgramTei(""),
                SyncContext.Enrollment(""),
                SyncContext.EnrollmentEvent("", ""),
                SyncContext.GlobalEventProgram(""),
                SyncContext.EventProgram(""),
                SyncContext.Event(""),
                SyncContext.GlobalDataSet(""),
                SyncContext.DataSet(""),
                SyncContext.DataSetInstance("", "", "", ""),
            )

        syncContexts
            .map {
                val enable =
                    GranularSyncPresenter(
                        d2,
                        view,
                        repository,
                        trampolineSchedulerProvider,
                        testDispatcher,
                        it,
                        workManager,
                        smsSyncProvider,
                    ).canSendSMS()
                it to enable
            }.forEach { (syncContext, canSendSMS) ->
                val expected =
                    when (syncContext.conflictType()) {
                        ConflictType.ALL -> false
                        ConflictType.PROGRAM -> false
                        ConflictType.TEI -> true
                        ConflictType.EVENT -> true
                        ConflictType.DATA_SET -> false
                        ConflictType.DATA_VALUES -> true
                    }
                assertEquals(expected, canSendSMS)
            }
    }

    @Test
    fun shouldSmsSyncUsingPlayServices() {
        val presenter =
            GranularSyncPresenter(
                d2,
                view,
                repository,
                trampolineSchedulerProvider,
                testDispatcher,
                syncContext,
                workManager,
                smsSyncProvider,
            )

        val testingMsg = "testingMsg"
        val testingGateway = "testingGateWay"
        whenever(smsSyncProvider.isPlayServicesEnabled()) doReturn true
        whenever(smsSyncProvider.getConvertTask()) doReturn
            Single.just(
                ConvertTaskResult.Message(testingMsg),
            )
        whenever(smsSyncProvider.getGatewayNumber()) doReturn testingGateway
        presenter.onSmsSyncClick { }

        verify(smsSyncProvider, times(1)).getConvertTask()
        verify(view, times(1)).openSmsApp(testingMsg, testingGateway)
    }

    @Test
    fun shouldUnregisterReceiverIfSmsNotSent() {
        val presenter =
            GranularSyncPresenter(
                d2,
                view,
                repository,
                trampolineSchedulerProvider,
                testDispatcher,
                syncContext,
                workManager,
                smsSyncProvider,
            )

        presenter.onSmsNotManuallySent(context)
        verify(smsSyncProvider).unregisterSMSReceiver(context)
    }

    @Test
    fun shouldInitDefaultSmsSync() {
        val presenter =
            GranularSyncPresenter(
                d2,
                view,
                repository,
                trampolineSchedulerProvider,
                testDispatcher,
                syncContext,
                workManager,
                smsSyncProvider,
            )

        whenever(smsSyncProvider.isPlayServicesEnabled()) doReturn false
        whenever(view.checkSmsPermission()) doReturn true
        whenever(
            smsSyncProvider.getConvertTask(),
        ) doReturn Single.just(ConvertTaskResult.Count(1))
        whenever(smsSyncProvider.smsSender) doReturn mock()
        whenever(smsSyncProvider.smsSender.submissionId) doReturn 1
        var testingState: LiveData<List<SmsSendingService.SendingStatus>> =
            MutableLiveData(emptyList())

        presenter.onSmsSyncClick {
            testingState = it
        }

        assertTrue(testingState.value?.isNotEmpty() == true)
        assertTrue(
            testingState.value?.get(0)?.state == SmsSendingService.State.WAITING_COUNT_CONFIRMATION,
        )
    }

    @Test
    fun shouldSetSmsSent() {
        val presenter =
            GranularSyncPresenter(
                d2,
                view,
                repository,
                trampolineSchedulerProvider,
                testDispatcher,
                syncContext,
                workManager,
                smsSyncProvider,
            )

        whenever(smsSyncProvider.expectsResponseSMS()) doReturn false
        whenever(smsSyncProvider.smsSender) doReturn mock()
        whenever(smsSyncProvider.smsSender.markAsSentViaSMS()) doReturn Completable.complete()
        presenter.onSmsManuallySent(context) {
        }

        verify(repository).getUiState()
    }

    @Test
    fun shouldWaitForSMSResponse() {
        val presenter =
            GranularSyncPresenter(
                d2,
                view,
                repository,
                trampolineSchedulerProvider,
                testDispatcher,
                syncContext,
                workManager,
                smsSyncProvider,
            )

        whenever(smsSyncProvider.expectsResponseSMS()) doReturn true
        presenter.onSmsManuallySent(context) {
        }

        verify(repository, times(0)).getUiState()
    }

    @Test
    fun shouldConfirmSmsReceived() {
        val presenter =
            GranularSyncPresenter(
                d2,
                view,
                repository,
                trampolineSchedulerProvider,
                testDispatcher,
                syncContext,
                workManager,
                smsSyncProvider,
            )

        whenever(smsSyncProvider.smsSender) doReturn mock()
        whenever(smsSyncProvider.smsSender.markAsSentViaSMS()) doReturn mock()

        presenter.onConfirmationMessageStateChanged(true)

        verify(repository).getUiState()
    }

    @Test
    fun shouldInformSmsWasNotReceived() {
        val presenter =
            GranularSyncPresenter(
                d2,
                view,
                repository,
                trampolineSchedulerProvider,
                testDispatcher,
                syncContext,
                workManager,
                smsSyncProvider,
            )

        whenever(smsSyncProvider.smsSender) doReturn mock()
        whenever(smsSyncProvider.smsSender.markAsSentViaSMS()) doReturn mock()

        presenter.onConfirmationMessageStateChanged(false)

        verify(repository).getUiState()
    }

    @Test
    fun shouldSyncProgram() {
        val presenter =
            GranularSyncPresenter(
                d2,
                view,
                repository,
                trampolineSchedulerProvider,
                testDispatcher,
                SyncContext.TrackerProgram("programUid"),
                workManager,
                smsSyncProvider,
            )

        val workInfoList = MutableLiveData<List<WorkInfo>>(emptyList())
        whenever(workManager.getWorkInfosForUniqueWorkLiveData(any())) doReturn workInfoList

        val workInfoObserver: Observer<List<WorkInfo>> = mock()
        val resultLiveData = presenter.initGranularSync()
        resultLiveData.observeForever(workInfoObserver)

        verify(workManager).beginUniqueWork(any())
        verify(workInfoObserver).onChanged(anyList())
    }

    @Test
    fun shouldSyncTei() {
        val presenter =
            GranularSyncPresenter(
                d2,
                view,
                repository,
                trampolineSchedulerProvider,
                testDispatcher,
                SyncContext.TrackerProgramTei("enrollmentUid"),
                workManager,
                smsSyncProvider,
            )

        val workInfoList = MutableLiveData<List<WorkInfo>>(emptyList())
        whenever(workManager.getWorkInfosForUniqueWorkLiveData(any())) doReturn workInfoList

        val workInfoObserver: Observer<List<WorkInfo>> = mock()
        val resultLiveData = presenter.initGranularSync()
        resultLiveData.observeForever(workInfoObserver)

        verify(workManager).beginUniqueWork(any())
        verify(workInfoObserver).onChanged(anyList())
    }

    @Test
    fun shouldSyncEvent() {
        val presenter =
            GranularSyncPresenter(
                d2,
                view,
                repository,
                trampolineSchedulerProvider,
                testDispatcher,
                SyncContext.Event("eventUid"),
                workManager,
                smsSyncProvider,
            )

        val workInfoList = MutableLiveData<List<WorkInfo>>(emptyList())
        whenever(workManager.getWorkInfosForUniqueWorkLiveData(any())) doReturn workInfoList

        val workInfoObserver: Observer<List<WorkInfo>> = mock()
        val resultLiveData = presenter.initGranularSync()
        resultLiveData.observeForever(workInfoObserver)

        verify(workManager).beginUniqueWork(any())
        verify(workInfoObserver).onChanged(anyList())
    }

    @Test
    fun shouldSyncDataSet() {
        val presenter =
            GranularSyncPresenter(
                d2,
                view,
                repository,
                trampolineSchedulerProvider,
                testDispatcher,
                SyncContext.DataSet("dataSetUid"),
                workManager,
                smsSyncProvider,
            )

        val workInfoList = MutableLiveData<List<WorkInfo>>(emptyList())
        whenever(workManager.getWorkInfosForUniqueWorkLiveData(any())) doReturn workInfoList

        val workInfoObserver: Observer<List<WorkInfo>> = mock()
        val resultLiveData = presenter.initGranularSync()
        resultLiveData.observeForever(workInfoObserver)

        verify(workManager).beginUniqueWork(any())
        verify(workInfoObserver).onChanged(anyList())
    }

    @Test
    fun shouldSyncDataValues() {
        val presenter =
            GranularSyncPresenter(
                d2,
                view,
                repository,
                trampolineSchedulerProvider,
                testDispatcher,
                SyncContext.DataSetInstance(
                    "dataSetUid",
                    "periodId",
                    "orgUnitUid",
                    "attrOptionComboUid",
                ),
                workManager,
                smsSyncProvider,
            )

        val mockedDataSet: DataSet =
            mock {
                on { dataSetElements() } doReturn
                    listOf(
                        DataSetElement
                            .builder()
                            .categoryCombo(ObjectWithUid.create("catComboUid"))
                            .dataElement(ObjectWithUid.create("dataElementUid"))
                            .dataSet(ObjectWithUid.create("dataSetUid"))
                            .build(),
                    )
            }
        whenever(
            d2
                .dataSetModule()
                .dataSets()
                .withDataSetElements()
                .uid(any())
                .blockingGet(),
        ) doReturn mockedDataSet
        whenever(
            d2.categoryModule().categoryOptionCombos().byCategoryComboUid(),
        ) doReturn mock()
        whenever(
            d2
                .categoryModule()
                .categoryOptionCombos()
                .byCategoryComboUid()
                .`in`(anyList()),
        ) doReturn mock()
        whenever(
            d2
                .categoryModule()
                .categoryOptionCombos()
                .byCategoryComboUid()
                .`in`(anyList())
                .blockingGetUids(),
        ) doReturn listOf("catComboUid")

        val workInfoList = MutableLiveData<List<WorkInfo>>(emptyList())
        whenever(workManager.getWorkInfosForUniqueWorkLiveData(any())) doReturn workInfoList
        val workInfoObserver: Observer<List<WorkInfo>> = mock()
        val resultLiveData = presenter.initGranularSync()
        resultLiveData.observeForever(workInfoObserver)

        verify(workManager).beginUniqueWork(any())
        verify(workInfoObserver).onChanged(anyList())
    }

    @Test
    fun shouldPerformInitialSync() {
        val presenter =
            GranularSyncPresenter(
                d2,
                view,
                repository,
                trampolineSchedulerProvider,
                testDispatcher,
                SyncContext.Global(),
                workManager,
                smsSyncProvider,
            )

        val workInfoList = MutableLiveData<List<WorkInfo>>(emptyList())
        whenever(workManager.getWorkInfosForUniqueWorkLiveData(any())) doReturn workInfoList

        val workInfoObserver: Observer<List<WorkInfo>> = mock()
        val resultLiveData = presenter.initGranularSync()
        resultLiveData.observeForever(workInfoObserver)

        verify(workManager).syncDataForWorker(Constants.DATA_NOW, Constants.INITIAL_SYNC)
        verify(workInfoObserver).onChanged(anyList())
    }

    @Test
    fun shouldCheckAvailableConnection() =
        runBlocking {
            whenever(repository.checkServerAvailability()) doReturn "pong"

            val presenter =
                GranularSyncPresenter(
                    d2,
                    view,
                    repository,
                    trampolineSchedulerProvider,
                    testDispatcher,
                    SyncContext.Global(),
                    workManager,
                    smsSyncProvider,
                )

            presenter.checkServerAvailability()

            assertTrue(presenter.serverAvailability.value!!)
        }

    @Test
    fun shouldCheckUnavailableConnection() =
        runBlocking {
            whenever(repository.checkServerAvailability()) doThrow RuntimeException()

            val presenter =
                GranularSyncPresenter(
                    d2,
                    view,
                    repository,
                    trampolineSchedulerProvider,
                    testDispatcher,
                    SyncContext.Global(),
                    workManager,
                    smsSyncProvider,
                )

            presenter.checkServerAvailability()

            assertFalse(presenter.serverAvailability.value!!)
        }
}
