package org.dhis2.tracker.relationships.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.dhis2.tracker.relationships.domain.GetRelationshipsByType
import org.dhis2.tracker.relationships.model.RelationshipSection

class RelationShipsViewModel(
    private val getRelationshipsByType: GetRelationshipsByType
) : ViewModel() {
    private val _uiState =
        MutableStateFlow<RelationshipsListUiState<List<RelationshipSection>>>(
            RelationshipsListUiState.Loading
        )
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = RelationshipsListUiState.Success(
                getRelationshipsByType("")
            )
        }
    }
}