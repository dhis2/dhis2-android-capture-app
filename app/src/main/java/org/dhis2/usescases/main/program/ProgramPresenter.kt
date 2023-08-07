package org.dhis2.usescases.main.program

import androidx.compose.runtime.collectAsState
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.dhis2.android.rtsm.ui.home.model.DataEntryUiState
import org.dhis2.android.rtsm.ui.home.model.SettingsUiState
import java.util.concurrent.TimeUnit
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.matomo.Actions.Companion.SYNC_BTN
import org.dhis2.commons.matomo.Categories.Companion.HOME
import org.dhis2.commons.matomo.Labels.Companion.CLICK_ON
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.service.SyncStatusController
import org.dhis2.usescases.uiboost.data.model.DataStoreAppConfig
import org.dhis2.usescases.uiboost.ui.model.DataEntryUiStateBoost
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

    val _programsTest = MutableStateFlow<List<ProgramViewModel>>(emptyList())
    val programsTest: StateFlow<List<ProgramViewModel>> = _programsTest

    private val _dataStoreProgram =
        MutableStateFlow<List<org.dhis2.usescases.uiboost.data.model.Program>>(emptyList())
    val dataStoreProgram: StateFlow<List<org.dhis2.usescases.uiboost.data.model.Program>> =
        _dataStoreProgram

    val programsGridTest = MutableLiveData<List<ProgramViewModel>>(emptyList())
    private val _programsGrid = MutableStateFlow<List<ProgramViewModel>>(emptyList())
    val programsGrid: StateFlow<List<ProgramViewModel>> = _programsGrid

    //    private val _programsList = MutableLiveData<List<ProgramViewModel>>(emptyList())
    private val _programsList = MutableStateFlow<List<ProgramViewModel>>(emptyList())
    val programsList: StateFlow<List<ProgramViewModel>> = _programsList

    private val _programsGridUiSate =
        MutableStateFlow(ProgramGridUiState(programsListGridUiState = emptyList()))
    val programsGridUiSate: StateFlow<ProgramGridUiState> = _programsGridUiSate
//    private val _settingsUiSate = MutableStateFlow(SettingsUiState(programUid = config.program))
//    val settingsUiState: StateFlow<SettingsUiState> = _settingsUiSate

    private val _dataEntryUiState = MutableStateFlow(DataEntryUiStateBoost())
    val dataEntryUiState: StateFlow<DataEntryUiStateBoost> = _dataEntryUiState


    private val programsGridArray = ArrayList<ProgramViewModel>()
    private val programsListArray = ArrayList<ProgramViewModel>()

    private val refreshData = PublishProcessor.create<Unit>()
    var disposable: CompositeDisposable = CompositeDisposable()

    private fun getStore() {
        runBlocking(Dispatchers.IO) {
            launch {
                programRepository.getDataStoreData().collectLatest {
                    _dataStore.value = (it)
                    Timber.tag("STORE").d("$it")
                }
                programRepository.getFilteredDataStore().collectLatest {

                    _dataStoreFiltered.value = it

                    val flatPrograms = it!!.programGroups.flatMap { it.programs }
                    Timber.tag("FLAT_PROGRAMS").d("$flatPrograms")

                    val items = flatPrograms.map { it }
                    Timber.tag("ITEM_PROGRAMS").d("$items")

                    _dataStoreProgram.value = items

                    items.map {
                        it
                    }


                    val mapPrograms = it.programGroups.map { it.programs }
                    Timber.tag("MAP_PROGRAMS").d("$mapPrograms")

//                    _programs2.collectLatest {
//                        Timber.tag("REAL_PROGRAMS_STATE").d("${it}")
//                    }

                }
            }
        }
        Timber.tag("REAL_PROGRAMS").d("${programs().value}")
    }

    fun getGrid(): Flow<List<ProgramViewModel>> {
        return flowOf(programsGridArray)
    }

    fun getList(): Flow<List<ProgramViewModel>> {
        return flowOf(programsListArray)
    }

    fun setProgramsByDataStore2() {
        val dataStore = DataStoreAppConfig.fromJson(_dataStore.value.getOrNull(0)?.value())
        dataStore?.let { config ->

            config.programGroups.forEach { group ->

                programs.value?.let { programsList ->

                    programsList.forEach { programViewModel ->

                        if (group.style == "GRID") {
                            val listGrid = group.programs.filter { p ->
                                p.program == programViewModel.uid
                            }
                            Timber.tag("DO").d("$listGrid")

                            group.programs.forEach { localProgram ->
                                if ((localProgram.program == programViewModel.uid) && localProgram.hidden == "false") {
                                    programsGridTest.postValue(listOf(programViewModel))
//                                    _programsGrid.value = _programsGrid.value.toMutableList() + programViewModel
                                    Timber.tag("GRID1").d("${_programsGrid.value}")
                                    _programsGridUiSate.update { currentUiState ->
                                        currentUiState.copy(
                                            programsListGridUiState = listOf(programViewModel)
                                        )
                                    }
                                    programsGridArray.add(programViewModel)
                                }
                            }
                            _programsGrid.value = programsGridArray
                        }
                        if (group.style == "LIST") {
                            group.programs.forEach { localProgram ->
                                if ((localProgram.program == programViewModel.uid) && localProgram.hidden == "false") {
                                    _programsList.value =
                                        _programsList.value.toMutableList() + programViewModel
                                    Timber.tag("LIST1").d("${_programsList.value}")
                                }
                            }
                        }
                    }

                }

            }


        }


//        programs.value?.let { programsData ->
//            programsData.forEach { programViewModel ->
//
//                dataStore?.let { config ->
//                    config.programs.forEach {
//                        if (programViewModel.uid == it.program) {
//                            if (it.programGroup == "PRIMARY") {
//                                programsGridArray.add(programViewModel)
//                                _programsGrid.value = programsGridArray
//                            }
//
//                            if (it.programGroup == "SECONDARY") {
//                                programsListArray.add(programViewModel)
//                                _programsList.value = programsListArray
//                            }
//
//                        }
//                    }
//                }
//            }
//        }
    }

    fun setProgramsByDataStore() {
        val dataStore = DataStoreAppConfig.fromJson(_dataStore.value.getOrNull(0)?.value())
        dataStore?.let { config ->
            config.programGroups.forEach { group ->

                programs.value?.let { programsList ->

                    programsList.forEach { programViewModel ->

                        if (group.style == "GRID") {
                            group.programs.forEach { localProgram ->
                                if ((localProgram.program == programViewModel.uid) && localProgram.hidden == "false") {
                                    programsGridTest.postValue(listOf(programViewModel))
//                                    _programsGrid.value = _programsGrid.value.toMutableList() + programViewModel
                                    Timber.tag("GRID1").d("${_programsGrid.value}")
                                    _programsGridUiSate.update { currentUiState ->
                                        currentUiState.copy(
                                            programsListGridUiState = listOf(programViewModel)
                                        )
                                    }
                                    programsGridArray.add(programViewModel)
                                }
                            }
                            _programsGrid.value = programsGridArray
                        }
                        if (group.style == "LIST") {
                            group.programs.forEach { localProgram ->
                                if ((localProgram.program == programViewModel.uid) && localProgram.hidden == "false") {
                                    _programsList.value =
                                        _programsList.value.toMutableList() + programViewModel
                                    Timber.tag("LIST1").d("${_programsList.value}")
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    fun init() {
        val applyFiler = PublishProcessor.create<FilterManager>()
        programRepository.clearCache()

        disposable.add(
            applyFiler
                .switchMap {
                    refreshData.debounce(
                        500,
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
        setProgramsByDataStore()
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
    fun programsList() = _programsList
    fun programsGrid() = _programsGrid

    fun downloadState() = syncStatusController.observeDownloadProcess()

    fun setIsDownloading() {
        refreshData.onNext(Unit)
    }
}
