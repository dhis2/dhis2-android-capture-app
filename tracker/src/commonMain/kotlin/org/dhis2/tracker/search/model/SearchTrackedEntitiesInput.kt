package org.dhis2.tracker.search.model

data class SearchTrackedEntitiesInput(
    val selectedProgram: String?,
    val allowCache: Boolean,
    val excludeValues: Set<String>?,
    val hasStateFilters: Boolean,
    val isOnline: Boolean,
    val queryDataList: List<QueryData>? = null,
)
