package org.dhis2.tracker.searchparameters.ui.state

import org.dhis2.mobile.commons.input.InputUiState

data class SearchUiState(
    val items: List<InputUiState> = listOf(),
    val minAttributesMessage: String? = null,
    val searchEnabled: Boolean = false,
    val clearSearchEnabled: Boolean = false,
)
