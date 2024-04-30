package org.dhis2.usescases.programEventDetail.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.event.EventCreateProjection
import org.hisp.dhis.android.core.event.EventModule
import org.hisp.dhis.android.core.event.EventObjectRepository
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

class CreateEventUseCaseTest {

    private val programUid = "programUid"
    private val orgUnitUid = "orgUnitUid"
    private val programStageUid = "programStageUid"
    private val eventUid = "eventUid"

    private val dispatcherProvider: DispatcherProvider = mock {
        on { io() } doReturn Dispatchers.Unconfined
    }

    private val eventRepository: EventObjectRepository = mock()

    val eventModule: EventModule = mock {
        on { events() } doReturn mock()
        on { events().uid(eventUid) } doReturn eventRepository
    }

    private val d2: D2 = mock {
        on { eventModule() } doReturn eventModule
    }

    private val dateUtils: DateUtils = mock {
        on { today } doReturn Date()
    }

    private val createEventUseCase: CreateEventUseCase = CreateEventUseCase(
        dispatcher = dispatcherProvider,
        d2 = d2,
        dateUtils = dateUtils,
    )

    @Test
    fun `create event with enrollment`() {
        val enrollmentUid = "enrollmentUid"

        whenever(
            d2.eventModule().events().blockingAdd(any<EventCreateProjection>()),
        ) doReturn eventUid

        runBlocking {
            val result = createEventUseCase(programUid, orgUnitUid, programStageUid, enrollmentUid)
            assertEquals(Result.success(eventUid), result)
        }

        verify(eventModule.events()).blockingAdd(
            argThat {
                this.enrollment() == enrollmentUid &&
                    this.program() == programUid &&
                    this.programStage() == programStageUid &&
                    this.organisationUnit() == orgUnitUid
            },
        )

        verify(eventRepository).setEventDate(any<Date>())
    }

    @Test
    fun `create event without enrollment`() {
        whenever(
            d2.eventModule().events().blockingAdd(any<EventCreateProjection>()),
        ) doReturn eventUid

        runBlocking {
            val result = createEventUseCase(programUid, orgUnitUid, programStageUid, null)
            assertEquals(Result.success(eventUid), result)
        }

        verify(eventModule.events()).blockingAdd(
            argThat {
                this.enrollment() == null &&
                    this.program() == programUid &&
                    this.programStage() == programStageUid &&
                    this.organisationUnit() == orgUnitUid
            },
        )

        verify(eventRepository).setEventDate(any<Date>())
    }

    @Test
    fun `create event with error`() {
        val enrollmentUid = "enrollmentUid"
        val error = D2Error.builder()
            .errorCode(D2ErrorCode.UNEXPECTED)
            .errorDescription("Error creating the event").build()

        whenever(
            d2.eventModule().events().blockingAdd(any<EventCreateProjection>()),
        ) doThrow (error)

        runBlocking {
            val result = createEventUseCase(programUid, orgUnitUid, programStageUid, enrollmentUid)
            assertEquals(error, result.exceptionOrNull())
        }
    }
}
