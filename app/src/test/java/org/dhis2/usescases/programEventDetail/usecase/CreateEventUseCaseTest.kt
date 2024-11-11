package org.dhis2.usescases.programEventDetail.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.tracker.events.CreateEventUseCase
import org.dhis2.tracker.events.CreateEventUseCaseRepository
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.data.EventDetailsRepositoryTest.Companion.PROGRAM_STAGE_UID
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.filters.internal.StringFilterConnector
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.event.EventCollectionRepository
import org.hisp.dhis.android.core.event.EventCreateProjection
import org.hisp.dhis.android.core.event.EventModule
import org.hisp.dhis.android.core.event.EventObjectRepository
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.hisp.dhis.android.core.program.ProgramStageCollectionRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreateEventUseCaseTest {

    private val dispatcherProvider: DispatcherProvider = mock {
        on { io() } doReturn Dispatchers.Unconfined
    }

    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)

    private val eventRepository: EventObjectRepository = mock()

    private val programStageRepository: ProgramStageCollectionRepository = mock()

    private val eventCollectionRepository: EventCollectionRepository = mock()

    private val stringFilterConnector: StringFilterConnector<EventCollectionRepository> = mock()

    val eventModule: EventModule = mock {
        on { events() } doReturn mock()
        on { events().uid(EVENT_ID) } doReturn eventRepository
        on { events().uid(EVENT_ID) } doReturn eventRepository
    }

    private val dateUtils: DateUtils = mock {
        on { today } doReturn Date()
    }

    private lateinit var repository: CreateEventUseCaseRepository

    private lateinit var createEventUseCase: CreateEventUseCase

    @Before
    fun setUp() {
        repository = CreateEventUseCaseRepository(d2, dateUtils)
        createEventUseCase = CreateEventUseCase(dispatcherProvider, repository)
        mockD2Resources()
    }

    @Test
    fun `create event with enrollment`() {
        var result: Result<String>

        runBlocking {
            result = createEventUseCase(PROGRAM_ID, ORG_UNIT_ID, PROGRAM_STAGE_ID, ENROLLMENT_ID)
            assertEquals(Result.success(EVENT_ID), result)
        }
        verify(eventRepository).setEventDate(any<Date>())
    }

    @Test
    fun `create event without enrollment`() {
        var result: Result<String>

        runBlocking {
            result = createEventUseCase(PROGRAM_ID, ORG_UNIT_ID, PROGRAM_STAGE_ID, null)
            assertEquals(Result.success(EVENT_ID), result)
        }

        verify(eventRepository).setEventDate(any<Date>())
    }

    @Test
    fun `create event with error`() {
        val error = D2Error.builder()
            .errorCode(D2ErrorCode.UNEXPECTED)
            .errorDescription("Error creating the event").build()

        whenever(
            d2.eventModule().events().blockingAdd(any<EventCreateProjection>()),
        ) doThrow (error)

        runBlocking {
            val result = createEventUseCase(PROGRAM_ID, ORG_UNIT_ID, PROGRAM_STAGE_ID, ENROLLMENT_ID)
            assertEquals(error, result.exceptionOrNull())
        }
    }

    @Test
    fun `create event based on incident date`() {
        val incidentDateString = "01/11/2024"
        val enrollmentDateString = "10/10/2024"
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val incidentDate = dateFormat.parse(incidentDateString)

        val enrollmentDate = dateFormat.parse(enrollmentDateString)

        whenever(
            d2.enrollmentModule(),
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments(),
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments().uid(ENROLLMENT_ID),
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments().uid(ENROLLMENT_ID).blockingGet(),
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments().uid(ENROLLMENT_ID).blockingGet()?.incidentDate(),
        ) doReturn incidentDate

        whenever(
            d2.enrollmentModule().enrollments().byUid(),
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments().byUid().eq(ENROLLMENT_ID),
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments().byUid().eq(ENROLLMENT_ID).blockingGet(),
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments().byUid().eq(ENROLLMENT_ID).blockingGet().first(),
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments().byUid().eq(ENROLLMENT_ID).blockingGet().first().enrollmentDate(),
        ) doReturn (enrollmentDate)

        runBlocking {
            val result = createEventUseCase(PROGRAM_ID, ORG_UNIT_ID, PROGRAM_STAGE_ID, ENROLLMENT_ID)
        }

        verify(eventRepository).setEventDate(incidentDate)
    }

    @Test
    fun `create event based on enrollment date if no incident Date`() {
        val enrollmentDateString = "10/10/2024"
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val incidentDate = null

        val enrollmentDate = dateFormat.parse(enrollmentDateString)

        whenever(
            d2.enrollmentModule(),
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments(),
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments().uid(ENROLLMENT_ID),
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments().uid(ENROLLMENT_ID).blockingGet(),
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments().uid(ENROLLMENT_ID).blockingGet()?.incidentDate(),
        ) doReturn incidentDate

        whenever(
            d2.enrollmentModule().enrollments().byUid(),
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments().byUid().eq(ENROLLMENT_ID),
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments().byUid().eq(ENROLLMENT_ID).blockingGet(),
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments().byUid().eq(ENROLLMENT_ID).blockingGet().first(),
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments().byUid().eq(ENROLLMENT_ID).blockingGet().first().enrollmentDate(),
        ) doReturn (enrollmentDate)

        runBlocking {
            val result = createEventUseCase(PROGRAM_ID, ORG_UNIT_ID, PROGRAM_STAGE_ID, ENROLLMENT_ID)
        }

        verify(eventRepository).setEventDate(enrollmentDate)
    }

    companion object {
        const val PROGRAM_STAGE_ID = "programStageId"
        const val ENROLLMENT_ID = "enrollmentId"
        const val PROGRAM_ID = "programId"
        const val EVENT_ID = "eventId"
        const val ORG_UNIT_ID = "orgUnitId"
    }

    private fun mockD2Resources() {
        whenever(
            d2.eventModule().events().blockingAdd(any<EventCreateProjection>()),
        ) doReturn EVENT_ID

        whenever(
            d2.eventModule().events().uid(EVENT_ID),
        ) doReturn eventRepository

        whenever(
            d2.eventModule().events().byEnrollmentUid()
                .eq(ENROLLMENT_ID),
        ) doReturn eventCollectionRepository

        whenever(
            eventCollectionRepository.byProgramStageUid(),
        ) doReturn stringFilterConnector

        whenever(
            stringFilterConnector.eq(any()),
        ) doReturn eventCollectionRepository

        whenever(
            d2.eventModule().events().byEnrollmentUid()
                .eq(ENROLLMENT_ID).byProgramStageUid().eq(PROGRAM_STAGE_UID).byDeleted(),
        ) doReturn mock()

        whenever(
            d2.eventModule().events().byEnrollmentUid()
                .eq(ENROLLMENT_ID).byProgramStageUid().eq(PROGRAM_STAGE_UID).byDeleted().isFalse,
        ) doReturn mock()
        whenever(
            d2.eventModule().events().byEnrollmentUid()
                .eq(ENROLLMENT_ID).byProgramStageUid().eq(PROGRAM_STAGE_UID).byDeleted().isFalse
                .orderByEventDate(RepositoryScope.OrderByDirection.DESC),
        ) doReturn mock()
        whenever(
            d2.eventModule().events().byEnrollmentUid()
                .eq(ENROLLMENT_ID).byProgramStageUid().eq(PROGRAM_STAGE_UID).byDeleted().isFalse
                .orderByDueDate(RepositoryScope.OrderByDirection.DESC),
        ) doReturn mock()

        whenever(
            d2.eventModule().events().uid(EVENT_ID),
        ) doReturn eventRepository

        whenever(
            d2.eventModule().events().byEnrollmentUid()
                .eq(null),
        ) doReturn eventCollectionRepository
        whenever(
            d2.eventModule().events().byEnrollmentUid()
                .eq(null).byProgramStageUid().eq(PROGRAM_STAGE_UID).byDeleted(),
        ) doReturn mock()

        whenever(
            d2.eventModule().events().byEnrollmentUid()
                .eq(null).byProgramStageUid().eq(PROGRAM_STAGE_UID).byDeleted().isFalse,
        ) doReturn mock()
        whenever(
            d2.eventModule().events().byEnrollmentUid()
                .eq(null).byProgramStageUid().eq(PROGRAM_STAGE_UID).byDeleted().isFalse
                .orderByEventDate(RepositoryScope.OrderByDirection.DESC),
        ) doReturn mock()
        whenever(
            d2.eventModule().events().byEnrollmentUid()
                .eq(null).byProgramStageUid().eq(PROGRAM_STAGE_UID).byDeleted().isFalse
                .orderByDueDate(RepositoryScope.OrderByDirection.DESC),
        ) doReturn mock()
    }
}
