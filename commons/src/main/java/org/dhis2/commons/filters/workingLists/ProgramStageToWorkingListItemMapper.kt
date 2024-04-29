package org.dhis2.commons.filters.workingLists

import org.hisp.dhis.android.core.programstageworkinglist.ProgramStageWorkingList

class ProgramStageToWorkingListItemMapper(
    private val defaultWorkingListLabel: String,
) {

    fun map(item: ProgramStageWorkingList): WorkingListItem {
        return ProgramStageWorkingList(
            item.uid(),
            item.displayName() ?: defaultWorkingListLabel,
        )
    }
}
