package org.dhis2.commons.filters.sorting

import org.dhis2.commons.filters.Filters

data class SortingItem(
    var filterSelectedForSorting: Filters,
    var sortingStatus: SortingStatus = SortingStatus.ASC,
) {
    companion object {
        @JvmStatic
        fun create(filter: Filters) = SortingItem(filter)
    }
}
