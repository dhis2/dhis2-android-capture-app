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
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.mobile.ui.designsystem.component.OrgTreeItem

class OUTreeViewModel(
    private val repository: OUTreeRepository,
    private val selectedOrgUnits: MutableList<String>,
    private val singleSelection: Boolean,
    private val model: OUTreeModel,
    private val dispatchers: DispatcherProvider,
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
        viewModelScope.launch(dispatchers.io()) {
            OrgUnitIdlingResource.increment()
            val orgUnits = repository.orgUnits(name)
            val treeNodes = ArrayList<OrgTreeItem>()

            orgUnits.forEach { org ->
                val canBeSelected = repository.canBeSelected(org.uid())
                treeNodes.add(
                    OrgTreeItem(
                        uid = org.uid(),
                        label = org.displayName()!!,
                        isOpen = true,
                        hasChildren = repository.orgUnitHasChildren(org.uid()),
                        selected = selectedOrgUnits.contains(org.uid()),
                        level = org.level()!!,
                        selectedChildrenCount =
                            repository.countSelectedChildren(
                                org.uid(),
                                selectedOrgUnits,
                            ),
                        canBeSelected = canBeSelected,
                    ),
                )
            }
            OrgUnitIdlingResource.decrement()
            _treeNodes.update { treeNodes }
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
        viewModelScope.launch(dispatchers.io()) {
            _treeNodes.update { openChildren(parentOrgUnitUid = parentOrgUnitUid) }
        }
    }

    fun model() = model

    private fun openChildren(
        currentList: List<OrgTreeItem> = _treeNodes.value,
        parentOrgUnitUid: String,
    ): List<OrgTreeItem> {
        val parentIndex = currentList.indexOfFirst { it.uid == parentOrgUnitUid }
        val orgUnits = repository.childrenOrgUnits(parentOrgUnitUid)
        val treeNodes =
            orgUnits.map { org ->
                val hasChildren = repository.orgUnitHasChildren(org.uid())
                OrgTreeItem(
                    uid = org.uid(),
                    label = org.displayName()!!,
                    isOpen = hasChildren,
                    hasChildren = hasChildren,
                    selected = selectedOrgUnits.contains(org.uid()),
                    level = org.level()!!,
                    selectedChildrenCount =
                        repository.countSelectedChildren(
                            org.uid(),
                            selectedOrgUnits,
                        ),
                    canBeSelected = repository.canBeSelected(org.uid()),
                )
            }
        return rebuildOrgUnitList(
            currentList = currentList,
            location = parentIndex,
            nodes = treeNodes,
        )
    }

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

    private fun getOrgUnits(): List<OrganisationUnit> = selectedOrgUnits.mapNotNull { uid -> repository.orgUnit(uid) }

    fun confirmSelection() {
        singleEventEnforcer.processEvent {
            _finalSelectedOrgUnits.update { getOrgUnits() }
        }
    }
}
