package org.dhis2.utils.granularsync

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import java.time.Instant
import java.util.Date
import junit.framework.Assert.assertTrue
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.data.dhislogic.DhisProgramUtils
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.usescases.settings.models.ErrorModelMapper
import org.dhis2.usescases.sms.SmsSendingService
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.`object`.ReadOnlyOneObjectRepositoryFinalImpl
import org.hisp.dhis.android.core.common.Access
import org.hisp.dhis.android.core.common.DataAccess
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.dataset.DataSetCompleteRegistration
import org.hisp.dhis.android.core.imports.ImportStatus
import org.hisp.dhis.android.core.imports.TrackerImportConflict
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.hisp.dhis.android.core.maintenance.ForeignKeyViolation
import org.hisp.dhis.android.core.program.AccessLevel
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramCollectionRepository
import org.hisp.dhis.android.core.program.ProgramModule
import org.hisp.dhis.android.core.program.ProgramType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType
import org.junit.Rule
import org.junit.Test
import org.mockito.BDDMockito.then
import org.mockito.Mockito
import org.mockito.Mockito.mock

class GranularSyncPresenterTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val d2: D2 = mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val programUtils: DhisProgramUtils = mock()
    private val view = mock(GranularSyncContracts.View::class.java)
    private val trampolineSchedulerProvider = TrampolineSchedulerProvider()
    private val workManager = mock(WorkManagerController::class.java)
    private val programRepoMock = mock(ReadOnlyOneObjectRepositoryFinalImpl::class.java)
    private val errorMapper: ErrorModelMapper = ErrorModelMapper("%s %s %s %s")
    private val testProgram = getProgram()
    private val preferenceProvider: PreferenceProvider = mock()
    private val smsSyncProvider: SMSSyncProvider = mock()
    private val context: Context = mock()

    @Test
    fun simplePresenterTest() {
        // GIVEN
        val presenter = GranularSyncPresenterImpl(
            d2,
            programUtils,
            trampolineSchedulerProvider,
            SyncStatusDialog.ConflictType.PROGRAM,
            "test_uid",
            null,
            null,
            null,
            workManager,
            errorMapper,
            preferenceProvider,
            smsSyncProvider
        )
        Mockito.`when`(d2.programModule()).thenReturn(mock(ProgramModule::class.java))
        Mockito.`when`(d2.programModule().programs())
            .thenReturn(mock(ProgramCollectionRepository::class.java))
        Mockito.`when`(d2.programModule().programs().uid("test_uid"))
            .thenReturn(programRepoMock as ReadOnlyOneObjectRepositoryFinalImpl<Program>?)
        Mockito.`when`(d2.programModule().programs().uid("test_uid").get())
            .thenReturn(Single.just(testProgram))
        // WHEN
        presenter.configure(view)
        // THEN
        then(view).should().showTitle("DISPLAY_NAME_FIRST")
    }

    @Test
    fun `should return tracker program error state`() {
        val presenter = GranularSyncPresenterImpl(
            d2,
            programUtils,
            trampolineSchedulerProvider,
            SyncStatusDialog.ConflictType.PROGRAM,
            "test_uid",
            null,
            null,
            null,
            workManager,
            errorMapper,
            preferenceProvider,
            smsSyncProvider
        )

        whenever(
            programUtils.getProgramState("test_uid")
        ) doReturn State.ERROR
        val testSubscriber = presenter.getState().test()

        testSubscriber.assertSubscribed()
        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue(State.ERROR)
    }

    @Test
    fun `DataSet with ERROR completeRegistration should return ERROR from candidates`() {
        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations()
                .byDataSetUid()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations()
                .byDataSetUid().eq("data_set_uid")
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations()
                .byDataSetUid().eq("data_set_uid").blockingGet()
        ) doReturn getMockedCompleteRegistrations(State.ERROR)

        val presenter = GranularSyncPresenterImpl(
            d2,
            programUtils,
            trampolineSchedulerProvider,
            SyncStatusDialog.ConflictType.DATA_SET,
            "data_set_uid",
            null,
            null,
            null,
            workManager,
            errorMapper,
            preferenceProvider,
            smsSyncProvider
        )

        val state = presenter.getStateFromCanditates(arrayListOf())

        assertTrue(state == State.ERROR)
    }

    @Test
    fun `DataSet with TO_POST completeRegistration should return TO_UPDATE from candidates`() {
        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations()
                .byDataSetUid()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations()
                .byDataSetUid().eq("data_set_uid")
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations()
                .byDataSetUid().eq("data_set_uid").blockingGet()
        ) doReturn getMockedCompleteRegistrations(State.TO_POST)

        val presenter = GranularSyncPresenterImpl(
            d2,
            programUtils,
            trampolineSchedulerProvider,
            SyncStatusDialog.ConflictType.DATA_SET,
            "data_set_uid",
            null,
            null,
            null,
            workManager,
            errorMapper,
            preferenceProvider,
            smsSyncProvider
        )

        val state = presenter.getStateFromCanditates(arrayListOf())

        assertTrue(state == State.TO_UPDATE)
    }

    @Test
    fun `DataSet with TO_POST candidate should return TO_UPDATE from candidates`() {
        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations()
                .byDataSetUid()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations()
                .byDataSetUid().eq("data_set_uid")
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations()
                .byDataSetUid().eq("data_set_uid").blockingGet()
        ) doReturn arrayListOf()

        val presenter = GranularSyncPresenterImpl(
            d2,
            programUtils,
            trampolineSchedulerProvider,
            SyncStatusDialog.ConflictType.DATA_SET,
            "data_set_uid",
            null,
            null,
            null,
            workManager,
            errorMapper,
            preferenceProvider,
            smsSyncProvider
        )

        val state = presenter.getStateFromCanditates(arrayListOf(State.TO_POST))

        assertTrue(state == State.TO_UPDATE)
    }

    @Test
    fun `Should get list of sync errors order by date`() {
        val presenter = GranularSyncPresenterImpl(
            d2,
            programUtils,
            trampolineSchedulerProvider,
            SyncStatusDialog.ConflictType.PROGRAM,
            "test_uid",
            null,
            null,
            null,
            workManager,
            errorMapper,
            preferenceProvider,
            smsSyncProvider
        )

        whenever(
            d2.maintenanceModule().d2Errors().blockingGet()
        ) doReturn arrayListOf(
            D2Error.builder()
                .created(Date.from(Instant.parse("2020-01-01T00:00:00.00Z")))
                .errorCode(D2ErrorCode.API_RESPONSE_PROCESS_ERROR)
                .httpErrorCode(500)
                .errorDescription("ErrorDescription")
                .build()
        )
        whenever(
            d2.importModule().trackerImportConflicts().blockingGet()
        ) doReturn arrayListOf(
            TrackerImportConflict.builder()
                .created(Date.from(Instant.parse("2020-01-01T00:00:00.00Z")))
                .errorCode("API")
                .displayDescription("DisplayDescription")
                .conflict("Conflict")
                .status(ImportStatus.ERROR)
                .build()
        )
        whenever(
            d2.maintenanceModule().foreignKeyViolations().blockingGet()
        ) doReturn arrayListOf(
            ForeignKeyViolation.builder()
                .created(Date.from(Instant.parse("2020-01-02T12:00:00.00Z")))
                .toTable("ToTable")
                .fromTable("FromTable")
                .notFoundValue("NotFoundValue")
                .fromObjectUid("FromObjectUid")
                .build()
        )

        val errors = presenter.syncErrors()

        assertTrue(errors.size == 3)
        assertTrue(errors[0].errorCode == "500")
        assertTrue(errors[1].errorCode == "FK")
        assertTrue(errors[2].errorCode == "API")
    }

    @Test
    fun `should block sms for some conflict types`() {
        whenever(
            smsSyncProvider.isSMSEnabled(any())
        ) doReturn true
        val result = SyncStatusDialog.ConflictType.values().associate {
            val enable = GranularSyncPresenterImpl(
                d2,
                programUtils,
                trampolineSchedulerProvider,
                it,
                "test_uid",
                null,
                null,
                null,
                workManager,
                errorMapper,
                preferenceProvider,
                smsSyncProvider
            ).isSMSEnabled(true)
            it to enable
        }
        assertTrue(result[SyncStatusDialog.ConflictType.PROGRAM] == false)
        assertTrue(result[SyncStatusDialog.ConflictType.ALL] == false)
        assertTrue(result[SyncStatusDialog.ConflictType.DATA_SET] == false)
        assertTrue(result[SyncStatusDialog.ConflictType.TEI] == true)
        assertTrue(result[SyncStatusDialog.ConflictType.EVENT] == true)
        assertTrue(result[SyncStatusDialog.ConflictType.DATA_VALUES] == true)
    }

    @Test
    fun shouldSmsSyncUsingPlayServices() {
        val presenter = GranularSyncPresenterImpl(
            d2,
            programUtils,
            trampolineSchedulerProvider,
            SyncStatusDialog.ConflictType.PROGRAM,
            "test_uid",
            null,
            null,
            null,
            workManager,
            errorMapper,
            preferenceProvider,
            smsSyncProvider
        )

        val testingMsg = "testingMsg"
        val testingGateway = "testingGateWay"
        whenever(smsSyncProvider.isPlayServicesEnabled()) doReturn true
        whenever(smsSyncProvider.getConvertTask()) doReturn Single.just(
            ConvertTaskResult.Message(testingMsg)
        )
        whenever(smsSyncProvider.getGatewayNumber()) doReturn testingGateway
        presenter.configure(view)
        presenter.onSmsSyncClick { }

        verify(view, times(1)).logOpeningSmsApp()
        verify(smsSyncProvider, times(1)).getConvertTask()
        verify(view, times(1)).openSmsApp(testingMsg, testingGateway)
    }

    @Test
    fun shouldUnregisterReceiverIfSmsNotSent() {
        val presenter = GranularSyncPresenterImpl(
            d2,
            programUtils,
            trampolineSchedulerProvider,
            SyncStatusDialog.ConflictType.PROGRAM,
            "test_uid",
            null,
            null,
            null,
            workManager,
            errorMapper,
            preferenceProvider,
            smsSyncProvider
        )

        presenter.configure(view)
        presenter.onSmsNotManuallySent(context)
        verify(view).logSmsNotSent()
        verify(smsSyncProvider).unregisterSMSReceiver(context)
    }

    @Test
    fun shouldInitDefaultSmsSync() {
        val presenter = GranularSyncPresenterImpl(
            d2,
            programUtils,
            trampolineSchedulerProvider,
            SyncStatusDialog.ConflictType.PROGRAM,
            "test_uid",
            null,
            null,
            null,
            workManager,
            errorMapper,
            preferenceProvider,
            smsSyncProvider
        )

        whenever(smsSyncProvider.isPlayServicesEnabled()) doReturn false
        whenever(view.checkSmsPermission()) doReturn true
        whenever(
            smsSyncProvider.getConvertTask()
        ) doReturn Single.just(ConvertTaskResult.Count(1))
        whenever(smsSyncProvider.smsSender) doReturn mock()
        whenever(smsSyncProvider.smsSender.submissionId) doReturn 1
        var testingState: LiveData<List<SmsSendingService.SendingStatus>> =
            MutableLiveData(emptyList())
        presenter.configure(view)

        presenter.onSmsSyncClick {
            testingState = it
        }

        assertTrue(testingState.value?.isNotEmpty() == true)
        assertTrue(
            testingState.value?.get(0)?.state == SmsSendingService.State.WAITING_COUNT_CONFIRMATION
        )
    }

    @Test
    fun shouldSetSmsSent() {
        val presenter = GranularSyncPresenterImpl(
            d2,
            programUtils,
            trampolineSchedulerProvider,
            SyncStatusDialog.ConflictType.PROGRAM,
            "test_uid",
            null,
            null,
            null,
            workManager,
            errorMapper,
            preferenceProvider,
            smsSyncProvider
        )

        whenever(smsSyncProvider.expectsResponseSMS()) doReturn false
        whenever(smsSyncProvider.smsSender) doReturn mock()
        whenever(smsSyncProvider.smsSender.markAsSentViaSMS()) doReturn Completable.complete()
        presenter.configure(view)
        presenter.onSmsManuallySent(context) {
        }

        verify(view).logSmsSent()
        verify(view).updateState(State.SENT_VIA_SMS)
    }

    @Test
    fun shouldWaitForSMSResponse() {
        val presenter = GranularSyncPresenterImpl(
            d2,
            programUtils,
            trampolineSchedulerProvider,
            SyncStatusDialog.ConflictType.PROGRAM,
            "test_uid",
            null,
            null,
            null,
            workManager,
            errorMapper,
            preferenceProvider,
            smsSyncProvider
        )

        whenever(smsSyncProvider.expectsResponseSMS()) doReturn true
        presenter.configure(view)
        presenter.onSmsManuallySent(context) {
        }

        verify(view).logWaitingForServerResponse()
    }

    @Test
    fun shouldConfirmSmsReceived() {
        val presenter = GranularSyncPresenterImpl(
            d2,
            programUtils,
            trampolineSchedulerProvider,
            SyncStatusDialog.ConflictType.PROGRAM,
            "test_uid",
            null,
            null,
            null,
            workManager,
            errorMapper,
            preferenceProvider,
            smsSyncProvider
        )

        whenever(smsSyncProvider.smsSender) doReturn mock()
        whenever(smsSyncProvider.smsSender.markAsSentViaSMS()) doReturn mock()

        presenter.configure(view)
        presenter.onConfirmationMessageStateChanged(true)

        verify(view).logSmsReachedServer()
    }

    @Test
    fun shouldInformSmsWasNotReceived() {
        val presenter = GranularSyncPresenterImpl(
            d2,
            programUtils,
            trampolineSchedulerProvider,
            SyncStatusDialog.ConflictType.PROGRAM,
            "test_uid",
            null,
            null,
            null,
            workManager,
            errorMapper,
            preferenceProvider,
            smsSyncProvider
        )

        whenever(smsSyncProvider.smsSender) doReturn mock()
        whenever(smsSyncProvider.smsSender.markAsSentViaSMS()) doReturn mock()

        presenter.configure(view)
        presenter.onConfirmationMessageStateChanged(false)

        verify(view).logSmsReachedServerError()
    }

    private fun getMockedCompleteRegistrations(
        testingState: State
    ): MutableList<DataSetCompleteRegistration> {
        return arrayListOf(
            DataSetCompleteRegistration.builder()
                .attributeOptionCombo("attr_opt_comb")
                .dataSet("data_set_uid")
                .date(Date())
                .period("periodId")
                .organisationUnit("org_unit")
                .state(testingState)
                .build(),
            DataSetCompleteRegistration.builder()
                .attributeOptionCombo("attr_opt_comb")
                .dataSet("data_set_uid")
                .date(Date())
                .period("periodId")
                .organisationUnit("org_unit")
                .state(State.SYNCED)
                .build()
        )
    }

    private fun getListOfTEIsWithError(): MutableList<TrackedEntityInstance> {
        return mutableListOf(
            TrackedEntityInstance.builder()
                .uid("tei_uid")
                .organisationUnit("org_unit")
                .trackedEntityType("te_type")
                .state(State.ERROR)
                .build()
        )
    }

    private fun getProgram(): Program {
        return Program.builder()
            .id(1L)
            .version(1)
            .onlyEnrollOnce(true)
            .enrollmentDateLabel("enrollment_date_label")
            .displayIncidentDate(false)
            .incidentDateLabel("incident_date_label")
            .registration(true)
            .selectEnrollmentDatesInFuture(true)
            .dataEntryMethod(false)
            .ignoreOverdueEvents(false)
            .selectIncidentDatesInFuture(true)
            .useFirstStageDuringRegistration(true)
            .displayFrontPageList(false)
            .programType(ProgramType.WITH_REGISTRATION)
            .relatedProgram(ObjectWithUid.create("program_uid"))
            .trackedEntityType(TrackedEntityType.builder().uid("tracked_entity_type").build())
            .categoryCombo(ObjectWithUid.create("category_combo_uid"))
            .access(Access.create(true, true, DataAccess.create(true, true)))
            .expiryDays(2)
            .completeEventsExpiryDays(3)
            .minAttributesRequiredToSearch(1)
            .maxTeiCountToReturn(2)
            .featureType(FeatureType.POINT)
            .accessLevel(AccessLevel.PROTECTED)
            .shortName("SHORT_NAME")
            .displayShortName("DISPLAY_SHORT_NAME")
            .description("DESCRIPTION")
            .displayDescription("DISPLAY_DESCRIPTION")
            .uid("test_uid")
            .code("CODE")
            .name("NAME")
            .displayName("DISPLAY_NAME_FIRST")
            .created(Date())
            .lastUpdated(Date())
            .build()
    }
}
