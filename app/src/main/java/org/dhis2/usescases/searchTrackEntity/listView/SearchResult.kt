package org.dhis2.usescases.searchTrackEntity.listView

data class SearchResult(val type: SearchResultType) {
    enum class SearchResultType {
        LOADING,
        SEARCH_OUTSIDE,
        NO_MORE_RESULTS,
        NO_RESULTS,
        TOO_MANY_RESULTS
    }
}
