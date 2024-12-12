package org.dhis2.tracker.relationships.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dhis2.commons.resources.D2ErrorUtils
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.tracker.relationships.domain.AddRelationship
import org.dhis2.tracker.relationships.domain.DeleteRelationships
import org.dhis2.tracker.relationships.domain.GetRelationshipsByType
import org.dhis2.tracker.relationships.model.ListSelectionState
import org.dhis2.tracker.relationships.model.RelationshipDirection
import org.dhis2.tracker.relationships.model.RelationshipSection

@OptIn(ExperimentalCoroutinesApi::class)
class RelationshipsViewModel(
    private val dispatcher: DispatcherProvider,
    private val getRelationshipsByType: GetRelationshipsByType,
    private val deleteRelationships: DeleteRelationships,
    private val addRelationship: AddRelationship,
    private val d2ErrorUtils: D2ErrorUtils,
) : ViewModel() {

    private val _relationshipsUiState =
        MutableStateFlow<RelationshipsUiState<List<RelationshipSection>>>(RelationshipsUiState.Loading)
    val relationshipsUiState: StateFlow<RelationshipsUiState<List<RelationshipSection>>> =
        _relationshipsUiState.asStateFlow()

    private val _relationshipSelectionState = MutableStateFlow(ListSelectionState())
    val relationshipSelectionState = _relationshipSelectionState.asStateFlow()

    private val _showDeleteConfirmation = MutableStateFlow(false)
    val showDeleteConfirmation = _showDeleteConfirmation.asStateFlow()

    fun refreshRelationships() {
        viewModelScope.launch(dispatcher.io()) {
            getRelationshipsByType()
                .flatMapLatest {
                    if (it.isEmpty()) {
                        flowOf(RelationshipsUiState.Empty)
                    } else {
                        flowOf(RelationshipsUiState.Success(it))
                    }
                }
                .collect {
                    _relationshipsUiState.value = it
                }
        }
    }

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
        viewModelScope.launch(dispatcher.io()) {
            deleteRelationships(
                relationshipSelectionState.value.selectedItems
            ).collect { result ->
                if (result.isSuccess) {
                    stopSelectingMode()
                    refreshRelationships()
                }
            }
        }
    }

    fun onDeleteClick() {
        _showDeleteConfirmation.value = true
    }

    fun onDismissDelete() {
        _showDeleteConfirmation.value = false
    }

    fun onAddRelationship(
        selectedTeiUid: String,
        relationshipTypeUid: String,
        direction: RelationshipDirection,
    ) {
        viewModelScope.launch(dispatcher.io()) {
            addRelationship(
                selectedTeiUid = selectedTeiUid,
                relationshipTypeUid = relationshipTypeUid,
                direction = direction,
            ).fold(
                onSuccess = {
                    refreshRelationships()
                },
                onFailure = { d2Error ->
                    d2ErrorUtils.getErrorMessage(d2Error)?.let {
                        showSnackbar(it)
                    }
                }
            )
        }
    }

    private fun showSnackbar(message: String) {
        viewModelScope.launch {
            _relationshipsUiState.value.sendSnackbarMessage(message)
        }
    }
}