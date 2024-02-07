package org.dhis2.usescases.searchTrackEntity.searchparameters.model

import kotlinx.coroutines.flow.MutableSharedFlow

data class SearchParametersUiState(
    val items: List<SearchParameter> = listOf(),
    val minAttributesMessage: String? = null,
    val shouldShowMinAttributeWarning: MutableSharedFlow<Boolean> = MutableSharedFlow(),
)
