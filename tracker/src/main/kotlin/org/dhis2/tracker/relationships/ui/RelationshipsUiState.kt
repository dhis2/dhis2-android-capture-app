package org.dhis2.tracker.relationships.ui

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

sealed class RelationshipsUiState<out T> {
    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> get() = _snackbarMessage

    suspend fun sendSnackbarMessage(message: String) {
        _snackbarMessage.emit(message)
    }

    data object Loading : RelationshipsUiState<Nothing>()
    data object Empty : RelationshipsUiState<Nothing>()
    data class Success<T>(val data: T) : RelationshipsUiState<T>()
    data class Error(val message: String) : RelationshipsUiState<Nothing>()
}
