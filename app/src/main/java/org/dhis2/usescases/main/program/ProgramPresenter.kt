package org.dhis2.usescases.main.program

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.matomo.Actions.Companion.SYNC_BTN
import org.dhis2.commons.matomo.Categories.Companion.HOME
import org.dhis2.commons.matomo.Labels.Companion.CLICK_ON
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.service.SyncStatusController
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import timber.log.Timber
import java.util.concurrent.TimeUnit

class ProgramPresenter internal constructor(
    private val view: ProgramView,
    private val programRepository: ProgramRepository,
    private val schedulerProvider: SchedulerProvider,
    private val filterManager: FilterManager,
    private val matomoAnalyticsController: MatomoAnalyticsController,
    private val syncStatusController: SyncStatusController,
) {

    private val _programs = MutableLiveData<List<ProgramViewModel>>()
    val programs: LiveData<List<ProgramViewModel>> = _programs
    private val refreshData = PublishProcessor.create<Unit>()
    var disposable: CompositeDisposable = CompositeDisposable()

    fun init() {
        val applyFiler = PublishProcessor.create<FilterManager>()
        programRepository.clearCache()

        disposable.add(
            applyFiler
                .switchMap {
                    refreshData.debounce(
                        500,
                        TimeUnit.MILLISECONDS,
                        schedulerProvider.io(),
                    ).startWith(Unit).switchMap {
                        programRepository.homeItems(
                            syncStatusController.observeDownloadProcess().value!!,
                        )
                    }
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { programs ->
                        _programs.postValue(programs)
                    },
                    { throwable -> Timber.d(throwable) },
                    { Timber.tag("INIT DATA").d("LOADING ENDED") },
                ),
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
                    { Timber.e(it) },
                ),
        )

        disposable.add(
            filterManager.ouTreeFlowable()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.openOrgUnitTreeSelector() },
                    { Timber.e(it) },
                ),
        )
    }

    fun onSyncStatusClick(program: ProgramViewModel) {
        val programTitle = "$CLICK_ON${program.title}"
        matomoAnalyticsController.trackEvent(HOME, SYNC_BTN, programTitle)
        view.showSyncDialog(program)
    }

    fun updateProgramQueries() {
        programRepository.clearCache()
        filterManager.publishData()
    }

    fun onItemClick(programModel: ProgramViewModel) {
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

    fun downloadState() = syncStatusController.observeDownloadProcess()

    fun setIsDownloading() {
        refreshData.onNext(Unit)
    }
}
