package org.dhis2.commons.orgunitselector

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import java.util.ArrayList
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.schedulers.defaultSubscribe
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

class OUTreePresenter(
    private val view: OUTreeView,
    private val repository: OUTreeRepository,
    private val schedulerProvider: SchedulerProvider,
    private val preselectedOrgUnits: List<String>
) {

    var compositeDisposable = CompositeDisposable()
    val ouChildListener = PublishProcessor.create<Pair<Int, OrganisationUnit>>()
    val onSearchListener = PublishProcessor.create<String>()
    val onStartSearch = PublishProcessor.create<Boolean>()
    val onRebuildList = PublishProcessor.create<Unit>()

    fun init() {
        compositeDisposable.add(
            onStartSearch
                .startWith(true)
                .flatMap {
                    repository.orgUnits().toFlowable()
                }.map { orgUnits ->
                    orgUnits.filter { orgUnit ->
                        orgUnit.level() == orgUnits.minByOrNull { it.level()!! }?.level()
                    }
                }.map { organisationUnits ->
                    val nodes = ArrayList<TreeNode>()
                    organisationUnits.forEach { org ->
                        nodes.add(
                            TreeNode(
                                org,
                                false,
                                repository.orgUnitHasChildren(org.uid()),
                                preselectedOrgUnits.contains(org.uid()),
                                org.level()!!,
                                repository.countSelectedChildren(org, preselectedOrgUnits)
                            )
                        )
                    }
                    nodes
                }.defaultSubscribe(
                    schedulerProvider,
                    onNext = { view.setOrgUnits(it) }
                )
        )

        compositeDisposable.add(
            ouChildListener
                .flatMap { parent ->
                    repository.orgUnits(parent.second.uid()).toFlowable()
                        .map { ouList -> Pair(parent.first, ouList) }
                }.map { organisationUnits ->
                    val nodes = ArrayList<TreeNode>()
                    organisationUnits.second.forEach { org ->
                        nodes.add(
                            TreeNode(
                                org,
                                false,
                                repository.orgUnitHasChildren(org.uid()),
                                preselectedOrgUnits.contains(org.uid()),
                                org.level()!!,
                                repository.countSelectedChildren(org, preselectedOrgUnits)
                            )
                        )
                    }
                    rebuildOrgUnitList(organisationUnits.first, nodes)
                }.defaultSubscribe(
                    schedulerProvider,
                    onNext = { view.setOrgUnits(it) }
                )
        )

        compositeDisposable.add(
            onSearchListener
                .filter { name -> name.length > 3 }
                .flatMap { name ->
                    repository.orgUnits(name = name).toFlowable()
                }.map { organisationUnits ->
                    val nodes = ArrayList<TreeNode>()
                    val orderedList = mutableListOf<String>()
                    organisationUnits.forEach { org ->
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
                        organisationUnitParent?.let {
                            nodes.add(
                                TreeNode(
                                    it,
                                    false,
                                    repository.orgUnitHasChildren(it.uid()),
                                    preselectedOrgUnits.contains(it.uid()),
                                    it.level()!!,
                                    repository.countSelectedChildren(it, preselectedOrgUnits)
                                )
                            )
                        }
                    }
                    (1 until nodes.size)
                        .asSequence()
                        .filter { nodes[it].level > nodes[it - 1].level }
                        .forEach { nodes[it - 1].isOpen = true }
                    nodes
                }.defaultSubscribe(
                    schedulerProvider,
                    onNext = { view.setOrgUnits(it) }
                )
        )

        compositeDisposable.add(
            onRebuildList
                .map {
                    view.getCurrentList().map { currentTreeNode ->
                        currentTreeNode.copy(
                            isChecked = preselectedOrgUnits.contains(currentTreeNode.content.uid()),
                            selectedChildrenCount = repository.countSelectedChildren(
                                currentTreeNode.content,
                                preselectedOrgUnits
                            )
                        )
                    }
                }
                .defaultSubscribe(
                    schedulerProvider,
                    onNext = { view.setOrgUnits(it) }
                )
        )
    }

    private fun rebuildOrgUnitList(location: Int, nodes: List<TreeNode>): List<TreeNode> {
        val nodesCopy: MutableList<TreeNode> = ArrayList<TreeNode>(view.getCurrentList())
        nodesCopy[location].isOpen = !nodesCopy[location].isOpen

        if (!nodesCopy[location].isOpen) {
            val (_, _, _, _, level) = nodesCopy[location]
            val deleteList: MutableList<TreeNode> = ArrayList()
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

    fun onDestroy() {
        compositeDisposable.clear()
    }

    fun getOrgUnits(selectedOrgUnits: MutableList<String>): List<OrganisationUnit> {
        return selectedOrgUnits.mapNotNull { uid -> repository.orgUnit(uid) }
    }

    fun rebuildCurrentList() {
        onRebuildList.onNext(Unit)
    }
}
