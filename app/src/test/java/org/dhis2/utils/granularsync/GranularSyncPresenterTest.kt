package org.dhis2.utils.granularsync

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import java.util.Collections
import java.util.Date
import junit.framework.Assert.assertTrue
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.data.service.workManager.WorkManagerController
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.`object`.ReadOnlyOneObjectRepositoryFinalImpl
import org.hisp.dhis.android.core.common.Access
import org.hisp.dhis.android.core.common.DataAccess
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.dataset.DataSetCompleteRegistration
import org.hisp.dhis.android.core.program.AccessLevel
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramCollectionRepository
import org.hisp.dhis.android.core.program.ProgramModule
import org.hisp.dhis.android.core.program.ProgramType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType
import org.junit.Test
import org.mockito.BDDMockito.then
import org.mockito.Mockito
import org.mockito.Mockito.mock

class GranularSyncPresenterTest {

    private val d2: D2 = mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val view = mock(GranularSyncContracts.View::class.java)
    private val trampolineSchedulerProvider = TrampolineSchedulerProvider()
    private val workManager = mock(WorkManagerController::class.java)
    private val programRepoMock = mock(ReadOnlyOneObjectRepositoryFinalImpl::class.java)

    private val testProgram = getProgram()

    @Test
    fun simplePresenterTest() {
        // GIVEN
        val presenter = GranularSyncPresenterImpl(
            d2,
            trampolineSchedulerProvider,
            SyncStatusDialog.ConflictType.PROGRAM,
            "test_uid",
            null,
            null,
            null,
            workManager
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
            trampolineSchedulerProvider,
            SyncStatusDialog.ConflictType.PROGRAM,
            "test_uid",
            null,
            null,
            null,
            workManager
        )

        whenever(d2.programModule()) doReturn mock()
        whenever(d2.programModule().programs()) doReturn mock()
        whenever(d2.programModule().programs().uid("test_uid")) doReturn mock()
        whenever(d2.programModule().programs().uid("test_uid").get()) doReturn Single.just(
            getProgram()
        )

        whenever(d2.trackedEntityModule()) doReturn mock()
        whenever(d2.trackedEntityModule().trackedEntityInstances()) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstances().byProgramUids(
                Collections.singletonList("test_uid")
            )
        ) doReturn mock()

        whenever(
            d2.trackedEntityModule().trackedEntityInstances().byProgramUids(
                Collections.singletonList("test_uid")
            ).byState()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstances().byProgramUids(
                Collections.singletonList("test_uid")
            ).byState().`in`(State.ERROR)
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstances().byProgramUids(
                Collections.singletonList("test_uid")
            ).byState().`in`(State.ERROR).blockingGet()
        ) doReturn getListOfTEIsWithError()
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
            trampolineSchedulerProvider,
            SyncStatusDialog.ConflictType.DATA_SET,
            "data_set_uid",
            null,
            null,
            null,
            workManager
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
            trampolineSchedulerProvider,
            SyncStatusDialog.ConflictType.DATA_SET,
            "data_set_uid",
            null,
            null,
            null,
            workManager
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
            trampolineSchedulerProvider,
            SyncStatusDialog.ConflictType.DATA_SET,
            "data_set_uid",
            null,
            null,
            null,
            workManager
        )

        val state = presenter.getStateFromCanditates(arrayListOf(State.TO_POST))

        assertTrue(state == State.TO_UPDATE)
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
            .access(Access.create(null, null, DataAccess.create(true, true)))
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
