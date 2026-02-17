package org.dhis2.tracker.search.ui.action

sealed interface SearchScreenUiEvent {
    class OnSearchButtonClicked : SearchScreenUiEvent

    class OnClearSearchButtonClicked : SearchScreenUiEvent

    class OnCloseClicked : SearchScreenUiEvent
}
