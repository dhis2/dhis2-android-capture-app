package org.dhis2.tracker.search.model

data class SearchTrackerParametersModel(
    val selectedProgram: String?,
    val allowCache: Boolean,
    val excludeValues: HashSet<String>?,
    val hasStateFilters: Boolean,
    val isOnline: Boolean,
    val queryData: MutableMap<String, List<String>?>?,
) {
    fun copy(): SearchTrackerParametersModel =
        copy(
            queryData = mutableMapOf<String, List<String>?>().apply { queryData?.let { putAll(it) } },
        )
}
