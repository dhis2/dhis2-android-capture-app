package org.dhis2.commons.orgunitselector

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.ui.dialogs.orgunit.OrgUnitTreeItem
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

class OUTreeViewModel(
    private val repository: OUTreeRepository,
    private val selectedOrgUnits: MutableList<String>,
    private val singleSelection: Boolean,
    private val dispatchers: DispatcherProvider
) : ViewModel() {
    private val _treeNodes = MutableStateFlow(emptyList<OrgUnitTreeItem>())
    val treeNodes: StateFlow<List<OrgUnitTreeItem>> = _treeNodes

    init {
        fetchInitialOrgUnits()
    }

    private fun fetchInitialOrgUnits(name: String? = null) {
        viewModelScope.launch(dispatchers.io()) {
            val orgUnits = repository.orgUnits(name)
            val treeNodes = ArrayList<OrgUnitTreeItem>()

            orgUnits.forEach { org ->
                val canBeSelected = repository.canBeSelected(org.uid())
                treeNodes.add(
                    OrgUnitTreeItem(
                        uid = org.uid(),
                        label = org.displayName()!!,
                        isOpen = true,
                        hasChildren = repository.orgUnitHasChildren(org.uid()),
                        selected = selectedOrgUnits.contains(org.uid()),
                        level = org.level()!!,
                        selectedChildrenCount = repository.countSelectedChildren(
                            org.uid(),
                            selectedOrgUnits
                        ),
                        canBeSelected = canBeSelected
                    )
                )
            }
            _treeNodes.update { treeNodes }
        }
    }

    fun searchByName(name: String) {
        if (name.isEmpty()) {
            fetchInitialOrgUnits()
        } else if (name.length >= 3) {
            fetchInitialOrgUnits(name)
        }
    }

    fun onOpenChildren(parentOrgUnitUid: String) {
        viewModelScope.launch(dispatchers.io()) {
            _treeNodes.update { openChildren(parentOrgUnitUid = parentOrgUnitUid) }
        }
    }

    private fun openChildren(
        currentList: List<OrgUnitTreeItem> = _treeNodes.value,
        parentOrgUnitUid: String
    ): List<OrgUnitTreeItem> {
        val parentIndex = currentList.indexOfFirst { it.uid == parentOrgUnitUid }
        val orgUnits = repository.childrenOrgUnits(parentOrgUnitUid)
        val treeNodes = orgUnits.map { org ->
            val hasChildren = repository.orgUnitHasChildren(org.uid())
            OrgUnitTreeItem(
                uid = org.uid(),
                label = org.displayName()!!,
                isOpen = hasChildren,
                hasChildren = hasChildren,
                selected = selectedOrgUnits.contains(org.uid()),
                level = org.level()!!,
                selectedChildrenCount = repository.countSelectedChildren(
                    org.uid(),
                    selectedOrgUnits
                ),
                canBeSelected = repository.canBeSelected(org.uid())
            )
        }
        return rebuildOrgUnitList(
            currentList = currentList,
            location = parentIndex,
            nodes = treeNodes
        )
    }

    fun onOrgUnitCheckChanged(orgUnitUid: String, isChecked: Boolean) {
        viewModelScope.launch(dispatchers.io()) {
            if (singleSelection) {
                selectedOrgUnits.clear()
            }
            if (isChecked && !selectedOrgUnits.contains(orgUnitUid)) {
                selectedOrgUnits.add(orgUnitUid)
            } else if (!isChecked && selectedOrgUnits.contains(orgUnitUid)) {
                selectedOrgUnits.remove(orgUnitUid)
            }
            val treeNodeList = treeNodes.value.map { currentTreeNode ->
                currentTreeNode.copy(
                    selected = selectedOrgUnits.contains(currentTreeNode.uid),
                    selectedChildrenCount = repository.countSelectedChildren(
                        currentTreeNode.uid,
                        selectedOrgUnits
                    )
                )
            }
            _treeNodes.update { treeNodeList }
        }
    }

    fun clearAll() {
        viewModelScope.launch(dispatchers.io()) {
            selectedOrgUnits.clear()
            val treeNodeList = treeNodes.value.map { currentTreeNode ->
                currentTreeNode.copy(
                    selected = false,
                    selectedChildrenCount = 0
                )
            }
            _treeNodes.update { treeNodeList }
        }
    }

    private fun rebuildOrgUnitList(
        currentList: List<OrgUnitTreeItem>,
        location: Int,
        nodes: List<OrgUnitTreeItem>
    ): List<OrgUnitTreeItem> {
        val nodesCopy = ArrayList(currentList)
        nodesCopy[location] = nodesCopy[location].copy(isOpen = !nodesCopy[location].isOpen)

        if (!nodesCopy[location].isOpen) {
            val level = nodesCopy[location].level
            val deleteList: MutableList<OrgUnitTreeItem> = ArrayList()
            var sameLevel = true
            for (i in location + 1 until nodesCopy.size) {
                if (sameLevel) if (nodesCopy[i].level > level) {
                    deleteList.add(nodesCopy[i])
                } else {
                    sameLevel = false
                }
            }
            nodesCopy.removeAll(deleteList)
        } else {
            nodesCopy.addAll(location + 1, nodes)
        }

        return nodesCopy
    }

    fun getOrgUnits(): List<OrganisationUnit> {
        return selectedOrgUnits.mapNotNull { uid -> repository.orgUnit(uid) }
    }
}
