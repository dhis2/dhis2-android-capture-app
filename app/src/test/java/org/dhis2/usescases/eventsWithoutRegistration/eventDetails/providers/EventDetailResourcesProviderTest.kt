package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers

import org.dhis2.R
import org.dhis2.commons.resources.EventResourcesProvider
import org.dhis2.commons.resources.ResourceManager
import org.hisp.dhis.android.core.event.EventNonEditableReason
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class EventDetailResourcesProviderTest {
    private val eventNotFoundMessage = "Event not found message"
    private val resourceManager: ResourceManager =
        mock {
            on { getString(R.string.edition_event_not_found) } doReturn eventNotFoundMessage
        }
    private val eventResourcesProvider: EventResourcesProvider = mock()

    private val provider =
        EventDetailResourcesProvider(
            programUid = "programUid",
            programStage = "programStageUid",
            resourceManager = resourceManager,
            eventResourcesProvider = eventResourcesProvider,
        )

    @Test
    fun `Should provide event not found message when event does not exist`() {
        val result = provider.provideEditionStatus(EventNonEditableReason.EVENT_NOT_FOUND)

        assertEquals(eventNotFoundMessage, result)
    }
}
