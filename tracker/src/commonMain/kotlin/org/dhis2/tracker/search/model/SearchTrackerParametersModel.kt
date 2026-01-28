package org.dhis2.tracker.search.model

data class SearchTrackerParametersModel(
    val selectedProgram: String?,
    val queryData: MutableMap<String, List<String>?>?,
) {
    fun copy(): SearchTrackerParametersModel =
        copy(
            queryData = mutableMapOf<String, List<String>?>().apply { queryData?.let { putAll(it) } },
        )
}
