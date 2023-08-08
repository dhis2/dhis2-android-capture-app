package org.dhis2.usescases.main.program

import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.matomo.Actions.Companion.SYNC_BTN
import org.dhis2.commons.matomo.Categories.Companion.HOME
import org.dhis2.commons.matomo.Labels.Companion.CLICK_ON
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.service.SyncStatusController
import org.dhis2.usescases.uiboost.data.model.DataStoreAppConfig
import org.dhis2.usescases.uiboost.ui.model.ProgramGridUiState
import org.hisp.dhis.android.core.datastore.DataStoreEntry
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramType
import timber.log.Timber

class ProgramPresenter internal constructor(
    private val view: ProgramView,
    private val programRepository: ProgramRepository,
    private val schedulerProvider: SchedulerProvider,
    private val filterManager: FilterManager,
    private val matomoAnalyticsController: MatomoAnalyticsController,
    private val syncStatusController: SyncStatusController,
    private val identifyProgramType: IdentifyProgramType,
    private val stockManagementMapper: StockManagementMapper
) {
    private val programs = MutableLiveData<List<ProgramViewModel>>(emptyList())

    private val _dataStore = MutableStateFlow<List<DataStoreEntry>>(emptyList())
    val dataStore: StateFlow<List<DataStoreEntry>> = _dataStore

    private val _dataStoreFiltered = MutableStateFlow<DataStoreAppConfig?>(null)
    val dataStoreFiltered: StateFlow<DataStoreAppConfig?> = _dataStoreFiltered

    private val _dataStoreProgram =
        MutableStateFlow<List<org.dhis2.usescases.uiboost.data.model.Program>>(emptyList())
    val dataStoreProgram: StateFlow<List<org.dhis2.usescases.uiboost.data.model.Program>> =
        _dataStoreProgram

    private val _programsGrid = MutableStateFlow<List<ProgramViewModel>>(emptyList())
    val programsGrid: StateFlow<List<ProgramViewModel>> = _programsGrid

    private val _programsList = MutableStateFlow<List<ProgramViewModel>>(emptyList())
    val programsList: StateFlow<List<ProgramViewModel>> = _programsList

    private val _programsGridUiSate =
        MutableStateFlow(ProgramGridUiState(programsListGridUiState = emptyList()))
    val programsGridUiSate: StateFlow<ProgramGridUiState> = _programsGridUiSate

    private val programsGridArray = ArrayList<ProgramViewModel>()
    private val programsListArray = ArrayList<ProgramViewModel>()

    private val refreshData = PublishProcessor.create<Unit>()
    var disposable: CompositeDisposable = CompositeDisposable()

    private fun getStore() {
        runBlocking(Dispatchers.IO) {
            launch {
                programRepository.getDataStoreData().collectLatest {
                    _dataStore.value = (it)
                }
                programRepository.getFilteredDataStore().collectLatest {
                    _dataStoreFiltered.value = it
                }
            }
        }
    }

    fun setProgramsGrid(program: List<ProgramViewModel>) {
        _programsGrid.value = program
    }

    fun setProgramsList(program: List<ProgramViewModel>) {
        _programsList.value = program
    }

    fun getGrid(): Flow<List<ProgramViewModel>> {
        return flowOf(programsGridArray)
    }

    fun getList(): Flow<List<ProgramViewModel>> {
        return flowOf(programsListArray)
    }
    fun init() {
        val applyFiler = PublishProcessor.create<FilterManager>()
        programRepository.clearCache()

        disposable.add(
            applyFiler
                .switchMap {
                    refreshData.debounce(
                        100,
                        TimeUnit.MILLISECONDS,
                        schedulerProvider.io()
                    ).startWith(Unit).switchMap {
                        programRepository.homeItems(
                            syncStatusController.observeDownloadProcess().value!!
                        )
                    }
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { programs ->
                        this.programs.postValue(programs)
                        view.swapProgramModelData(programs)
                    },
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
        getStore()
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
        when (getHomeItemType(programModel)) {
            HomeItemType.PROGRAM_STOCK ->
                view.navigateToStockManagement(stockManagementMapper.map(programModel))

            else ->
                view.navigateTo(programModel)
        }
    }

    private fun getHomeItemType(programModel: ProgramViewModel): HomeItemType {
        return when (programModel.programType) {
            ProgramType.WITH_REGISTRATION.name -> {
                identifyProgramType(programModel.uid)
            }

            ProgramType.WITHOUT_REGISTRATION.name -> {
                HomeItemType.EVENTS
            }

            else -> {
                HomeItemType.DATA_SET
            }
        }
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

    fun programs() = programs
    fun downloadState() = syncStatusController.observeDownloadProcess()

    fun setIsDownloading() {
        refreshData.onNext(Unit)
    }
}
