package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents

import androidx.fragment.app.FragmentManager
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.dhis2.commons.data.EventViewModel
import org.hisp.dhis.android.core.event.Event
import org.junit.Assert.assertTrue
import org.junit.Test

class EventCatComboOptionSelectorTest {

    private val catComboUid: String = "catComboUid"
    private val fragmentManager: FragmentManager = mock()
    private val categoryDialogInteractions: CategoryDialogInteractions = mock()
    private val eventCatComboOptionSelector = EventCatComboOptionSelector(
        catComboUid,
        fragmentManager,
        categoryDialogInteractions
    )

    @Test
    fun should_set_events() {
        val list = listOf(mock<EventViewModel>())
        eventCatComboOptionSelector.setEventsWithoutCatComboOption(list)
        assertTrue(eventCatComboOptionSelector.pollEvent() == list.first())
    }

    @Test
    fun should_clear_previous_queue_and_set_events() {
        val prevList = listOf(mock<EventViewModel>())
        val list = listOf(mock<EventViewModel>())
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
        val mockedEvent = mock<Event> {
            on { eventDate() } doReturn mock()
            on { uid() } doReturn "eventUid"
        }
        val mockedEventViewModel = mock<EventViewModel> {
            on { event } doReturn mockedEvent
        }
        val list = listOf(mockedEventViewModel)
        eventCatComboOptionSelector.setEventsWithoutCatComboOption(list)
        eventCatComboOptionSelector.requestCatComboOption { eventUid, selectedCatOptComboUid -> }
        verify(categoryDialogInteractions).showDialog(any(), any(), any(), any())
    }

    @Test
    fun should_not_request_cat_option_combo() {
        val list = emptyList<EventViewModel>()
        eventCatComboOptionSelector.setEventsWithoutCatComboOption(list)
        eventCatComboOptionSelector.requestCatComboOption { eventUid, selectedCatOptComboUid -> }
        verify(categoryDialogInteractions, times(0)).showDialog(any(), any(), any(), any())
    }
}
