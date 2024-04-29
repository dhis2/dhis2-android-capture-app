package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.data

import org.dhis2.form.ui.FieldViewModelFactory
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope.OrderByDirection.DESC
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.program.ProgramStage
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

class EventDetailsRepositoryTest {

    private val date: Date = mock()
    private val event: Event = mock {
        on { eventDate() } doReturn date
        on { dueDate() } doReturn date
    }
    private val programStage: ProgramStage = mock()
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)

    private val fieldViewModelFactory: FieldViewModelFactory = mock()
    private lateinit var repository: EventDetailsRepository

    @Before
    fun setup() {
        repository = EventDetailsRepository(
            d2,
            PROGRAM_UID,
            EVENT_UID,
            PROGRAM_STAGE_UID,
            fieldViewModelFactory,
        ) { d2Error -> "" }

        whenever(
            d2.programModule().programStages().uid(PROGRAM_STAGE_UID).blockingGet(),
        ) doReturn programStage

        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().eq(ENROLLMENT_UID),
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().eq(ENROLLMENT_UID)
                .byProgramStageUid(),
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().eq(ENROLLMENT_UID)
                .byProgramStageUid().eq(PROGRAM_STAGE_UID),
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().eq(ENROLLMENT_UID)
                .byProgramStageUid().eq(PROGRAM_STAGE_UID)
                .byDeleted(),
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().eq(ENROLLMENT_UID)
                .byProgramStageUid().eq(PROGRAM_STAGE_UID)
                .byDeleted().isFalse,
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().eq(ENROLLMENT_UID)
                .byProgramStageUid().eq(PROGRAM_STAGE_UID)
                .byDeleted().isFalse
                .orderByEventDate(DESC),
        ) doReturn mock()

        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().eq(ENROLLMENT_UID)
                .byProgramStageUid().eq(PROGRAM_STAGE_UID)
                .byDeleted().isFalse
                .orderByDueDate(DESC),
        ) doReturn mock()
    }

    @Test
    fun `should getMinDaysFromStartByProgramStage`() {
        // Given a program stage with min days from start
        whenever(programStage.minDaysFromStart()) doReturn 1

        // When client is asking min days from start
        // Then is returning days from program stage
        assertEquals(repository.getMinDaysFromStartByProgramStage(), 1)
    }

    @Test
    fun `should not getMinDaysFromStartByProgramStage`() {
        // Given a program stage without min days from start
        whenever(programStage.minDaysFromStart()) doReturn null

        // When client is asking min days from start
        // Then is returning days from program stage
        assertEquals(repository.getMinDaysFromStartByProgramStage(), 0)
    }

    @Test
    fun `should getStateLastDate`() {
        // Given a program stage with active events
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().eq(ENROLLMENT_UID)
                .byProgramStageUid().eq(PROGRAM_STAGE_UID)
                .byDeleted().isFalse
                .orderByEventDate(DESC)
                .blockingGet(),
        ) doReturn listOf(event)
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().eq(ENROLLMENT_UID)
                .byProgramStageUid().eq(PROGRAM_STAGE_UID)
                .byDeleted().isFalse
                .orderByDueDate(DESC)
                .blockingGet(),
        ) doReturn emptyList()

        // When client is asking for getStageLastDate
        // Then gets the date of the active event
        assertEquals(repository.getStageLastDate(ENROLLMENT_UID), date)
    }

    @Test
    fun `should getStateLastDate for scheduled event`() {
        // Given a program stage with active events
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().eq(ENROLLMENT_UID)
                .byProgramStageUid().eq(PROGRAM_STAGE_UID)
                .byDeleted().isFalse
                .orderByEventDate(DESC)
                .blockingGet(),
        ) doReturn emptyList()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().eq(ENROLLMENT_UID)
                .byProgramStageUid().eq(PROGRAM_STAGE_UID)
                .byDeleted().isFalse
                .orderByDueDate(DESC)
                .blockingGet(),
        ) doReturn listOf(event)

        // When client is asking for getStageLastDate
        // Then gets the date of the active event
        assertEquals(repository.getStageLastDate(ENROLLMENT_UID), date)
    }

    companion object {
        const val PROGRAM_UID = "programUid"
        const val EVENT_UID = "eventUid"
        const val PROGRAM_STAGE_UID = "programStageUid"
        const val ENROLLMENT_UID = "enrollmentUid"
    }
}
