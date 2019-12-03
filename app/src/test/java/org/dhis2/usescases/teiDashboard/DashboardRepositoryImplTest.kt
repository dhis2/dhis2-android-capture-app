package org.dhis2.usescases.teiDashboard

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.squareup.sqlbrite2.BriteDatabase
import io.reactivex.Single
import org.dhis2.utils.CodeGenerator
import org.dhis2.utils.DateUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class DashboardRepositoryImplTest {

    private lateinit var repository: DashboardRepositoryImpl
    private val codeGenerator: CodeGenerator = mock()
    private val britedatabase: BriteDatabase = mock()
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)

    @Before
    fun setUp() {
        repository = DashboardRepositoryImpl(codeGenerator, britedatabase, d2)
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
    fun `event list should be order from newest to oldest`() {
        whenever(
            d2.enrollmentModule().enrollments()
                .byProgram().eq("programUid")
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments()
                .byProgram().eq("programUid")
                .byTrackedEntityInstance()
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments()
                .byProgram().eq("programUid")
                .byTrackedEntityInstance().eq("teiUid")
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments()
                .byProgram().eq("programUid")
                .byTrackedEntityInstance().eq("teiUid")
                .one()
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments()
                .byProgram().eq("programUid")
                .byTrackedEntityInstance().eq("teiUid")
                .one().get()
        ) doReturn Single.just(getMockingEnrollment())
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().eq("enrollmentUid")
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().eq("enrollmentUid")
                .byDeleted()
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().eq("enrollmentUid")
                .byDeleted().isFalse
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().eq("enrollmentUid")
                .byDeleted().isFalse.get()
        ) doReturn Single.just(getMockingEventList())

        whenever(
            d2.programModule().programs()
                .uid("programUid").blockingGet()
        ) doReturn getMockingProgram()

        val testObserver = repository.getTEIEnrollmentEvents(
            "programUid",
            "teiUid"
        ).test()

        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)
        testObserver.assertValue { events ->
            events[0].uid() == "event_uid_4" &&
                events[1].uid() == "event_uid_2" &&
                events[2].uid() == "event_uid_3" &&
                events[3].uid() == "event_uid_1"
        }
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
                .eventDate(DateUtils.uiDateFormat().parse("2019-06-01"))
                .build(),
            Event.builder()
                .uid("event_uid_2")
                .programStage("program_stage")
                .program("program")
                .enrollment("enrollmentUid")
                .status(EventStatus.ACTIVE)
                .eventDate(DateUtils.uiDateFormat().parse("2019-06-05"))
                .build(),
            Event.builder()
                .uid("event_uid_3")
                .programStage("program_stage")
                .program("program")
                .enrollment("enrollmentUid")
                .status(EventStatus.SCHEDULE)
                .dueDate(DateUtils.uiDateFormat().parse("2019-06-02"))
                .build(),
            Event.builder()
                .uid("event_uid_4")
                .programStage("program_stage")
                .program("program")
                .enrollment("enrollmentUid")
                .status(EventStatus.ACTIVE)
                .eventDate(DateUtils.uiDateFormat().parse("2019-06-10"))
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
