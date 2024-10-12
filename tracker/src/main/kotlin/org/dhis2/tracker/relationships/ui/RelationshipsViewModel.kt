package org.dhis2.tracker.relationships.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import org.dhis2.tracker.relationships.domain.GetRelationshipsByType
import org.dhis2.tracker.relationships.model.RelationshipSection

@OptIn(ExperimentalCoroutinesApi::class)
class RelationshipsViewModel(
    getRelationshipsByType: GetRelationshipsByType
) : ViewModel() {

    val relationshipsUiState: StateFlow<RelationshipsUiState<List<RelationshipSection>>> =
        getRelationshipsByType()
            .flatMapLatest {
                if (it.isEmpty()) {
                    flowOf(RelationshipsUiState.Empty)
                } else {
                    flowOf(RelationshipsUiState.Success(it))
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = RelationshipsUiState.Loading
            )
}