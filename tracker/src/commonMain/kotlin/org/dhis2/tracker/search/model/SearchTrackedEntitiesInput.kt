package org.dhis2.tracker.search.model

data class SearchTrackedEntitiesInput(
    val selectedProgram: String?,
    val allowCache: Boolean,
    val excludeValues: HashSet<String>?,
    val hasStateFilters: Boolean,
    val isOnline: Boolean,
    val queryData: MutableMap<String, List<String>?>?,
) {
    fun copy(): SearchTrackedEntitiesInput =
        copy(
            queryData = mutableMapOf<String, List<String>?>().apply { queryData?.let { putAll(it) } },
        )
}
