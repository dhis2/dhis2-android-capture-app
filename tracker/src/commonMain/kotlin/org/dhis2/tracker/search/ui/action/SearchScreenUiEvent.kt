package org.dhis2.tracker.search.ui.action

sealed interface SearchScreenUiEvent {
    data object OnSearchButtonClicked : SearchScreenUiEvent

    data object OnClearSearchButtonClicked : SearchScreenUiEvent

    data object OnCloseClicked : SearchScreenUiEvent
}
