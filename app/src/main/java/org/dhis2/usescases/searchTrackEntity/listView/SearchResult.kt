package org.dhis2.usescases.searchTrackEntity.listView

data class SearchResult(val type: SearchResultType) {
    enum class SearchResultType {
        LOADING,
        SEARCH_OR_CREATE,
        SEARCH_OUTSIDE,
        NO_MORE_RESULTS,
        NO_RESULTS,
        TOO_MANY_RESULTS
    }

    fun shouldClearProgramData() =
        type == SearchResultType.TOO_MANY_RESULTS || type == SearchResultType.SEARCH_OUTSIDE

    fun shouldClearGlobalData() = type == SearchResultType.SEARCH_OUTSIDE
}
