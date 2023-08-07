package org.dhis2.usescases.uiboost.viewmodels

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.service.SyncStatusController
import org.dhis2.usescases.main.program.IdentifyProgramType
import org.dhis2.usescases.main.program.ProgramRepository
import org.dhis2.usescases.main.program.ProgramView
import org.dhis2.usescases.main.program.StockManagementMapper
import timber.log.Timber
import javax.inject.Inject

class HomeProgramViewModel @Inject internal constructor(
    private val view: ProgramView,
    private val programRepository: ProgramRepository,
    private val schedulerProvider: SchedulerProvider,
    private val filterManager: FilterManager,
    private val matomoAnalyticsController: MatomoAnalyticsController,
    private val syncStatusController: SyncStatusController,
    private val identifyProgramType: IdentifyProgramType,
    private val stockManagementMapper: StockManagementMapper
): ViewModel() {
    private val refreshData = PublishProcessor.create<Unit>()
    var disposable: CompositeDisposable = CompositeDisposable()

    fun getPrograms() {
        val applyFiler = PublishProcessor.create<FilterManager>()
        programRepository.clearCache()

//        disposable.add(
//            applyFiler
//                .switchMap {
//                    refreshData.debounce(
//                        500,
//                        TimeUnit.MILLISECONDS,
//                        schedulerProvider.io()
//                    ).startWith(Unit).switchMap {
//                        programRepository.homeItems(
//                            syncStatusController.observeDownloadProcess().value!!
//                        )
//                    }
//                }
//                .subscribeOn(schedulerProvider.io())
//                .observeOn(schedulerProvider.ui())
//                .subscribe(
//                    { programs ->
//                        this.programs.postValue(programs)
//                        view.swapProgramModelData(programs)
//                    },
//                    { throwable -> Timber.d(throwable) },
//                    { Timber.tag("INIT DATA").d("LOADING ENDED") }
//                )
//        )
//
//        disposable.add(
//            filterManager.asFlowable()
//                .startWith(filterManager)
//                .subscribeOn(schedulerProvider.io())
//                .observeOn(schedulerProvider.ui())
//                .subscribe(
//                    {
//                        view.showFilterProgress()
//                        applyFiler.onNext(filterManager)
//                    },
//                    { Timber.e(it) }
//                )
//        )
//
//        disposable.add(
//            filterManager.ouTreeFlowable()
//                .subscribeOn(schedulerProvider.io())
//                .observeOn(schedulerProvider.ui())
//                .subscribe(
//                    { view.openOrgUnitTreeSelector() },
//                    { Timber.e(it) }
//                )
//        )
    }
}