package org.dhis2.usescases.programEventDetail

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import org.dhis2.commons.filters.DisableHomeFiltersFromSettingsApp
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.FilterManager.PeriodRequest
import org.dhis2.commons.filters.Filters
import org.dhis2.commons.filters.data.FilterRepository
import org.dhis2.commons.filters.workingLists.EventFilterToWorkingListItemMapper
import org.dhis2.commons.filters.workingLists.WorkingListItem
import org.dhis2.commons.matomo.Actions
import org.dhis2.commons.matomo.Categories
import org.dhis2.commons.matomo.Labels
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.schedulers.SchedulerProvider
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import timber.log.Timber

class ProgramEventDetailPresenter(
    private val view: ProgramEventDetailView,
    private val eventRepository: ProgramEventDetailRepository,
    private val schedulerProvider: SchedulerProvider,
    private val filterManager: FilterManager,
    private val workingListMapper: EventFilterToWorkingListItemMapper,
    private val filterRepository: FilterRepository,
    private val disableHomFilters: DisableHomeFiltersFromSettingsApp,
    private val matomoAnalyticsController: MatomoAnalyticsController,
) {
    val compositeDisposable: CompositeDisposable = CompositeDisposable()
    val program: Program?
        get() = eventRepository.program().blockingGet()
    val featureType: FeatureType?
        get() = eventRepository.featureType().blockingGet()
    val stageUid: String?
        get() = eventRepository.programStage().blockingGet()?.uid()

    fun init() {
        compositeDisposable.add(
            Observable.fromCallable {
                program?.uid()?.let { filterRepository.programFilters(it) } ?: emptyList()
            }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { filters ->
                        if (filters.isEmpty()) {
                            view.hideFilters()
                        } else {
                            view.setFilterItems(filters)
                        }
                    },
                    { t -> Timber.e(t) },
                ),
        )
        compositeDisposable.add(
            FilterManager.getInstance().catComboRequest
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { catComboUid -> view.showCatOptComboDialog(catComboUid) },
                    { t -> Timber.e(t) },
                ),
        )
        compositeDisposable.add(
            Single.just(eventRepository.getAccessDataWrite())
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { aBoolean -> view.setWritePermission(aBoolean) },
                    { t -> Timber.e(t) },
                ),
        )
        compositeDisposable.add(
            eventRepository.program()
                .observeOn(schedulerProvider.ui())
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { programModel -> view.setProgram(programModel!!) },
                    { t -> Timber.e(t) },
                ),
        )
        compositeDisposable.add(
            filterManager.ouTreeFlowable()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.openOrgUnitTreeSelector() },
                    { t -> Timber.e(t) },
                ),
        )
        compositeDisposable.add(
            filterManager.asFlowable().onBackpressureLatest()
                .doOnNext { view.showFilterProgress() }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { filterManager -> view.updateFilters(filterManager.totalFilters) },
                    { t -> Timber.e(t) },
                ),
        )
        compositeDisposable.add(
            filterManager.periodRequest
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { (first): Pair<PeriodRequest, Filters> -> view.showPeriodRequest(first) },
                    { t -> Timber.e(t) },
                ),
        )
    }

    fun onSyncIconClick(uid: String) {
        matomoAnalyticsController.trackEvent(
            Categories.EVENT_LIST,
            Actions.SYNC_EVENT,
            Labels.CLICK,
        )
        view.showSyncDialog(uid)
    }

    fun addEvent() {
        view.startNewEvent()
    }

    fun onBackClick() {
        view.back()
    }

    fun onDettach() {
        compositeDisposable.clear()
    }

    fun displayMessage(message: String) {
        view.displayMessage(message)
    }

    fun showFilter() {
        view.showHideFilter()
    }

    fun clearFilterClick() {
        filterManager.clearAllFilters()
    }

    fun filterCatOptCombo(selectedCatOptionCombo: String) {
        FilterManager.getInstance()
            .addCatOptCombo(eventRepository.getCatOptCombo(selectedCatOptionCombo))
    }

    fun workingLists(): List<WorkingListItem> {
        return eventRepository.workingLists().toFlowable()
            .flatMapIterable { data -> data }
            .map { eventFilter ->
                workingListMapper.map(eventFilter)
            }
            .toList().blockingGet()
    }

    fun clearOtherFiltersIfWebAppIsConfig() {
        val filters = filterRepository.homeFilters()
        disableHomFilters.execute(filters)
    }

    fun setOpeningFilterToNone() {
        filterRepository.collapseAllFilters()
    }

    fun setOrgUnitFilters(selectedOrgUnits: List<OrganisationUnit>) {
        FilterManager.getInstance().addOrgUnits(selectedOrgUnits)
    }

    fun trackEventProgramAnalytics() {
        matomoAnalyticsController.trackEvent(
            Categories.EVENT_LIST,
            Actions.OPEN_ANALYTICS,
            Labels.CLICK,
        )
    }

    fun trackEventProgramMap() {
        matomoAnalyticsController.trackEvent(
            Categories.EVENT_LIST,
            Actions.MAP_VISUALIZATION,
            Labels.CLICK,
        )
    }
}
