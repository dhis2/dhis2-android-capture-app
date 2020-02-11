package org.dhis2.usescases.orgunitselector

import com.mapbox.mapboxsdk.Mapbox.getApplicationContext
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import org.dhis2.App
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.utils.filters.FilterManager
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import timber.log.Timber
import java.util.ArrayList

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

        val d2 = (getApplicationContext() as App).serverComponent()!!.userManager().d2

        compositeDisposable.add(
            onStartSearch
                .flatMap {
                    repository.orgUnits().toFlowable()
                }.map { list ->
                            var minLevel = list[0].level()
                            for (ou in list)
                                minLevel = if (ou.level()!! < minLevel!!) ou.level() else minLevel
                            val it = list.iterator()
                            while (it.hasNext()) {
                                if (it.next().level()!! > minLevel!!)
                                    it.remove()
                            }
                            list
                        }
                        .map { organisationUnits ->
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
                .subscribe({
                        organisationUnits -> view.setOrgUnits(organisationUnits)
                }, { Timber.e(it) }
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
                .subscribe({
                        organisationUnits ->
                    view.addOrgUnits(organisationUnits.first, organisationUnits.second)
                }, { Timber.e(it) }
                )
        )

        compositeDisposable.add(
            onSearchListener
                .filter { name -> name.length > 3 }
                .flatMap { name ->
                    repository.orgUnits(name = name).toFlowable()
                }.map { list ->
                    var minLevel = list[0].level()
                    for (ou in list)
                        minLevel = if (ou.level()!! < minLevel!!) ou.level() else minLevel
                    val it = list.iterator()
                    while (it.hasNext()) {
                        if (it.next().level()!! > minLevel!!)
                            it.remove()
                    }
                    list
                }
                .map { organisationUnits ->
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
                .subscribe({
                        organisationUnits -> view.setOrgUnits(organisationUnits)
                }, { Timber.e(it) }
                )
        )
    }

    fun onDestroy() {
        compositeDisposable.clear()
    }
}