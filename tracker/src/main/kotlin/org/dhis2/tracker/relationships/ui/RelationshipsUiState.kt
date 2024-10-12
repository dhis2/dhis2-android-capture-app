package org.dhis2.tracker.relationships.ui

sealed class RelationshipsUiState<out T> {
    data object Loading : RelationshipsUiState<Nothing>()
    data object Empty : RelationshipsUiState<Nothing>()
    data class Success<T>(val data: T) : RelationshipsUiState<T>()
    data class Error(val message: String) : RelationshipsUiState<Nothing>()
}
