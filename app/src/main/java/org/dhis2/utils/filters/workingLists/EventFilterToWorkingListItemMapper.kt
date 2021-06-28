package org.dhis2.utils.filters.workingLists

import org.hisp.dhis.android.core.event.EventFilter

class EventFilterToWorkingListItemMapper(
    private val defaultWorkingListLabel: String
) {
    fun map(eventFilter: EventFilter): WorkingListItem {
        return WorkingListItem(
            eventFilter.uid(),
            eventFilter.displayName() ?: defaultWorkingListLabel
        )
    }
}
