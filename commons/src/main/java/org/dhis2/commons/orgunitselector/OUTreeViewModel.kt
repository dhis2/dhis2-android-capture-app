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

private const val minNumberOfOpenOrgUnits = 15

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

    private fun fetchInitialOrgUnits() {
        viewModelScope.launch(dispatchers.io()) {
            val orgUnits = repository.orgUnits().blockingGet()
            var treeNodeList = orgUnits.filter { orgUnit ->
                orgUnit.level() == orgUnits.minByOrNull { it.level()!! }?.level()
            }.map { orgUnit ->
                val hasChildren = repository.orgUnitHasChildren(orgUnit.uid())
                OrgUnitTreeItem(
                    uid = orgUnit.uid(),
                    label = orgUnit.displayName()!!,
                    isOpen = !hasChildren,
                    hasChildren = hasChildren,
                    selected = selectedOrgUnits.contains(orgUnit.uid()),
                    level = orgUnit.level()!!,
                    selectedChildrenCount = repository.countSelectedChildren(
                        orgUnit.uid(),
                        selectedOrgUnits
                    )
                )
            }

            while (!initialOrgUnitsCheck(treeNodeList)) {
                val closedOrgUnits = treeNodeList.filter { !it.isOpen && it.hasChildren }
                closedOrgUnits.forEach {
                    treeNodeList = openChildren(treeNodeList, it.uid)
                }
            }

            _treeNodes.update { treeNodeList }
        }
    }

    private fun initialOrgUnitsCheck(initialOrgUnits: List<OrgUnitTreeItem>): Boolean {
        val currentSizeCheck = initialOrgUnits.size >= minNumberOfOpenOrgUnits
        val allOpened = initialOrgUnits.all { it.isOpen }
        return currentSizeCheck || allOpened
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
        val orgUnits = repository.orgUnits(parentOrgUnitUid).blockingGet()
        val treeNodes = orgUnits.map { org ->
            val hasChildren = repository.orgUnitHasChildren(org.uid())
            OrgUnitTreeItem(
                uid = org.uid(),
                label = org.displayName()!!,
                isOpen = !hasChildren,
                hasChildren = hasChildren,
                selected = selectedOrgUnits.contains(org.uid()),
                level = org.level()!!,
                selectedChildrenCount = repository.countSelectedChildren(
                    org.uid(),
                    selectedOrgUnits
                )
            )
        }
        return rebuildOrgUnitList(
            currentList = currentList,
            location = parentIndex,
            nodes = treeNodes
        )
    }

    fun searchByName(name: String) {
        if (name.isEmpty()) {
            fetchInitialOrgUnits()
        } else {
            viewModelScope.launch(dispatchers.io()) {
                if (name.length >= 3) {
                    val orgUnits = repository.orgUnits(name = name).blockingGet()
                    val treeNodes = ArrayList<OrgUnitTreeItem>()
                    val orderedList = mutableListOf<String>()
                    orgUnits.forEach { org ->
                        org.path()?.split("/")
                            ?.filter { it.isNotEmpty() }
                            ?.forEach { str ->
                                when {
                                    !orderedList.contains(str) -> orderedList.add(str)
                                }
                            }
                        when {
                            !orderedList.contains(org.uid()) -> orderedList.add(org.uid())
                        }
                    }
                    orderedList.forEach { ouUid ->
                        val organisationUnitParent = repository.orgUnit(ouUid)
                        organisationUnitParent?.let { org ->
                            treeNodes.add(
                                OrgUnitTreeItem(
                                    uid = org.uid(),
                                    label = org.displayName()!!,
                                    isOpen = false,
                                    hasChildren = repository.orgUnitHasChildren(org.uid()),
                                    selected = selectedOrgUnits.contains(org.uid()),
                                    level = org.level()!!,
                                    selectedChildrenCount = repository.countSelectedChildren(
                                        org.uid(),
                                        selectedOrgUnits
                                    )
                                )
                            )
                        }
                    }

                    (1 until treeNodes.size)
                        .asSequence()
                        .filter { treeNodes[it].level > treeNodes[it - 1].level }
                        .forEach { treeNodes[it - 1].isOpen = true }

                    _treeNodes.update { treeNodes }
                }
            }
        }
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
