package org.dhis2.usescases.main.program

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.ui.ThemeManager
import org.dhis2.utils.analytics.matomo.Actions.Companion.SYNC_BTN
import org.dhis2.utils.analytics.matomo.Categories.Companion.HOME
import org.dhis2.utils.analytics.matomo.Labels.Companion.CLICK_ON
import org.dhis2.utils.analytics.matomo.MatomoAnalyticsController
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import timber.log.Timber

class ProgramPresenter internal constructor(
    private val view: ProgramView,
    private val programRepository: ProgramRepository,
    private val schedulerProvider: SchedulerProvider,
    private val themeManager: ThemeManager,
    private val filterManager: FilterManager,
    private val matomoAnalyticsController: MatomoAnalyticsController
) {

    var disposable: CompositeDisposable = CompositeDisposable()

    fun init() {
        val applyFiler = PublishProcessor.create<FilterManager>()

        disposable.add(
            applyFiler
                .switchMap {
                    programRepository.programModels().onErrorReturn {
                        arrayListOf()
                    }
                        .mergeWith(
                            programRepository.aggregatesModels().onErrorReturn {
                                arrayListOf()
                            }
                        )
                        .flatMapIterable { data -> data }
                        .sorted { p1, p2 -> p1.title().compareTo(p2.title(), ignoreCase = true) }
                        .toList().toFlowable()
                        .subscribeOn(schedulerProvider.io())
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
        val programTitle = "$CLICK_ON${program.title()}"
        matomoAnalyticsController.trackEvent(HOME, SYNC_BTN, programTitle)
        view.showSyncDialog(program)
    }

    fun updateProgramQueries() {
        filterManager.publishData()
    }

    fun onItemClick(programModel: ProgramViewModel) {
        if (programModel.programType().isNotEmpty()) {
            themeManager.setProgramTheme(programModel.id())
        } else {
            themeManager.setDataSetTheme(programModel.id())
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

    fun setOrgUnitFilters(selectedOrgUnits: List<OrganisationUnit>) {
        filterManager.addOrgUnits(selectedOrgUnits)
    }
}
