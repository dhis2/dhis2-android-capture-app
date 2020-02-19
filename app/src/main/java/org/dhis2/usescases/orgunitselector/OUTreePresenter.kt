package org.dhis2.usescases.orgunitselector

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import java.util.ArrayList
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.utils.filters.FilterManager
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import timber.log.Timber

class OUTreePresenter(
    private val view: OUTreeView,
    private val repository: OUTreeRepository,
    private val schedulerProvider: SchedulerProvider,
    private val filterManager: FilterManager
) {

    var compositeDisposable = CompositeDisposable()
    val ouChildListener = PublishProcessor.create<Pair<Int, OrganisationUnit>>()
    val onSearchListener = PublishProcessor.create<String>()
    val onStartSearch = PublishProcessor.create<Boolean>()

    fun init() {
        compositeDisposable.add(
            onStartSearch
                .startWith(true)
                .flatMap {
                    repository.orgUnits().toFlowable()
                }.map { orgUnits ->
                    orgUnits.filter { orgUnit ->
                        orgUnit.level() == orgUnits.minBy { it.level()!! }?.level()
                    }
                }.map { organisationUnits ->
                    val nodes = ArrayList<TreeNode>()
                    organisationUnits.forEach { org ->
                        nodes.add(
                            TreeNode(
                                org,
                                false,
                                repository.orgUnitHasChildren(org.uid()),
                                filterManager.orgUnitFilters.contains(org),
                                org.level()!!
                            )
                        )
                    }
                    nodes
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    {
                        organisationUnits ->
                        view.setOrgUnits(organisationUnits)
                    },
                    { Timber.e(it) }
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
                                filterManager.orgUnitFilters.contains(org),
                                org.level()!!
                            )
                        )
                    }
                    Pair(organisationUnits.first, nodes)
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    {
                        organisationUnits ->
                        view.addOrgUnits(organisationUnits.first, organisationUnits.second)
                    },
                    { Timber.e(it) }
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
                                    filterManager.orgUnitFilters.contains(it),
                                    it.level()!!
                                )
                            )
                        }
                    }
                    (1 until nodes.size)
                        .asSequence()
                        .filter { nodes[it].level > nodes[it - 1].level }
                        .forEach { nodes[it - 1].isOpen = true }
                    nodes
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    {
                        organisationUnits ->
                        view.setOrgUnits(organisationUnits)
                    },
                    { Timber.e(it) }
                )
        )
    }

    fun onDestroy() {
        compositeDisposable.clear()
    }
}
