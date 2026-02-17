package org.dhis2.tracker.search.ui.provider

sealed interface SearchScreenUiEvent {
    class OnSearchButtonClicked : SearchScreenUiEvent

    class OnClearSearchButtonClicked : SearchScreenUiEvent

    class OnCloseClicked : SearchScreenUiEvent
}
