package org.dhis2.tracker.relationships.ui

sealed class RelationshipsListUiState<out T> {
    data object Loading : RelationshipsListUiState<Nothing>()
    data object Empty : RelationshipsListUiState<Nothing>()
    data class Success<T>(val data: T) : RelationshipsListUiState<T>()
    data class Error(val message: String) : RelationshipsListUiState<Nothing>()
}
