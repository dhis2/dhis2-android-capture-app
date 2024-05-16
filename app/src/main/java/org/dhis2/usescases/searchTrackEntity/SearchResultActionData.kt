package org.dhis2.usescases.searchTrackEntity

data class SearchResultActionData(
    val resultAction: ResultAction,
    val onlineAvailable: Boolean = true,
)

enum class ResultAction {
    END_OF_LIST,
    NO_RESULTS,
    TOO_MANY_RESULTS,
}
