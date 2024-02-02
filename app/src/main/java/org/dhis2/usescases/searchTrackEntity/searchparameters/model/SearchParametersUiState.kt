package org.dhis2.usescases.searchTrackEntity.searchparameters.model

data class SearchParametersUiState(
    val items: List<SearchParameter> = listOf(),
)

val SearchParametersUiState.isSearchEnable: Boolean get() = items.isNotEmpty()
