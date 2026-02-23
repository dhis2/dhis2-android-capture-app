package org.dhis2.tracker.search.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dhis2.mobile.commons.resources.StringResourceProvider
import org.dhis2.mobile.tracker.resources.Res
import org.dhis2.mobile.tracker.resources.min_characters_warning
import org.dhis2.tracker.search.ui.state.SearchParametersUiState

class SearchParametersViewModel(
    private val resourceProvider: StringResourceProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchParametersUiState())
    val uiState: StateFlow<SearchParametersUiState> = _uiState.asStateFlow()

    private val _validationResult = MutableStateFlow<Boolean?>(null)
    val validationResult: StateFlow<Boolean?> = _validationResult.asStateFlow()

    fun updateFromExternal(externalState: SearchParametersUiState) {
        _uiState.value = externalState
    }

    fun onValidateSearch() {
        viewModelScope.launch {
            val minCharactersToSearch = onValidateMinCharacters()
            _validationResult.value = minCharactersToSearch
        }
    }

    suspend fun onValidateMinCharacters(): Boolean {
        val items = _uiState.value.items
        // Check if there are invalid items to later check the warning
        val invalidItems =
            items.filter { item ->
                val min = item.minCharactersToSearch ?: return@filter false
                val value = item.value
                !value.isNullOrEmpty() && value.length < min
            }

        if (invalidItems.isNotEmpty()) {
            val updatedItems =
                items.map { item ->
                    val min = item.minCharactersToSearch
                    if (min != null && !item.value.isNullOrEmpty() && item.value.length < min) {
                        item.copy(
                            error =
                                resourceProvider.provideString(
                                    resource = Res.string.min_characters_warning,
                                    min,
                                ),
                        )
                    } else {
                        item.copy(error = null)
                    }
                }
            _uiState.update { it.copy(items = updatedItems) }
            return false
        }

        val clearedItems = items.map { it.copy(error = null) }
        _uiState.update { it.copy(items = clearedItems) }
        return true
    }

    fun resetValidationResult() {
        _validationResult.value = null
    }
}
