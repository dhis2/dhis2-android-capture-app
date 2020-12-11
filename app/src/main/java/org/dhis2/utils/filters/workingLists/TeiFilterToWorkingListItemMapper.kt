package org.dhis2.utils.filters.workingLists

import org.hisp.dhis.android.core.common.AssignedUserMode
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceFilter

class TeiFilterToWorkingListItemMapper(
    private val defaultWorkingListLabel: String
) {
    fun map(teiFilter: TrackedEntityInstanceFilter): WorkingListItem {
        return TeiWorkingListItem(
            teiFilter.uid(),
            teiFilter.displayName() ?: defaultWorkingListLabel,
            teiFilter.enrollmentStatus(),
            teiFilter.eventFilters()?.any { eventFilter->eventFilter.assignedUserMode() == AssignedUserMode.CURRENT }
        )
    }
}