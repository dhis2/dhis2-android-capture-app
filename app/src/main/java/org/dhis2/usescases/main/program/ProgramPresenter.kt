package org.dhis2.usescases.main.program

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.utils.Constants.PROGRAM_THEME
import org.dhis2.utils.filters.FilterManager
import timber.log.Timber

class ProgramPresenter internal constructor(
    private val view: ProgramView,
    private val homeRepository: HomeRepository,
    private val schedulerProvider: SchedulerProvider,
    private val preferenceProvider: PreferenceProvider,
    private val filterManager: FilterManager
) {

    var disposable: CompositeDisposable = CompositeDisposable()

    fun init() {
        val applyFiler = PublishProcessor.create<FilterManager>()

        disposable.add(
            applyFiler
                .doOnNext { Timber.tag("INIT DATA").d("NEW FILTER") }
                .switchMap { filterManager ->
                    homeRepository.programModels(
                        filterManager.periodFilters,
                        filterManager.orgUnitUidsFilters,
                        filterManager.stateFilters,
                        filterManager.assignedFilter
                    ).onErrorReturn { t ->
                        arrayListOf()
                    }
                        .mergeWith(
                            homeRepository.aggregatesModels(
                                filterManager.periodFilters,
                                filterManager.orgUnitUidsFilters,
                                filterManager.stateFilters,
                                filterManager.assignedFilter
                            ).onErrorReturn { t ->
                                arrayListOf()
                            }
                        )
                        .doOnNext { Timber.tag("INIT DATA").d("LIST READY TO BE SORTED SORTED") }
                        .flatMapIterable { data -> data }
                        .sorted { p1, p2 -> p1.title().compareTo(p2.title(), ignoreCase = true) }
                        .toList().toFlowable()
                        .subscribeOn(schedulerProvider.io())
                        .doOnNext { Timber.tag("INIT DATA").d("LIST SORTED") }
                        .onErrorReturn { arrayListOf() }
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { programs -> view.swapProgramModelData(programs) },
                    { throwable -> Timber.d(throwable) },
                    { Timber.tag("INIT DATA").d("LOADING ENDED") }
                )
        )

        disposable.add(
            filterManager.asFlowable()
                .startWith(filterManager)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    {
                        view.showFilterProgress()
                        applyFiler.onNext(filterManager)
                    },
                    { Timber.e(it) }
                )
        )

        disposable.add(
            filterManager.ouTreeFlowable()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.openOrgUnitTreeSelector() },
                    { Timber.e(it) }
                )
        )
    }

    fun onSyncStatusClick(program: ProgramViewModel) {
        view.showSyncDialog(program)
    }

    fun updateProgramQueries() {
        filterManager.publishData()
    }

    fun onItemClick(programModel: ProgramViewModel, programTheme: Int) {
        if (programTheme != -1) {
            preferenceProvider.setValue(PROGRAM_THEME, programTheme)
        } else {
            preferenceProvider.removeValue(PROGRAM_THEME)
        }

        view.navigateTo(programModel)
    }

    fun showDescription(description: String?) {
        if (!description.isNullOrEmpty()) {
            view.showDescription(description)
        }
    }

    fun showHideFilterClick() {
        view.showHideFilter()
    }

    fun clearFilterClick() {
        filterManager.clearAllFilters()
        view.clearFilters()
    }

    fun dispose() {
        disposable.clear()
    }
}
