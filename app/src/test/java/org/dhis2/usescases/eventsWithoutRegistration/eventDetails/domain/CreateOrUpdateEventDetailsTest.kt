package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain

import java.util.Date
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.data.EventDetailsRepository
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.EventDetailResourcesProvider
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventEditableStatus.Editable
import org.hisp.dhis.android.core.event.EventEditableStatus.NonEditable
import org.hisp.dhis.android.core.event.EventNonEditableReason.NO_DATA_WRITE_ACCESS
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class CreateOrUpdateEventDetailsTest {

    private val selectedDate = Date()
    private val event: Event = mock()
    private val repository: EventDetailsRepository = mock {
        on { getEvent() } doReturn event
        on { updateEvent(selectedDate, ORG_UNIT_UID, null, null) } doReturn event
    }
    private val resourcesProvider: EventDetailResourcesProvider = mock {
        on { provideEventCreationError() } doReturn EVENT_UPDATE_ERROR
    }

    private lateinit var createOrUpdateEventDetails: CreateOrUpdateEventDetails

    @Before
    fun setUp() {
        createOrUpdateEventDetails = CreateOrUpdateEventDetails(repository, resourcesProvider)
    }

    @Test
    fun `should successfully edit event`() = runBlocking {
        whenever(repository.getEditableStatus()) doReturn Editable()

        // When trying to update an existing event
        val result = createOrUpdateEventDetails.invoke(
            selectedDate,
            ORG_UNIT_UID,
            null,
            null
        ).first()

        // Then event should have been updated
        assertTrue(result.isSuccess)
    }

    @Test
    fun `Should not update event status on ReadOnly event`() = runBlocking {
        whenever(repository.getEditableStatus()) doReturn NonEditable(NO_DATA_WRITE_ACCESS)

        // When trying to update an existing event
        val result = createOrUpdateEventDetails.invoke(
            selectedDate,
            ORG_UNIT_UID,
            null,
            null
        ).first()

        // Then event should have been updated
        assertTrue(result.isFailure)
        assert(result.exceptionOrNull()?.message == EVENT_UPDATE_ERROR)
    }

    companion object {
        const val ORG_UNIT_UID = "orgUnitUid"
        const val EVENT_UPDATE_ERROR = "Event update error"
    }
}
