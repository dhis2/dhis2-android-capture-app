package org.dhis2.usescases.settings.models

sealed class SyncErrorLogUiState {
    data object Loading : SyncErrorLogUiState()

    data class Log(
        val errorList: List<ErrorViewModel>,
    ) : SyncErrorLogUiState()

    data class Selection(
        val errorList: List<ErrorViewModel>,
    ) : SyncErrorLogUiState()
}
