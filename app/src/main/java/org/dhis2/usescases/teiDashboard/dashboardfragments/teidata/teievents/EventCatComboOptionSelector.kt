package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents

import androidx.annotation.VisibleForTesting
import androidx.fragment.app.FragmentManager
import org.dhis2.commons.data.EventViewModel
import java.util.LinkedList
import java.util.Queue

class EventCatComboOptionSelector(
    private val categoryComboUid: String,
    private val fragmentManager: FragmentManager,
    private val categoryDialogInteractions: CategoryDialogInteractions = object :
        CategoryDialogInteractions {},
) {

    private val eventsWithoutCatComboOptionQueue: Queue<EventViewModel> = LinkedList()

    fun setEventsWithoutCatComboOption(events: List<EventViewModel>) {
        eventsWithoutCatComboOptionQueue.clear()
        eventsWithoutCatComboOptionQueue.addAll(events)
    }

    fun requestCatComboOption(
        onCatOptionComboSelected: (eventUid: String, selectedCatOptComboUid: String) -> Unit,
    ) {
        pollEvent()?.let { eventModel ->
            val event = eventModel.event
            categoryDialogInteractions.showDialog(
                categoryComboUid,
                event!!.eventDate() ?: event.dueDate(),
                fragmentManager,
            ) { selectedCatOptComboUid ->
                onCatOptionComboSelected(event.uid(), selectedCatOptComboUid)
                requestCatComboOption(onCatOptionComboSelected)
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun pollEvent(): EventViewModel? = eventsWithoutCatComboOptionQueue.poll()
}
