package org.dhis2.usescases.main.program

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
import org.dhis2.commons.featureconfig.model.Feature
import org.dhis2.commons.featureconfig.model.FeatureOptions
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.matomo.Actions.Companion.SYNC_BTN
import org.dhis2.commons.matomo.Categories.Companion.HOME
import org.dhis2.commons.matomo.Labels.Companion.CLICK_ON
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.service.SyncStatusController
import timber.log.Timber
import java.util.concurrent.TimeUnit

class ProgramViewModel internal constructor(
    private val view: ProgramView,
    private val programRepository: ProgramRepository,
    private val featureConfigRepository: FeatureConfigRepository,
    private val dispatchers: DispatcherProvider,
    private val matomoAnalyticsController: MatomoAnalyticsController,
    private val filterManager: FilterManager,
    private val syncStatusController: SyncStatusController,
    private val schedulerProvider: SchedulerProvider,
) : ViewModel() {
    private val _programs = MutableLiveData<List<ProgramUiModel>>()
    val programs: LiveData<List<ProgramUiModel>> = _programs
    private val refreshData = PublishProcessor.create<Unit>()
    var disposable: CompositeDisposable = CompositeDisposable()

    fun init() {
        programRepository.clearCache()
        fetchPrograms()
        initFilters()
    }

    private fun initFilters() {
        val applyFilter = PublishProcessor.create<FilterManager>()
        disposable.add(
            applyFilter
                .switchMap {
                    refreshData
                        .debounce(
                            500,
                            TimeUnit.MILLISECONDS,
                            schedulerProvider.io(),
                        ).startWith(Unit)
                        .switchMap {
                            programRepository.homeItems(
                                syncStatusController.observeDownloadProcess().value,
                            )
                        }
                }.subscribeOn(schedulerProvider.io())
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
            filterManager
                .asFlowable()
                .startWith(filterManager)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    {
                        applyFilter.onNext(filterManager)
                    },
                    { Timber.e(it) },
                ),
        )
    }

    private fun fetchPrograms() {
        viewModelScope.launch {
            val result =
                async(dispatchers.io()) {
                    val programs =
                        programRepository
                            .homeItems(
                                syncStatusController.observeDownloadProcess().value,
                            ).blockingLast()
                    if (featureConfigRepository.isFeatureEnable(Feature.RESPONSIVE_HOME)) {
                        val feature = featureConfigRepository.featuresList.find { it.feature == Feature.RESPONSIVE_HOME }
                        val totalItems =
                            feature?.extras?.takeIf { it is FeatureOptions.ResponsiveHome }?.let {
                                it as FeatureOptions.ResponsiveHome
                                it.totalItems
                            }
                        programs.take(
                            totalItems ?: programs.size,
                        )
                    } else {
                        programs
                    }
                }
            try {
                _programs.postValue(result.await())
            } catch (e: Exception) {
                Timber.d(e)
            }
        }
    }

    fun onSyncStatusClick(program: ProgramUiModel) {
        val programTitle = "$CLICK_ON${program.title}"
        matomoAnalyticsController.trackEvent(HOME, SYNC_BTN, programTitle)
        view.showSyncDialog(program)
    }

    fun updateProgramQueries() {
        init()
    }

    fun onItemClick(programModel: ProgramUiModel) {
        view.navigateTo(programModel)
    }

    fun dispose() {
        disposable.clear()
    }

    fun downloadState() = syncStatusController.observeDownloadProcess()

    fun setIsDownloading() {
        viewModelScope.launch(dispatchers.io()) {
            fetchPrograms()
        }
    }
}
