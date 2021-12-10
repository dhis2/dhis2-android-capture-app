package org.dhis2.usescases.teiDashboard

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doReturnConsecutively
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dhis2.org.analytics.charts.Charts
import io.reactivex.Single
import org.dhis2.Bindings.toDate
import org.dhis2.utils.resources.ResourceManager
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.common.Unit
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.hisp.dhis.android.core.maintenance.D2ErrorComponent
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito

class DashboardRepositoryImplTest {

    private lateinit var repository: DashboardRepositoryImpl
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val resources: ResourceManager = mock()
    private val charts: Charts = mock()

    @Before
    fun setUp() {
        repository = DashboardRepositoryImpl(
            d2,
            charts,
            "teiUid",
            "programUid",
            "enrollmentUid",
            resources
        )
    }

    @Test
    fun `Should return program stage to show display generate event`() {
        whenever(d2.eventModule()) doReturn mock()
        whenever(d2.eventModule().events()) doReturn mock()
        whenever(d2.eventModule().events().uid("event_uid")) doReturn mock()
        whenever(d2.eventModule().events().uid("event_uid").get()) doReturn
            Single.just(getMockSingleEvent())

        whenever(d2.programModule()) doReturn mock()
        whenever(d2.programModule().programStages()) doReturn mock()
        whenever(d2.programModule().programStages().uid("program_stage")) doReturn mock()
        whenever(d2.programModule().programStages().uid("program_stage").get()) doReturn
            Single.just(getMockStage())

        val testObserver = repository.displayGenerateEvent("event_uid").test()

        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)
        testObserver.assertValue(getMockStage())

        testObserver.dispose()
    }

    @Test
    fun `Should return only enrollments that are not deleted`() {
        val teiUid = "teiUid"

        val enrollment1 = getMockingEnrollment().toBuilder()
            .uid("enrollment_1").deleted(true).trackedEntityInstance(teiUid)
            .build()
        val enrollment2 = getMockingEnrollment().toBuilder()
            .deleted(false).trackedEntityInstance(teiUid)
            .build()

        val enrollments = listOf(enrollment1, enrollment2)

        whenever(d2.enrollmentModule().enrollments().byTrackedEntityInstance()) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments().byTrackedEntityInstance().eq(teiUid)
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments()
                .byTrackedEntityInstance().eq(teiUid)
                .byDeleted()
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments()
                .byTrackedEntityInstance().eq(teiUid)
                .byDeleted().eq(false)
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments()
                .byTrackedEntityInstance().eq(teiUid)
                .byDeleted().eq(false).get()
        ) doReturn Single.just(enrollments)

        val testObserver = repository.getTEIEnrollments(teiUid).test()

        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)
    }

    @Test
    fun `Should get enrollment status`() {
        whenever(d2.enrollmentModule().enrollments()) doReturn mock()
        whenever(d2.enrollmentModule().enrollments().uid("uid")) doReturn mock()
        whenever(d2.enrollmentModule().enrollments().uid("uid").blockingGet()) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments().uid("uid").blockingGet().status()
        ) doReturn EnrollmentStatus.COMPLETED

        val status = repository.getEnrollmentStatus("uid")

        assert(status == EnrollmentStatus.COMPLETED)
    }

    @Test
    fun `Should return false if updating status of enrollment returns a D2Error`() {
        whenever(d2.programModule().programs()) doReturn mock()
        whenever(d2.programModule().programs().uid("programUid")) doReturn mock()
        whenever(d2.programModule().programs().uid("programUid").blockingGet()) doReturn mock()
        whenever(
            d2.programModule().programs().uid("programUid").blockingGet().access()
        ) doReturn mock()
        whenever(
            d2.programModule().programs().uid("programUid").blockingGet().access().data()
        ) doReturn mock()
        whenever(
            d2.programModule().programs().uid("programUid").blockingGet().access().data().write()
        ) doReturn true
        whenever(d2.enrollmentModule().enrollments()) doReturn mock()
        whenever(d2.enrollmentModule().enrollments().uid("enrollmentUid")) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments()
                .uid("enrollmentUid").setStatus(EnrollmentStatus.COMPLETED)
        ) doThrow D2Error.builder().errorCode(D2ErrorCode.VALUE_CANT_BE_SET)
            .errorComponent(D2ErrorComponent.Database)
            .errorDescription("description")
            .build()

        val testObserver =
            repository.updateEnrollmentStatus("enrollmentUid", EnrollmentStatus.COMPLETED).test()

        testObserver.assertNoErrors()
        testObserver.assertValueAt(0) {
            it == StatusChangeResultCode.FAILED
        }
    }

    @Test
    fun `Should return true if enrollment status was updated correctly`() {
        whenever(d2.programModule().programs()) doReturn mock()
        whenever(d2.programModule().programs().uid("programUid")) doReturn mock()
        whenever(d2.programModule().programs().uid("programUid").blockingGet()) doReturn mock()
        whenever(
            d2.programModule().programs().uid("programUid").blockingGet().access()
        ) doReturn mock()
        whenever(
            d2.programModule().programs().uid("programUid").blockingGet().access().data()
        ) doReturn mock()
        whenever(
            d2.programModule().programs().uid("programUid").blockingGet().access().data().write()
        ) doReturn true
        whenever(d2.enrollmentModule().enrollments()) doReturn mock()
        whenever(d2.enrollmentModule().enrollments().uid("enrollmentUid")) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments()
                .uid("enrollmentUid").setStatus(EnrollmentStatus.COMPLETED)
        ) doReturn Unit()

        val testObserver =
            repository.updateEnrollmentStatus("enrollmentUid", EnrollmentStatus.COMPLETED).test()

        testObserver.assertNoErrors()
        testObserver.assertValueAt(0) {
            it == StatusChangeResultCode.CHANGED
        }
    }

    @Test
    fun `Should return false if user does not hava write permission to update enrollment status`() {
        whenever(d2.programModule().programs()) doReturn mock()
        whenever(d2.programModule().programs().uid("programUid")) doReturn mock()
        whenever(d2.programModule().programs().uid("programUid").blockingGet()) doReturn mock()
        whenever(
            d2.programModule().programs().uid("programUid").blockingGet().access()
        ) doReturn mock()
        whenever(
            d2.programModule().programs().uid("programUid").blockingGet().access().data()
        ) doReturn mock()
        whenever(
            d2.programModule().programs().uid("programUid").blockingGet().access().data().write()
        ) doReturn false

        val testObserver =
            repository.updateEnrollmentStatus("enrollmentUid", EnrollmentStatus.COMPLETED).test()

        testObserver.assertNoErrors()
        testObserver.assertValueAt(0) {
            it == StatusChangeResultCode.WRITE_PERMISSION_FAIL
        }
    }

    @Test
    fun `Should get program attributes if program is not null`() {
        val programUid = "programUid"
        val teiUid = "teiUid"
        val expectedResults = arrayListOf<String>("1", "", "3")

        whenever(
            d2.programModule().programTrackedEntityAttributes()
        ) doReturn mock()

        whenever(
            d2.programModule().programTrackedEntityAttributes()
                .byDisplayInList()
        ) doReturn mock()

        whenever(
            d2.programModule().programTrackedEntityAttributes()
                .byDisplayInList().isTrue
        ) doReturn mock()

        whenever(
            d2.programModule().programTrackedEntityAttributes()
                .byDisplayInList().isTrue
                .byProgram()
        ) doReturn mock()

        whenever(
            d2.programModule().programTrackedEntityAttributes()
                .byDisplayInList().isTrue
                .byProgram().eq(programUid)
        ) doReturn mock()

        whenever(
            d2.programModule().programTrackedEntityAttributes()
                .byDisplayInList().isTrue
                .byProgram().eq(programUid)
                .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
        ) doReturn mock()

        whenever(
            d2.programModule().programTrackedEntityAttributes()
                .byDisplayInList().isTrue
                .byProgram().eq(programUid)
                .orderBySortOrder(RepositoryScope.OrderByDirection.ASC).get()
        ) doReturn Single.just(programAttributeValues())

        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
        ) doReturn mock()

        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues().value(anyString(), anyString())
        ) doReturn mock()

        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues().value(anyString(), anyString())
                .blockingExists()
        ) doReturnConsecutively arrayListOf(true, false, true)

        whenever(
            d2.trackedEntityModule()
                .trackedEntityAttributeValues().value(anyString(), anyString())
                .blockingGet()
        ) doReturnConsecutively arrayListOf(
            TrackedEntityAttributeValue.builder()
                .trackedEntityAttribute("attr1")
                .trackedEntityInstance(teiUid)
                .value("1")
                .build(),
            TrackedEntityAttributeValue.builder()
                .trackedEntityAttribute("attr3")
                .trackedEntityInstance(teiUid)
                .value("3")
                .build()
        )

        whenever(
            d2.trackedEntityModule().trackedEntityAttributes().uid(anyString()).blockingGet()
        ) doReturnConsecutively arrayListOf(
            TrackedEntityAttribute.builder()
                .uid("attr1")
                .valueType(ValueType.TEXT)
                .build(),
            TrackedEntityAttribute.builder()
                .uid("attr3")
                .valueType(ValueType.TEXT)
                .build()
        )

        val testObserver = repository.getTEIAttributeValues(programUid, teiUid).test()
        testObserver
            .assertNoErrors()
            .assertValue {
                it[0].value() == expectedResults[0] &&
                    it[1].value() == expectedResults[1] &&
                    it[2].value() == expectedResults[2]
            }
    }

    private fun programAttributeValues(): List<ProgramTrackedEntityAttribute> {
        return arrayListOf(
            ProgramTrackedEntityAttribute.builder()
                .uid("programAttr1")
                .trackedEntityAttribute(ObjectWithUid.create(("attr1")))
                .build(),
            ProgramTrackedEntityAttribute.builder()
                .uid("programAttr2")
                .trackedEntityAttribute(ObjectWithUid.create(("attr2")))
                .build(),
            ProgramTrackedEntityAttribute.builder()
                .uid("programAttr3")
                .trackedEntityAttribute(ObjectWithUid.create(("attr3")))
                .build()
        )
    }

    private fun getMockingProgram(): Program {
        return Program.builder()
            .uid("programUid")
            .ignoreOverdueEvents(true)
            .build()
    }

    private fun getMockingEventList(): MutableList<Event> {
        return arrayListOf(
            Event.builder()
                .uid("event_uid_1")
                .programStage("program_stage")
                .program("program")
                .enrollment("enrollmentUid")
                .status(EventStatus.ACTIVE)
                .eventDate("2019-06-01".toDate())
                .build(),
            Event.builder()
                .uid("event_uid_2")
                .programStage("program_stage")
                .program("program")
                .enrollment("enrollmentUid")
                .status(EventStatus.ACTIVE)
                .eventDate("2019-06-05".toDate())
                .build(),
            Event.builder()
                .uid("event_uid_3")
                .programStage("program_stage")
                .program("program")
                .enrollment("enrollmentUid")
                .status(EventStatus.SCHEDULE)
                .dueDate("2019-06-02".toDate())
                .build(),
            Event.builder()
                .uid("event_uid_4")
                .programStage("program_stage")
                .program("program")
                .enrollment("enrollmentUid")
                .status(EventStatus.ACTIVE)
                .eventDate("2019-06-10".toDate())
                .build()
        )
    }

    private fun getMockingEnrollment(): Enrollment {
        return Enrollment.builder()
            .uid("enrollmentUid")
            .build()
    }

    private fun getMockSingleEvent(): Event {
        return Event.builder()
            .uid("event_uid")
            .programStage("program_stage")
            .program("program")
            .build()
    }

    private fun getMockStage(): ProgramStage {
        return ProgramStage.builder()
            .uid("program_stage")
            .build()
    }
}
