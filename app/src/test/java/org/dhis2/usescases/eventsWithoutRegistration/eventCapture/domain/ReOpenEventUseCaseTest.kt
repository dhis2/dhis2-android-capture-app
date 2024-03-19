package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.event.EventModule
import org.hisp.dhis.android.core.event.EventObjectRepository
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ReOpenEventUseCaseTest {

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

    private val reOpenEventUseCase = ReOpenEventUseCase(
        dispatcher = dispatcherProvider,
        d2 = d2,
    )

    @Test
    fun `reopen event should return success`() {
        runBlocking {
            val result = reOpenEventUseCase(eventUid)
            assertEquals(Result.success(Unit), result)
        }
    }

    @Test
    fun `reopen event should return failure`() {
        val error = D2Error.builder()
            .errorCode(D2ErrorCode.UNEXPECTED)
            .errorDescription("Error creating the event").build()

        whenever(
            eventModule.events().uid(eventUid).setStatus(EventStatus.ACTIVE),
        ) doThrow (error)

        runBlocking {
            val result = reOpenEventUseCase(eventUid)
            assertEquals(error, result.exceptionOrNull())
        }
    }
}
