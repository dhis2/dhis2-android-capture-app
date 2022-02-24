package org.dhis2.usescases.searchTrackEntity

sealed class SearchTEScreenState(
    val screenState: SearchScreenState,
    open val previousSate: SearchScreenState
)

data class SearchList(
    override val previousSate: SearchScreenState,
    private val listType: SearchScreenState,
    val displayFrontPageList: Boolean,
    val canCreateWithoutSearch: Boolean,
    val queryHasData: Boolean,
    val minAttributesToSearch: Int,
    val isSearching: Boolean
) : SearchTEScreenState(listType, previousSate) {
    fun canDisplayCreateButton() = canCreateWithoutSearch || isSearching
}

data class SearchMap(
    override val previousSate: SearchScreenState,
    private val mapType: SearchScreenState,
    val canCreateWithoutSearch: Boolean
) : SearchTEScreenState(mapType, previousSate)

data class SearchForm(
    override val previousSate: SearchScreenState,
    val queryHasData: Boolean,
    val minAttributesToSearch: Int
) : SearchTEScreenState(SearchScreenState.SEARCHING, previousSate)

data class SearchAnalytics(
    override val previousSate: SearchScreenState
) : SearchTEScreenState(SearchScreenState.ANALYTICS, previousSate)

enum class SearchScreenState {
    NONE,
    LIST,
    MAP,
    SEARCHING,
    ANALYTICS
}
