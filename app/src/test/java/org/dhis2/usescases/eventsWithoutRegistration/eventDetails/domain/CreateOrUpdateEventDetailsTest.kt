package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import java.util.Date
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.data.EventDetailsRepository
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventEditableStatus.Editable
import org.hisp.dhis.android.core.event.EventEditableStatus.NonEditable
import org.hisp.dhis.android.core.event.EventNonEditableReason.NO_DATA_WRITE_ACCESS
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreateOrUpdateEventDetailsTest {

    private val selectedDate = Date()
    private val event: Event = mock()
    private val repository: EventDetailsRepository = mock {
        on { getEvent() } doReturn event
        on { updateEvent(selectedDate, ORG_UNIT_UID, null, null) } doReturn event
    }

    private lateinit var createOrUpdateEventDetails: CreateOrUpdateEventDetails

    @Before
    fun setUp() {
        createOrUpdateEventDetails = CreateOrUpdateEventDetails(repository)
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
        assertTrue(result)
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
        assertFalse(result)
    }

    companion object {
        const val ORG_UNIT_UID = "orgUnitUid"
    }
}
