package org.dhis2.usescases.searchTrackEntity.searchparameters.model

import kotlinx.coroutines.flow.MutableSharedFlow
import org.dhis2.form.model.FieldUiModel

data class SearchParametersUiState(
    val items: List<FieldUiModel> = listOf(),
    val minAttributesMessage: String? = null,
    val shouldShowMinAttributeWarning: MutableSharedFlow<Boolean> = MutableSharedFlow(),
)
