package org.dhis2.tracker.relationships.ui.state

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

sealed class RelationshipsUiState {
    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> get() = _snackbarMessage

    suspend fun sendSnackbarMessage(message: String) {
        _snackbarMessage.emit(message)
    }

    data object Loading : RelationshipsUiState()

    data object Empty : RelationshipsUiState()

    data class Success(
        val data: List<RelationshipSectionUiState>,
    ) : RelationshipsUiState()

    data class Error(
        val message: String,
    ) : RelationshipsUiState()
}
