package org.dhis2.usescases.searchTrackEntity.listView

import org.dhis2.usescases.searchTrackEntity.ui.SearchUIData

data class SearchResult(
    val type: SearchResultType,
    val extraData: String? = null,
    val uiData: SearchUIData? = null,
) {
    enum class SearchResultType {
        LOADING,
        SEARCH_OR_CREATE,
        SEARCH,
        UNABLE_SEARCH_OUTSIDE,
        SEARCH_OUTSIDE,
        NO_MORE_RESULTS,
        NO_MORE_RESULTS_OFFLINE,
        NO_RESULTS,
        TOO_MANY_RESULTS,
    }

    fun shouldClearProgramData() =
        type == SearchResultType.TOO_MANY_RESULTS || type == SearchResultType.SEARCH_OR_CREATE

    fun shouldClearGlobalData() = type == SearchResultType.SEARCH_OUTSIDE

    fun shouldDisplayInFullSize() = when (type) {
        SearchResultType.SEARCH_OR_CREATE,
        SearchResultType.SEARCH,
        SearchResultType.NO_RESULTS,
        SearchResultType.TOO_MANY_RESULTS,
        -> true
        else -> false
    }
}
