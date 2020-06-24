package org.dhis2.utils.filters.sorting

data class SortingItem(
    val displayName: String,
    val sortingType: SortingType,
    val selectedForList: Boolean,
    val selectedForSorting: Boolean
)