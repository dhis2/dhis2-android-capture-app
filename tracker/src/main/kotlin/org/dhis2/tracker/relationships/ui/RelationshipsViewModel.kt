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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.tracker.relationships.domain.DeleteRelationships
import org.dhis2.tracker.relationships.domain.GetRelationshipsByType
import org.dhis2.tracker.relationships.model.ListSelectionState
import org.dhis2.tracker.relationships.model.RelationshipSection

@OptIn(ExperimentalCoroutinesApi::class)
class RelationshipsViewModel(
    getRelationshipsByType: GetRelationshipsByType,
    private val deleteRelationships: DeleteRelationships,
    private val dispatcher: DispatcherProvider,
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
        viewModelScope.launch(dispatcher.io()) {
            _relationshipSelectionState.update {
                val updatedList = it.selectedItems.toMutableList().apply {
                    if (contains(relationshipUid)) {
                        remove(relationshipUid)
                    } else {
                        add(relationshipUid)
                    }
                }
                it.copy(
                    selectingMode = updatedList.isNotEmpty(),
                    selectedItems = updatedList
                )
            }
        }
    }

    fun deselectAll() {
        viewModelScope.launch(dispatcher.io()) {
            _relationshipSelectionState.update {
                it.copy(selectedItems = emptyList())
            }
        }
    }

    fun selectAll() {
        val allRelationshipsUid = (relationshipsUiState.value as? RelationshipsUiState.Success)
            ?.data?.flatMap { it.relationships.map { it.ownerUid } } ?: listOf()

        viewModelScope.launch(dispatcher.io()) {
            _relationshipSelectionState.update {
                it.copy(selectedItems = allRelationshipsUid)
            }
        }
    }

    fun startSelectingMode() {
        viewModelScope.launch(dispatcher.io()) {
            _relationshipSelectionState.update {
                it.copy(selectingMode = true)
            }
        }
    }

    fun stopSelectingMode() {
        viewModelScope.launch(dispatcher.io()) {
            _relationshipSelectionState.update {
               it.copy(
                    selectingMode = false,
                    selectedItems = emptyList()
               )
            }
        }
    }

    fun deleteSelectedRelationships() {
        deleteRelationships(relationshipSelectionState.value.selectedItems)
    }
}