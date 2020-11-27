package org.dhis2.utils.filters

import org.dhis2.utils.filters.workingLists.WorkingListItem

sealed class FilterItem(val type: Filters)
data class WorkingListFilter(val workingLists: List<WorkingListItem>) :
    FilterItem(Filters.WORKING_LIST) {
    private var selectedWorkingList: WorkingListItem? = null
}