package org.dhis2.tracker.relationships.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.dhis2.tracker.relationships.domain.GetRelationshipsByType
import org.dhis2.tracker.relationships.model.ListSelectionState
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

    private val _relationshipSelectionState = MutableStateFlow(ListSelectionState())
    val relationshipSelectionState = _relationshipSelectionState.asStateFlow()


    fun updateSelectedList(relationshipUid: String) {
        viewModelScope.launch {
            val updatedState = relationshipSelectionState.value.let {
                it.copy(
                    selectingMode = true,
                    selectedItems = it.selectedItems.toMutableList().apply {
                        if (contains(relationshipUid)) {
                            remove(relationshipUid)
                        } else {
                            add(relationshipUid)
                        }
                    }
                )
            }
            _relationshipSelectionState.emit(updatedState)
        }
    }

    fun deselectAll() {
        viewModelScope.launch {
            val updatedState = relationshipSelectionState.value.copy(
                selectedItems = emptyList()
            )
            _relationshipSelectionState.emit(updatedState)
        }
    }

    fun selectAll() {
        val allRelationshipsUid = (relationshipsUiState.value as? RelationshipsUiState.Success)
            ?.data?.flatMap { it.relationships.map { it.ownerUid } } ?: listOf()

        viewModelScope.launch {
            val updatedState = relationshipSelectionState.value.copy(
                selectedItems = allRelationshipsUid
            )
            _relationshipSelectionState.emit(updatedState)
        }
    }

    fun startSelectingMode() {
        viewModelScope.launch {
            val updatedState = relationshipSelectionState.value.copy(
                selectingMode = true,
            )
            _relationshipSelectionState.emit(updatedState)
        }
    }

    fun stopSelectingMode() {
        viewModelScope.launch {
            val updatedState = relationshipSelectionState.value.copy(
                selectingMode = false,
                selectedItems = emptyList()
            )
            _relationshipSelectionState.emit(updatedState)
        }
    }
}