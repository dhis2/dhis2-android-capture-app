package org.dhis2.usescases.programEventDetail.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.tracker.events.CreateEventUseCase
import org.dhis2.tracker.events.CreateEventUseCaseRepository
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.data.EventDetailsRepositoryTest.Companion.ENROLLMENT_UID
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.data.EventDetailsRepositoryTest.Companion.PROGRAM_STAGE_UID
import org.dhis2.tracker.events.CreateEventUseCase
import org.dhis2.tracker.events.CreateEventUseCaseRepository
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.data.EventDetailsRepositoryTest.Companion.PROGRAM_STAGE_UID
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope.OrderByDirection.DESC
import org.hisp.dhis.android.core.event.EventCollectionRepository
import org.hisp.dhis.android.core.arch.repositories.filters.internal.StringFilterConnector
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.event.EventCollectionRepository
import org.hisp.dhis.android.core.event.EventCreateProjection
import org.hisp.dhis.android.core.event.EventModule
import org.hisp.dhis.android.core.event.EventObjectRepository
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.hisp.dhis.android.core.period.PeriodType
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
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CreateEventUseCaseTest {

    private val dispatcherProvider: DispatcherProvider = mock {
        on { io() } doReturn Dispatchers.Unconfined
    }

    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)

    private val eventRepository: EventObjectRepository = mock()
    private val eventCollectionRepository: EventCollectionRepository = mock()

    private val programStageRepository: ProgramStageCollectionRepository = mock()

    private val eventCollectionRepository: EventCollectionRepository = mock()

    private val stringFilterConnector: StringFilterConnector<EventCollectionRepository> = mock()

    val eventModule: EventModule = mock {
        on { events() } doReturn mock()
        on { events().uid(eventUid) } doReturn eventRepository
        on { events().uid(eventUid) } doReturn eventRepository
    }

    private val d2: D2 = mock {
        on { eventModule() } doReturn eventModule
    }

    private val dateUtils: DateUtils = mock {
        on { today } doReturn Date()
    }
    private val repository: CreateEventUseCaseRepository = CreateEventUseCaseRepository(
        d2 = d2,
        dateUtils = dateUtils,
    )

    private val createEventUseCase: CreateEventUseCase = CreateEventUseCase(
        dispatcher = dispatcherProvider,
        repository = repository,
    )

    @Test
    fun `create event with enrollment`() {
        whenever(
            d2.eventModule().events().blockingAdd(any<EventCreateProjection>()),
        ) doReturn eventUid

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
        whenever(
            d2.eventModule().events().byEnrollmentUid().eq(ENROLLMENT_ID),
        ) doReturn eventCollectionRepository

        whenever(
            repository.createEvent(ENROLLMENT_ID, PROGRAM_ID, PROGRAM_STAGE_ID, ORG_UNIT_ID),
        ) doReturn Result.success(eventUid)

        runBlocking {
            val result = createEventUseCase(programUid, orgUnitUid, programStageUid, ENROLLMENT_ID)
            assertEquals(Result.success(eventUid), result)
        }

        verify(eventModule.events()).blockingAdd(
            argThat {
                this.enrollment() == ENROLLMENT_ID &&
                    this.program() == programUid &&
                    this.programStage() == programStageUid &&
                    this.organisationUnit() == orgUnitUid
            },
        )

        verify(eventRepository).setEventDate(any<Date>())
    }

    @Test
    fun `create event without enrollment`() {
        var result: Result<String>

        runBlocking {
            val result = createEventUseCase(programUid, orgUnitUid, programStageUid, ENROLLMENT_ID)
            assertEquals(Result.success(eventUid), result)
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

    companion object {
        const val PROGRAM_STAGE_ID = "programStageId"
        const val ENROLLMENT_ID = "enrollmentId"
        const val PROGRAM_ID = "programId"

        const val ORG_UNIT_ID = "orgUnitId"
        const val DUE_DATE = "Due date"
        const val EVENT_DATE = "Event date"
        const val NEXT_EVENT = "Next event"
    }
}
