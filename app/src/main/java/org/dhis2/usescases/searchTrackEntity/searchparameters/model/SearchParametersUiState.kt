package org.dhis2.usescases.searchTrackEntity.searchparameters.model

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.dhis2.form.model.FieldUiModel

data class SearchParametersUiState(
    val items: List<FieldUiModel> = listOf(),
    val minAttributesMessage: String? = null,
    private val _shouldShowMinAttributeWarning: MutableSharedFlow<Boolean> = MutableSharedFlow(),
) {
    val shouldShowMinAttributeWarning: SharedFlow<Boolean> = _shouldShowMinAttributeWarning

    suspend fun updateMinAttributeWarning(showWarning: Boolean) {
        _shouldShowMinAttributeWarning.emit(showWarning)
    }
}
