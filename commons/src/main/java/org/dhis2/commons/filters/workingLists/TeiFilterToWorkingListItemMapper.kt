package org.dhis2.commons.filters.workingLists

import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceFilter

class TeiFilterToWorkingListItemMapper(
    private val defaultWorkingListLabel: String
) {
    fun map(teiFilter: TrackedEntityInstanceFilter): WorkingListItem {
        return TrackedEntityInstanceWorkingList(
            teiFilter.uid(),
            teiFilter.displayName() ?: defaultWorkingListLabel
        )
    }
}
