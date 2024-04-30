package org.dhis2.usescases.searchTrackEntity.searchparameters.model

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.dhis2.form.model.FieldUiModel

data class SearchParametersUiState(
    val items: List<FieldUiModel> = listOf(),
    val minAttributesMessage: String? = null,
    private val _shouldShowMinAttributeWarning: MutableSharedFlow<Boolean> = MutableSharedFlow(
        replay = Int.MAX_VALUE,
    ),
    val searchEnabled: Boolean = false,
    val clearSearchEnabled: Boolean = false,
    val searchedItems: Map<String, String> = mapOf(),
    private val _isOnBackPressed: MutableSharedFlow<Boolean> = MutableSharedFlow(
        replay = Int.MAX_VALUE,
    ),
) {
    val shouldShowMinAttributeWarning: SharedFlow<Boolean> = _shouldShowMinAttributeWarning
    val isOnBackPressed: SharedFlow<Boolean> = _isOnBackPressed

    suspend fun updateMinAttributeWarning(showWarning: Boolean) {
        _shouldShowMinAttributeWarning.emit(showWarning)
    }

    suspend fun onBackPressed(pressed: Boolean) {
        _isOnBackPressed.emit(pressed)
    }
}
