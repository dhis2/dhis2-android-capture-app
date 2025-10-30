package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents

import androidx.fragment.app.FragmentManager
import org.dhis2.commons.data.EventModel
import org.hisp.dhis.android.core.event.Event
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class EventCatComboOptionSelectorTest {
    private val catComboUid: String = "catComboUid"
    private val fragmentManager: FragmentManager = mock()
    private val categoryDialogInteractions: CategoryDialogInteractions = mock()
    private val eventCatComboOptionSelector =
        EventCatComboOptionSelector(
            catComboUid,
            fragmentManager,
            categoryDialogInteractions,
        )

    @Test
    fun should_set_events() {
        val list = listOf(mock<EventModel>())
        eventCatComboOptionSelector.setEventsWithoutCatComboOption(list)
        assertTrue(eventCatComboOptionSelector.pollEvent() == list.first())
    }

    @Test
    fun should_clear_previous_queue_and_set_events() {
        val prevList = listOf(mock<EventModel>())
        val list = listOf(mock<EventModel>())
        eventCatComboOptionSelector.setEventsWithoutCatComboOption(prevList)
        assertTrue(eventCatComboOptionSelector.pollEvent() == prevList.first())
        eventCatComboOptionSelector.setEventsWithoutCatComboOption(list)
        eventCatComboOptionSelector.pollEvent().let {
            assertTrue(it != null)
            assertTrue(it != prevList.first())
            assertTrue(it == list.first())
        }
    }

    @Test
    fun should_request_cat_option_combo() {
        val mockedEvent =
            mock<Event> {
                on { eventDate() } doReturn mock()
                on { uid() } doReturn "eventUid"
            }
        val mockedEventModel =
            mock<EventModel> {
                on { event } doReturn mockedEvent
            }
        val list = listOf(mockedEventModel)
        eventCatComboOptionSelector.setEventsWithoutCatComboOption(list)
        eventCatComboOptionSelector.requestCatComboOption { eventUid, selectedCatOptComboUid -> }
        verify(categoryDialogInteractions).showDialog(any(), any(), any(), any())
    }

    @Test
    fun should_not_request_cat_option_combo() {
        val list = emptyList<EventModel>()
        eventCatComboOptionSelector.setEventsWithoutCatComboOption(list)
        eventCatComboOptionSelector.requestCatComboOption { eventUid, selectedCatOptComboUid -> }
        verify(categoryDialogInteractions, times(0)).showDialog(any(), any(), any(), any())
    }
}
