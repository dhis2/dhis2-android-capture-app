package org.dhis2.commons.orgunitselector

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dhis2.commons.schedulers.SingleEventEnforcer
import org.dhis2.commons.schedulers.get
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.mobile.commons.domain.FetchOrgUnits
import org.dhis2.mobile.commons.domain.FetchOrgUnitsInput
import org.dhis2.mobile.commons.extensions.launchUseCase
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.mobile.ui.designsystem.component.OrgTreeItem
import timber.log.Timber

class OUTreeViewModel(
    private val repository: OUTreeRepository,
    private val selectedOrgUnits: MutableList<String>,
    private val singleSelection: Boolean,
    private val model: OUTreeModel,
    private val dispatchers: DispatcherProvider,
    private val fetchOrgUnits: FetchOrgUnits,
) : ViewModel() {
    private val _treeNodes = MutableStateFlow(emptyList<OrgTreeItem>())
    val treeNodes: StateFlow<List<OrgTreeItem>> =
        _treeNodes
            .map { list ->
                model.hideOrgUnits?.let { filterUnits ->
                    list.filterNot { orgUnit ->
                        filterUnits.any { filterUnit -> filterUnit.uid() == orgUnit.uid }
                    }
                } ?: list
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _finalSelectedOrgUnits = MutableStateFlow(emptyList<OrganisationUnit>())
    val finalSelectedOrgUnits: StateFlow<List<OrganisationUnit>> = _finalSelectedOrgUnits

    private val singleEventEnforcer = SingleEventEnforcer.get()

    init {
        fetchInitialOrgUnits()
    }

    private fun fetchInitialOrgUnits(name: String? = null) {
        launchUseCase(dispatchers.io()) {
            val result =
                fetchOrgUnits(
                    FetchOrgUnitsInput(
                        query = name,
                        selectedOrgUnits = selectedOrgUnits,
                    ),
                )

            result.fold(
                onSuccess = { newList ->
                    _treeNodes.update { newList }
                },
                onFailure = Timber::e,
            )
        }
    }

    fun searchByName(name: String) {
        if (name.length >= 2) {
            fetchInitialOrgUnits(name)
        } else {
            fetchInitialOrgUnits()
        }
    }

    fun onOpenChildren(parentOrgUnitUid: String) {
        launchUseCase(dispatchers.io()) {
            val parentIndex = _treeNodes.value.indexOfFirst { it.uid == parentOrgUnitUid }
            val orgUnits = repository.childrenOrgUnits(parentOrgUnitUid)
            val treeNodes =
                orgUnits.map { org ->
                    val hasChildren = repository.orgUnitHasChildren(org.uid)
                    OrgTreeItem(
                        uid = org.uid,
                        label = org.label,
                        isOpen = hasChildren,
                        hasChildren = hasChildren,
                        selected = selectedOrgUnits.contains(org.uid),
                        level = org.level,
                        selectedChildrenCount =
                            repository.countSelectedChildren(
                                org.uid,
                                selectedOrgUnits,
                            ),
                        canBeSelected = repository.canBeSelected(org.uid),
                    )
                }
            val rebuiltList =
                rebuildOrgUnitList(
                    currentList = _treeNodes.value,
                    location = parentIndex,
                    nodes = treeNodes,
                )

            _treeNodes.update { rebuiltList }
        }
    }

    fun model() = model

    fun onOrgUnitCheckChanged(
        orgUnitUid: String,
        isChecked: Boolean,
    ) {
        viewModelScope.launch(dispatchers.io()) {
            OrgUnitIdlingResource.increment()
            if (singleSelection) {
                selectedOrgUnits.clear()
            }
            if (isChecked && !selectedOrgUnits.contains(orgUnitUid)) {
                selectedOrgUnits.add(orgUnitUid)
            } else if (!isChecked && selectedOrgUnits.contains(orgUnitUid)) {
                selectedOrgUnits.remove(orgUnitUid)
            }
            val treeNodeList =
                treeNodes.value.map { currentTreeNode ->
                    currentTreeNode.copy(
                        selected = selectedOrgUnits.contains(currentTreeNode.uid),
                        selectedChildrenCount =
                            repository.countSelectedChildren(
                                currentTreeNode.uid,
                                selectedOrgUnits,
                            ),
                    )
                }
            OrgUnitIdlingResource.decrement()
            _treeNodes.update { treeNodeList }
        }
    }

    fun clearAll() {
        viewModelScope.launch(dispatchers.io()) {
            OrgUnitIdlingResource.increment()
            selectedOrgUnits.clear()
            val treeNodeList =
                treeNodes.value.map { currentTreeNode ->
                    currentTreeNode.copy(
                        selected = false,
                        selectedChildrenCount = 0,
                    )
                }
            OrgUnitIdlingResource.decrement()
            _treeNodes.update { treeNodeList }
        }
    }

    private fun rebuildOrgUnitList(
        currentList: List<OrgTreeItem>,
        location: Int,
        nodes: List<OrgTreeItem>,
    ): List<OrgTreeItem> {
        val nodesCopy = ArrayList(currentList)
        nodesCopy[location] = nodesCopy[location].copy(isOpen = !nodesCopy[location].isOpen)

        if (!nodesCopy[location].isOpen) {
            val level = nodesCopy[location].level
            val deleteList: MutableList<OrgTreeItem> = ArrayList()
            var sameLevel = true
            for (i in location + 1 until nodesCopy.size) {
                if (sameLevel) {
                    if (nodesCopy[i].level > level) {
                        deleteList.add(nodesCopy[i])
                    } else {
                        sameLevel = false
                    }
                }
            }
            nodesCopy.removeAll(deleteList.toSet())
        } else {
            nodesCopy.addAll(location + 1, nodes)
        }

        return nodesCopy
    }

    fun confirmSelection() {
        launchUseCase(dispatchers.io()) {
            val list =
                selectedOrgUnits.mapNotNull { uid ->
                    repository.legacyOrgUnit(uid)
                }
            singleEventEnforcer.processEvent {
                _finalSelectedOrgUnits.update { list }
            }
        }
    }
}
