package org.dhis2.usescases.searchTrackEntity

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Map
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dhis2.R
import org.dhis2.commons.extensions.toFriendlyDate
import org.dhis2.commons.extensions.toFriendlyDateTime
import org.dhis2.commons.extensions.toPercentage
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.form.ui.customintent.CustomIntentResult
import org.dhis2.form.ui.provider.DisplayNameProvider
import org.dhis2.maps.extensions.toStringProperty
import org.dhis2.maps.layer.MapLayer
import org.dhis2.maps.layer.basemaps.BaseMapStyle
import org.dhis2.maps.managers.MapManager
import org.dhis2.maps.usecases.MapStyleConfiguration
import org.dhis2.mobile.commons.coroutine.CoroutineTracker
import org.dhis2.tracker.NavigationBarUIState
import org.dhis2.tracker.input.model.TrackerInputType
import org.dhis2.tracker.input.ui.action.CustomIntentUid
import org.dhis2.tracker.input.ui.action.FieldUid
import org.dhis2.tracker.input.ui.action.TrackerInputAction
import org.dhis2.tracker.input.ui.mapper.toTrackerInputUiState
import org.dhis2.tracker.input.ui.state.TrackerInputUiState
import org.dhis2.tracker.input.ui.state.TrackerOptionItem
import org.dhis2.tracker.search.data.transformDomainTeiToSDKTei
import org.dhis2.tracker.search.domain.FetchOptionSetOptions
import org.dhis2.tracker.search.domain.FetchSearchParameters
import org.dhis2.tracker.search.domain.SearchTrackedEntities
import org.dhis2.tracker.search.model.FetchSearchParametersData
import org.dhis2.tracker.search.model.QueryData
import org.dhis2.tracker.search.model.SearchParametersUiState
import org.dhis2.tracker.search.model.SearchTrackedEntitiesInput
import org.dhis2.usescases.searchTrackEntity.listView.SearchResult
import org.dhis2.usescases.searchTrackEntity.ui.UnableToSearchOutsideData
import org.dhis2.utils.customviews.navigationbar.NavigationPage
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.hisp.dhis.mobile.ui.designsystem.component.navigationBar.NavigationBarItem
import org.maplibre.geojson.Feature
import timber.log.Timber

const val TEI_TYPE_SEARCH_MAX_RESULTS = 5

class SearchTEIViewModel(
    val initialProgramUid: String?,
    initialQuery: MutableMap<String, List<String>?>?,
    private val searchRepository: SearchRepository,
    private val searchRepositoryKt: SearchRepositoryKt,
    private val searchNavPageConfigurator: SearchPageConfigurator,
    private val mapDataRepository: MapDataRepository,
    private val networkUtils: NetworkUtils,
    private val dispatchers: DispatcherProvider,
    private val mapStyleConfig: MapStyleConfiguration,
    private val resourceManager: ResourceManager,
    private val displayNameProvider: DisplayNameProvider,
    private val filterManager: FilterManager,
    private val searchTrackedEntities: SearchTrackedEntities,
    private val fetchSearchParameters: FetchSearchParameters,
    private val fetchOptionSetOptions: FetchOptionSetOptions,
) : ViewModel() {
    private var layersVisibility: Map<String, MapLayer> = emptyMap()

    // Store option set flows per field UID
    private val optionSetFlows = mutableMapOf<String, Flow<PagingData<TrackerOptionItem>>>()

    // Store search query states for option sets
    private val optionSetSearchQueries = mutableMapOf<String, MutableStateFlow<String?>>()

    private val pageConfiguration = MutableLiveData<NavigationPageConfigurator>()

    private val _navigationBarUIState =
        mutableStateOf(
            NavigationBarUIState<NavigationPage>(),
        )
    val navigationBarUIState: MutableState<NavigationBarUIState<NavigationPage>> =
        _navigationBarUIState

    private val _legacyInteraction = MutableLiveData<LegacyInteraction?>()
    val legacyInteraction: LiveData<LegacyInteraction?> = _legacyInteraction

    private val _refreshData = MutableLiveData(Unit)
    val refreshData: LiveData<Unit> = _refreshData

    private val _mapResults = Channel<TrackerMapData>()
    val mapResults: Flow<TrackerMapData> = _mapResults.receiveAsFlow()

    private val _mapItemClicked = MutableSharedFlow<String>()
    val mapItemClicked: Flow<String> = _mapItemClicked

    private val _screenState = MutableLiveData<SearchTEScreenState>()
    val screenState: LiveData<SearchTEScreenState> = _screenState

    val createButtonScrollVisibility = MutableLiveData(false)
    val isScrollingDown = MutableLiveData(false)

    private var searching: Boolean = false
    private val filtersActive = MutableLiveData(false)

    private val _downloadResult = MutableLiveData<TeiDownloadResult>()
    val downloadResult: LiveData<TeiDownloadResult> = _downloadResult

    private val _dataResult = MutableLiveData<List<SearchResult>>()
    val dataResult: LiveData<List<SearchResult>> = _dataResult

    private val _filtersOpened = MutableLiveData(false)
    val filtersOpened: LiveData<Boolean> = _filtersOpened

    private val _backdropActive = MutableLiveData<Boolean>()
    val backdropActive: LiveData<Boolean> get() = _backdropActive

    private val _teTypeName = MutableLiveData("")
    val teTypeName: LiveData<String> = _teTypeName

    var searchParametersUiState by mutableStateOf(SearchParametersUiState())

    val queryDataList =
        mutableListOf<QueryData>().apply {
            initialQuery?.let { addAll(it.toQueryDataList(searchParametersUiState.items)) }
        }

    var mapManager: MapManager? = null

    private var fetchJob: Job? = null

    private val onNewSearch = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private val _searchActions = Channel<TrackerInputAction>()
    val searchActions = _searchActions.receiveAsFlow()

    val searchPagingData =
        onNewSearch
            .onStart { emit(Unit) }
            .flatMapLatest {
                flow {
                    CoroutineTracker.increment()
                    emitAll(
                        when {
                            searching -> loadSearchResults()
                            displayFrontPageList() -> loadDisplayInListResults()
                            else -> emptyFlow()
                        },
                    )
                    CoroutineTracker.decrement()
                }
            }.flowOn(dispatchers.io())
            .cachedIn(viewModelScope)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PagingData.empty())

    init {
        viewModelScope.launch(dispatchers.io()) {
            createButtonScrollVisibility.postValue(
                searchRepository.canCreateInProgramWithoutSearch(),
            )
            loadNavigationBarItems()

            _teTypeName.postValue(
                searchRepository.trackedEntityType.displayName(),
            )
        }
    }

    /**
     * Get or create option set flow for a given field.
     */
    fun getOptionSetFlow(
        fieldUid: String,
        optionSetUid: String,
    ): Flow<PagingData<TrackerOptionItem>> =
        optionSetFlows.getOrPut(fieldUid) {
            flow {
                val searchQuery =
                    optionSetSearchQueries.getOrPut(fieldUid) {
                        MutableStateFlow(null)
                    }

                searchQuery.collect { query ->
                    val result =
                        fetchOptionSetOptions(
                            FetchOptionSetOptions.Params(
                                optionSetUid = optionSetUid,
                                pageSize = 10,
                                searchQuery = query,
                            ),
                        )

                    result.fold(
                        onSuccess = { optionsFlow ->
                            emitAll(optionsFlow)
                        },
                        onFailure = {
                            emit(PagingData.empty())
                        },
                    )
                }
            }.cachedIn(viewModelScope)
        }

    /**
     * Handle search in option sets.
     */
    fun onOptionSetSearch(
        fieldUid: String,
        query: String,
    ) {
        optionSetSearchQueries[fieldUid]?.value = query.takeIf { it.isNotBlank() }
    }

    private fun loadNavigationBarItems() {
        CoroutineTracker.increment()
        val pageConfigurator = searchNavPageConfigurator.initVariables()
        pageConfiguration.postValue(pageConfigurator)

        val enrollmentItems = mutableListOf<NavigationBarItem<NavigationPage>>()

        if (pageConfigurator.displayListView()) {
            enrollmentItems.add(
                NavigationBarItem(
                    id = NavigationPage.LIST_VIEW,
                    icon = Icons.AutoMirrored.Outlined.List,
                    selectedIcon = Icons.AutoMirrored.Filled.List,
                    label = resourceManager.getString(R.string.navigation_list_view),
                ),
            )
        }

        if (pageConfigurator.displayMapView()) {
            enrollmentItems.add(
                NavigationBarItem(
                    id = NavigationPage.MAP_VIEW,
                    icon = Icons.Outlined.Map,
                    selectedIcon = Icons.Filled.Map,
                    label = resourceManager.getString(R.string.navigation_map_view),
                ),
            )
        }

        if (pageConfigurator.displayAnalytics()) {
            enrollmentItems.add(
                NavigationBarItem(
                    id = NavigationPage.ANALYTICS,
                    icon = Icons.Outlined.BarChart,
                    selectedIcon = Icons.Filled.BarChart,
                    label = resourceManager.getString(R.string.navigation_charts),
                ),
            )
        }

        _navigationBarUIState.value =
            _navigationBarUIState.value.copy(
                items = enrollmentItems.takeIf { it.size > 1 }.orEmpty(),
                selectedItem = enrollmentItems.firstOrNull()?.id,
            )

        if (enrollmentItems.isNotEmpty()) {
            onNavigationPageChanged(enrollmentItems.first().id)
        }
        CoroutineTracker.decrement()
    }

    fun onNavigationPageChanged(page: NavigationPage) {
        _navigationBarUIState.value = _navigationBarUIState.value.copy(selectedItem = page)
    }

    fun setListScreen() {
        _screenState.value.takeIf { it?.screenState == SearchScreenState.MAP }?.let {
            searching = (it as SearchList).isSearching
        }
        val displayFrontPageList =
            searchRepository.getProgram(initialProgramUid)?.displayFrontPageList() ?: true
        val shouldOpenSearch =
            !displayFrontPageList &&
                !searchRepository.canCreateInProgramWithoutSearch() &&
                !searching &&
                filtersActive.value == false

        createButtonScrollVisibility.postValue(
            if (searching) {
                true
            } else {
                searchRepository.canCreateInProgramWithoutSearch()
            },
        )
        _screenState.postValue(
            SearchList(
                previousSate = _screenState.value?.screenState ?: SearchScreenState.NONE,
                listType = SearchScreenState.LIST,
                displayFrontPageList =
                    searchRepository
                        .getProgram(initialProgramUid)
                        ?.displayFrontPageList() == true,
                canCreateWithoutSearch = searchRepository.canCreateInProgramWithoutSearch(),
                isSearching = searching,
                searchForm =
                    SearchForm(
                        queryHasData = queryDataList.isNotEmpty(),
                        minAttributesToSearch =
                            searchRepository
                                .getProgram(initialProgramUid)
                                ?.minAttributesRequiredToSearch()
                                ?: 1,
                        isForced = shouldOpenSearch,
                        isOpened = shouldOpenSearch,
                    ),
                searchFilters =
                    SearchFilters(
                        hasActiveFilters = hasActiveFilters(),
                        isOpened = filterIsOpen(),
                    ),
            ),
        )
    }

    private fun hasActiveFilters() = filtersActive.value == true

    fun setMapScreen() {
        _screenState.value.takeIf { it?.screenState == SearchScreenState.LIST }?.let {
            searching = (it as SearchList).isSearching
        }
        _screenState.postValue(
            SearchList(
                previousSate = _screenState.value?.screenState ?: SearchScreenState.NONE,
                listType = SearchScreenState.MAP,
                displayFrontPageList =
                    searchRepository
                        .getProgram(initialProgramUid)
                        ?.displayFrontPageList()
                        ?: false,
                canCreateWithoutSearch = searchRepository.canCreateInProgramWithoutSearch(),
                isSearching = searching,
                searchForm =
                    SearchForm(
                        queryHasData = queryDataList.isNotEmpty(),
                        minAttributesToSearch =
                            searchRepository
                                .getProgram(initialProgramUid)
                                ?.minAttributesRequiredToSearch()
                                ?: 1,
                        isForced = false,
                        isOpened = false,
                    ),
                searchFilters =
                    SearchFilters(
                        hasActiveFilters = hasActiveFilters(),
                        isOpened = filterIsOpen(),
                    ),
            ),
        )
    }

    fun setAnalyticsScreen() {
        _screenState.postValue(
            SearchAnalytics(
                previousSate = _screenState.value?.screenState ?: SearchScreenState.NONE,
            ),
        )
    }

    fun setSearchScreen() {
        _screenState.postValue(
            SearchList(
                previousSate = _screenState.value?.screenState ?: SearchScreenState.NONE,
                listType = _screenState.value?.screenState ?: SearchScreenState.LIST,
                displayFrontPageList =
                    searchRepository
                        .getProgram(initialProgramUid)
                        ?.displayFrontPageList()
                        ?: false,
                canCreateWithoutSearch = searchRepository.canCreateInProgramWithoutSearch(),
                isSearching = searching,
                searchForm =
                    SearchForm(
                        queryHasData = queryDataList.isNotEmpty(),
                        minAttributesToSearch =
                            searchRepository
                                .getProgram(initialProgramUid)
                                ?.minAttributesRequiredToSearch()
                                ?: 1,
                        isForced = false,
                        isOpened = true,
                    ),
                searchFilters =
                    SearchFilters(
                        hasActiveFilters = hasActiveFilters(),
                        isOpened = false,
                    ),
            ),
        )
    }

    fun setPreviousScreen() {
        when (_screenState.value?.previousSate) {
            SearchScreenState.LIST -> setListScreen()
            SearchScreenState.MAP -> setMapScreen()
            SearchScreenState.ANALYTICS -> setAnalyticsScreen()
            else -> {
                // no-op
            }
        }
    }

    fun updateActiveFilters(filters: Boolean) {
        if (filtersActive.value != filters) searchRepository.clearFetchedList()
        filtersActive.postValue(filters)
    }

    fun refreshData() {
        performSearch()
    }

    private fun updateQuery(
        uid: String,
        values: List<String>?,
    ) {
        if (values.isNullOrEmpty()) {
            queryDataList.removeIf { it.attributeId == uid }
        } else {
            if (queryDataList.none { it.attributeId == uid }) {
                queryDataList.add(
                    QueryData(
                        attributeId = uid,
                        values = values,
                        searchOperator = searchParametersUiState.items.firstOrNull { it.uid == uid }?.searchOperator,
                    ),
                )
            } else {
                queryDataList.firstOrNull { it.attributeId == uid }?.copy(
                    values = values,
                )
            }
        }

        updateSearchParameters(uid, values)
        updateSearch()
    }

    private fun updateSearchParameters(
        uid: String,
        values: List<String>?,
        errorMessage: String? = null,
    ) {
        val updatedItems =
            searchParametersUiState.items.map {
                if (it.uid == uid) {
                    it.copy(
                        value = values?.joinToString(","),
                        displayName =
                            displayNameProvider.provideDisplayName(
                                valueType = searchRepositoryKt.trackerValueTypeToSDKValueType(it.valueType),
                                value = values?.joinToString(","),
                                optionSet = it.optionSet,
                                periodType = null,
                            ),
                        error = errorMessage,
                    )
                } else {
                    it
                }
            }
        searchParametersUiState = searchParametersUiState.copy(items = updatedItems)
    }

    fun clearQueryData() {
        queryDataList.clear()
        clearSearchParameters()
        updateSearch()
        performSearch()
    }

    private fun clearSearchParameters() {
        val updatedItems =
            searchParametersUiState.items.map {
                it.copy(value = null, displayName = null)
            }
        searchParametersUiState =
            searchParametersUiState.copy(
                items = updatedItems,
                searchedItems = mapOf(),
            )
        searching = false
    }

    private fun updateSearch() {
        if (_screenState.value is SearchList) {
            val currentSearchList = _screenState.value as SearchList
            _screenState.postValue(
                currentSearchList.copy(
                    searchForm =
                        currentSearchList.searchForm.copy(
                            queryHasData = queryDataList.isNotEmpty(),
                        ),
                ),
            )
        }
        searchParametersUiState =
            searchParametersUiState.copy(searchEnabled = queryDataList.isNotEmpty())
    }

    private fun loadSearchResults(): Flow<PagingData<SearchTeiModel>> =
        flow {
            // get uids to exclude for possible duplicates
            val excludeValues = searchRepositoryKt.getExcludeValues()

            val isOnline = searching && networkUtils.isOnline()
            val selectedProgram = searchRepository.getProgram(initialProgramUid)

            val allowCache =
                searchRepositoryKt.saveSearchValuesAndGetAllowCache(
                    queryDataAsMap(),
                    selectedProgram?.uid(),
                )
            val newTrackerSearchModel =
                SearchTrackedEntitiesInput(
                    selectedProgram = selectedProgram?.uid(),
                    allowCache = allowCache,
                    excludeValues = excludeValues,
                    hasStateFilters = filterManager.stateFilters.isNotEmpty(),
                    isOnline = isOnline,
                    queryDataList = queryDataList,
                )
            val results = searchTrackedEntities.invoke(newTrackerSearchModel)

            emitAll(
                results.getOrThrow().map { pagingData ->
                    pagingData.map { item ->
                        withContext(dispatchers.io()) {
                            // TODO Create a new SearchTeiModel that does not use
                            // SDK objects and remove this mapping from the domain model back to the SDK one
                            val sdkTei = transformDomainTeiToSDKTei(item)
                            val searchOnline =
                                isOnline &&
                                    filterManager.stateFilters.isEmpty()
                            searchRepository.transform(
                                sdkTei,
                                selectedProgram,
                                !searchOnline,
                                filterManager.sortingItem,
                            )
                        }
                    }
                },
            )
        }

    private fun loadDisplayInListResults(): Flow<PagingData<SearchTeiModel>> =
        flow {
            val excludeValues = searchRepositoryKt.getExcludeValues()
            val selectedProgram = searchRepository.getProgram(initialProgramUid)

            val allowCache =
                searchRepositoryKt.saveSearchValuesAndGetAllowCache(
                    queryDataAsMap(),
                    selectedProgram?.uid(),
                )
            val newTrackerSearchModel =
                SearchTrackedEntitiesInput(
                    selectedProgram = selectedProgram?.uid(),
                    allowCache = allowCache,
                    excludeValues = excludeValues,
                    hasStateFilters = filterManager.stateFilters.isNotEmpty(),
                    isOnline = false,
                    queryDataList = queryDataList,
                )
            val results = searchTrackedEntities.invoke(newTrackerSearchModel)

            emitAll(
                results.getOrThrow().map { pagingData ->
                    pagingData.map { item ->
                        withContext(dispatchers.io()) {
                            // TODO Create a new SearchTeiModel that does not use
                            // SDK objects and remove this mapping from the domain model back to the SDK one
                            val sdkTei = transformDomainTeiToSDKTei(item)
                            searchRepository.transform(
                                sdkTei,
                                selectedProgram,
                                true,
                                filterManager.sortingItem,
                            )
                        }
                    }
                },
            )
        }

    fun fetchGlobalResults(): Flow<PagingData<SearchTeiModel>>? {
        // get uids to exclude for possible duplicates
        return if (searching) {
            flow {
                val excludeValues = searchRepositoryKt.getExcludeValues()

                val isOnline = searching && networkUtils.isOnline()
                val selectedProgram = searchRepository.getProgram(initialProgramUid)

                val allowCache =
                    searchRepositoryKt.saveSearchValuesAndGetAllowCache(
                        queryDataAsMap(),
                        selectedProgram?.uid(),
                    )
                val newTrackerSearchModel =
                    SearchTrackedEntitiesInput(
                        selectedProgram = null,
                        allowCache = allowCache,
                        excludeValues = excludeValues,
                        hasStateFilters = filterManager.stateFilters.isNotEmpty(),
                        isOnline = isOnline,
                        queryDataList = queryDataList,
                    )
                val results = searchTrackedEntities.invoke(newTrackerSearchModel)

                emitAll(
                    results.getOrThrow().map { pagingData ->
                        pagingData.map { item ->
                            withContext(dispatchers.io()) {
                                // TODO Create a new SearchTeiModel that does not use
                                // SDK objects and remove this mapping from the domain model back to the SDK one
                                val sdkTei = transformDomainTeiToSDKTei(item)
                                val searchOnline =
                                    isOnline &&
                                        filterManager.stateFilters.isEmpty()
                                searchRepository.transform(
                                    sdkTei,
                                    selectedProgram,
                                    !searchOnline,
                                    filterManager.sortingItem,
                                )
                            }
                        }
                    },
                )
            }
        } else {
            null
        }
    }

    fun fetchMapResults() {
        CoroutineTracker.increment()
        viewModelScope.launch(dispatchers.io()) {
            try {
                val data =
                    mapDataRepository.getTrackerMapData(
                        searchRepository.getProgram(initialProgramUid),
                        queryDataAsMap(),
                        layersVisibility,
                    )
                _mapResults.send(data)
            } catch (e: Exception) {
                Timber.e(e)
            } finally {
                searching = false
                CoroutineTracker.decrement()
            }
        }
    }

    fun onSearch() {
        searchRepository.clearFetchedList()
        performSearch()
    }

    private fun performSearch() {
        viewModelScope.launch(dispatchers.io()) {
            try {
                if (canPerformSearch()) {
                    searching = queryDataList.isNotEmpty()
                    searchParametersUiState =
                        searchParametersUiState.copy(
                            clearSearchEnabled = queryDataList.isNotEmpty(),
                            searchedItems = getFriendlyQueryData(),
                        )

                    when (_screenState.value?.screenState) {
                        SearchScreenState.LIST -> {
                            setListScreen()
                            onNewSearch.emit(Unit)
                        }

                        SearchScreenState.MAP -> {
                            _refreshData.postValue(Unit)
                            setMapScreen()
                            fetchMapResults()
                        }

                        else -> searching = false
                    }
                } else {
                    val minAttributesToSearch =
                        searchRepository
                            .getProgram(initialProgramUid)
                            ?.minAttributesRequiredToSearch()
                            ?: 0
                    val message =
                        resourceManager.getString(
                            R.string.search_min_num_attr,
                            minAttributesToSearch,
                        )
                    searchParametersUiState =
                        searchParametersUiState.copy(minAttributesMessage = message)
                    searchParametersUiState.updateMinAttributeWarning(true)
                    setSearchScreen()
                    _refreshData.postValue(Unit)
                    onNewSearch.emit(Unit)
                }
            } catch (e: Exception) {
                Timber.d(e)
            }
        }
    }

    private fun canPerformSearch(): Boolean = minAttributesToSearchCheck() || displayFrontPageList()

    private fun minAttributesToSearchCheck(): Boolean =
        searchRepository.getProgram(initialProgramUid)?.let { program ->
            (program.minAttributesRequiredToSearch() ?: 0) <= queryDataList.size
        } ?: true

    private fun displayFrontPageList(): Boolean =
        searchRepository.getProgram(initialProgramUid)?.let { program ->
            program.displayFrontPageList() == true && queryDataList.isEmpty()
        } ?: false

    private fun canDisplayResult(
        itemCount: Int,
        onlineTooManyResults: Boolean,
    ): Boolean =
        !onlineTooManyResults &&
            when (initialProgramUid) {
                null -> itemCount <= TEI_TYPE_SEARCH_MAX_RESULTS
                else ->
                    searchRepository
                        .getProgram(initialProgramUid)
                        ?.maxTeiCountToReturn()
                        ?.takeIf { it != 0 }
                        ?.let { maxTeiCount ->
                            itemCount <= maxTeiCount
                        } ?: true
            }

    fun queryDataByProgram(programUid: String?): MutableMap<String, List<String>> =
        searchRepository.filterQueryForProgram(queryDataAsMap(), programUid)

    fun onEnrollClick() {
        _legacyInteraction.postValue(LegacyInteraction.OnEnrollClick(queryDataAsMap()))
    }

    fun onAddRelationship(
        teiUid: String,
        relationshipTypeUid: String?,
        online: Boolean,
    ) {
        _legacyInteraction.postValue(
            LegacyInteraction.OnAddRelationship(
                teiUid,
                relationshipTypeUid,
                online,
            ),
        )
    }

    fun onSyncIconClick(teiUid: String) {
        _legacyInteraction.postValue(LegacyInteraction.OnSyncIconClick(teiUid))
    }

    fun onDownloadTei(
        teiUid: String,
        enrollmentUid: String?,
        reason: String? = null,
    ) {
        viewModelScope.launch {
            val result =
                async(dispatchers.io()) {
                    searchRepository.download(teiUid, enrollmentUid, reason)
                }
            try {
                val downloadResult = result.await()
                if (downloadResult is TeiDownloadResult.TeiToEnroll) {
                    _legacyInteraction.postValue(
                        LegacyInteraction.OnEnroll(
                            initialProgramUid,
                            downloadResult.teiUid,
                            queryDataAsMap(),
                        ),
                    )
                } else {
                    _downloadResult.postValue(downloadResult)
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun onTeiClick(
        teiUid: String,
        enrollmentUid: String?,
        online: Boolean,
    ) {
        _legacyInteraction.postValue(
            LegacyInteraction.OnTeiClick(teiUid, enrollmentUid, online),
        )
    }

    fun onDataLoaded(
        programResultCount: Int,
        globalResultCount: Int? = null,
        onlineErrorCode: D2ErrorCode? = null,
    ) {
        val canDisplayResults =
            canDisplayResult(
                programResultCount,
                onlineErrorCode == D2ErrorCode.MAX_TEI_COUNT_REACHED,
            )
        val hasProgramResults = programResultCount > 0
        val hasGlobalResults = globalResultCount?.let { it > 0 }

        val isSearching =
            _screenState.value?.takeIf { it is SearchList }?.let {
                (it as SearchList).isSearching
            } ?: false

        if (isSearching) {
            handleSearchResult(
                canDisplayResults,
                hasProgramResults,
                hasGlobalResults,
            )
        } else if (displayFrontPageList()) {
            handleDisplayInListResult(hasProgramResults)
        } else {
            handleInitWithoutData()
        }
    }

    private fun handleDisplayInListResult(hasProgramResults: Boolean) {
        val result =
            when {
                !hasProgramResults && searchRepository.canCreateInProgramWithoutSearch() ->
                    listOf(
                        SearchResult(
                            SearchResult.SearchResultType.SEARCH_OR_CREATE,
                            searchRepository.trackedEntityType.displayName(),
                        ),
                    )

                else -> listOf(SearchResult(SearchResult.SearchResultType.NO_MORE_RESULTS_OFFLINE))
            }

        if (result.isEmpty() && filtersActive.value == false) {
            setSearchScreen()
        }

        _dataResult.postValue(result)
    }

    private fun handleSearchResult(
        canDisplayResults: Boolean,
        hasProgramResults: Boolean,
        hasGlobalResults: Boolean?,
    ) {
        val result =
            when {
                !canDisplayResults -> {
                    listOf(SearchResult(SearchResult.SearchResultType.TOO_MANY_RESULTS))
                }

                hasGlobalResults == null &&
                    searchRepository.getProgram(initialProgramUid) != null &&
                    searchRepository.filterQueryForProgram(queryDataAsMap(), null).isNotEmpty() &&
                    searchRepository.filtersApplyOnGlobalSearch() -> {
                    listOf(
                        SearchResult(
                            SearchResult.SearchResultType.SEARCH_OUTSIDE,
                            searchRepository.getProgram(initialProgramUid)?.displayName(),
                        ),
                    )
                }

                hasGlobalResults == null &&
                    searchRepository.getProgram(initialProgramUid) != null &&
                    searchRepository.trackedEntityTypeFields().isNotEmpty() &&
                    searchRepository.filtersApplyOnGlobalSearch() -> {
                    listOf(
                        SearchResult(
                            type = SearchResult.SearchResultType.UNABLE_SEARCH_OUTSIDE,
                            uiData =
                                UnableToSearchOutsideData(
                                    trackedEntityTypeAttributes =
                                        searchRepository.trackedEntityTypeFields(),
                                    trackedEntityTypeName =
                                        searchRepository.trackedEntityType.displayName()!!,
                                ),
                        ),
                    )
                }

                hasProgramResults || hasGlobalResults == true ->
                    listOf(SearchResult(SearchResult.SearchResultType.NO_MORE_RESULTS))

                else ->
                    listOf(SearchResult(SearchResult.SearchResultType.NO_RESULTS))
            }
        _dataResult.postValue(result)
    }

    fun filtersApplyOnGlobalSearch(): Boolean = searchRepository.filtersApplyOnGlobalSearch()

    private fun handleInitWithoutData() {
        val result =
            when (searchRepository.canCreateInProgramWithoutSearch()) {
                true ->
                    listOf(
                        SearchResult(
                            SearchResult.SearchResultType.SEARCH_OR_CREATE,
                            searchRepository.trackedEntityType.displayName(),
                        ),
                    )

                false ->
                    listOf(
                        SearchResult(
                            SearchResult.SearchResultType.SEARCH,
                            searchRepository.trackedEntityType.displayName(),
                        ),
                    )
            }
        _dataResult.postValue(result)
    }

    fun onBackPressed(
        isPortrait: Boolean,
        searchOrFilterIsOpen: Boolean,
        keyBoardIsOpen: Boolean,
        goBackCallback: () -> Unit,
        closeSearchOrFilterCallback: () -> Unit,
        closeKeyboardCallback: () -> Unit,
    ) {
        val searchScreenIsForced =
            _screenState.value?.let {
                if (it is SearchList && it.searchForm.isForced) {
                    it.searchForm.isForced
                } else {
                    false
                }
            } ?: false

        if (isPortrait && searchOrFilterIsOpen && !searchScreenIsForced) {
            if (keyBoardIsOpen) closeKeyboardCallback()
            closeSearchOrFilterCallback()
            viewModelScope.launch {
                searchParametersUiState.onBackPressed(true)
            }
        } else if (keyBoardIsOpen) {
            closeKeyboardCallback()
            goBackCallback()
        } else {
            goBackCallback()
        }
    }

    fun canDisplayBottomNavigationBar(): Boolean =
        _screenState.value?.let {
            it is SearchList
        } ?: false

    fun onProgramSelected(
        programIndex: Int,
        programs: List<ProgramSpinnerModel>,
        onProgramChanged: (selectedProgramUid: String?) -> Unit,
    ) {
        val selectedProgram =
            when {
                programIndex > 0 ->
                    programs.takeIf { it.size > 1 }?.let { it[programIndex - 1] }
                        ?: programs.first()

                else -> null
            }
        searchRepository.setCurrentTheme(selectedProgram)

        if (selectedProgram?.uid != initialProgramUid) {
            onProgramChanged(selectedProgram?.uid)
        }
    }

    fun isBottomNavigationBarVisible(): Boolean =
        pageConfiguration.value?.let {
            it.displayMapView() || it.displayAnalytics()
        } ?: false

    fun setFiltersOpened(filtersOpened: Boolean) {
        _filtersOpened.postValue(filtersOpened)
    }

    fun onFiltersClick(isLandscape: Boolean) {
        _screenState.value
            .takeIf { it is SearchList }
            ?.let {
                val currentScreen = (it as SearchList)
                val filterFieldsVisible = !currentScreen.searchFilters.isOpened
                currentScreen.copy(
                    searchForm =
                        currentScreen.searchForm.copy(
                            isOpened =
                                if (filterFieldsVisible) {
                                    false
                                } else {
                                    isLandscape
                                },
                        ),
                    searchFilters =
                        SearchFilters(
                            hasActiveFilters = hasActiveFilters(),
                            isOpened = filterFieldsVisible,
                        ),
                )
            }?.let {
                _screenState.postValue(it)
            }
    }

    fun updateBackdrop(screenState: SearchTEScreenState) {
        _backdropActive.postValue(
            screenState.takeIf { it is SearchList }?.let {
                val currentScreen = it as SearchList
                currentScreen.searchForm.isOpened || currentScreen.searchFilters.isOpened
            } ?: false,
        )
    }

    fun filterIsOpen(): Boolean =
        _screenState.value?.takeIf { it is SearchList }?.let {
            val currentScreen = it as SearchList
            currentScreen.searchFilters.isOpened
        } ?: false

    fun fetchMapStyles(): List<BaseMapStyle> = mapStyleConfig.fetchMapStyles()

    fun onLegacyInteractionConsumed() {
        _legacyInteraction.postValue(null)
    }

    fun fetchSearchParameters(
        programUid: String?,
        teiTypeUid: String,
    ) {
        fetchJob?.cancel()
        fetchJob =
            viewModelScope.launch {
                fetchSearchParameters
                    .invoke(
                        input =
                            FetchSearchParametersData(
                                teiTypeUid = teiTypeUid,
                                programUid = programUid,
                            ),
                    ).fold(
                        onSuccess = { searchParameters ->
                            searchParametersUiState =
                                searchParametersUiState.copy(
                                    items =
                                        searchParameters.map { searchParameter ->
                                            searchParameter.toTrackerInputUiState()
                                        },
                                )
                        },
                        onFailure = {
                            // TODO(Implement error)
                        },
                    )
            }
    }

    private fun onQrCodeScanned(
        uid: String,
        value: String?,
    ) {
        viewModelScope.launch {
            updateQuery(
                uid,
                value?.let { listOf(it) },
            )

            searching = queryDataList.isNotEmpty()
            searchParametersUiState =
                searchParametersUiState.copy(
                    clearSearchEnabled = queryDataList.isNotEmpty(),
                    searchedItems = getFriendlyQueryData(),
                )

            val isOnline = searching && networkUtils.isOnline()
            val selectedProgram = searchRepository.getProgram(initialProgramUid)

            // get uids to exclude for possible duplicates
            val excludeValues = searchRepositoryKt.getExcludeValues()

            val newTrackerSearchModel =
                SearchTrackedEntitiesInput(
                    selectedProgram = selectedProgram?.uid(),
                    allowCache = false, // No need for cache in immediate search
                    excludeValues = excludeValues,
                    hasStateFilters = filterManager.stateFilters.isNotEmpty(),
                    isOnline = isOnline,
                    queryDataList = queryDataList,
                )

            // Use invokeImmediate for QR code scanning to get immediate non-paginated results
            val trackedEntitiesResult = searchTrackedEntities.invokeImmediate(newTrackerSearchModel)

            val trackedEntities = trackedEntitiesResult.getOrNull() ?: emptyList()

            if (trackedEntities.isEmpty() || trackedEntities.size > 1) return@launch

            val tei = trackedEntities.first()

            // Transform domain model to SDK model for compatibility with existing code
            val sdkTei =
                withContext(dispatchers.io()) {
                    transformDomainTeiToSDKTei(tei)
                }

            val searchTeiModel =
                withContext(dispatchers.io()) {
                    searchRepository.transform(
                        sdkTei,
                        selectedProgram,
                        !(isOnline && filterManager.stateFilters.isEmpty()),
                        filterManager.sortingItem,
                    )
                }

            searching = false

            clearQueryData()
            clearFocus()

            // Open TEI dashboard for the found TEI
            onTeiClick(
                teiUid = searchTeiModel.uid(),
                enrollmentUid = searchTeiModel.selectedEnrollment.uid(),
                online = searchTeiModel.isOnline,
            )
        }
    }

    fun clearFocus() {
        val updatedItems =
            searchParametersUiState.items.map {
                if (it.focused) {
                    it.copy(focused = false)
                } else {
                    it
                }
            }
        searchParametersUiState = searchParametersUiState.copy(items = updatedItems)
    }

    fun getFriendlyQueryData(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        searchParametersUiState.items
            .filter { !it.value.isNullOrEmpty() }
            .forEach { item ->

                when (item.valueType) {
                    TrackerInputType.ORGANISATION_UNIT, TrackerInputType.MULTI_SELECTION -> {
                        map[item.uid] = (item.displayName ?: "")
                    }

                    TrackerInputType.DATE, TrackerInputType.AGE -> {
                        item.value?.let {
                            map[item.uid] = it.toFriendlyDate()
                        }
                    }

                    TrackerInputType.DATE_TIME -> {
                        item.value?.let {
                            map[item.uid] = it.toFriendlyDateTime()
                        }
                    }

                    TrackerInputType.YES_ONLY_SWITCH,
                    TrackerInputType.YES_ONLY_CHECKBOX,
                    TrackerInputType.HORIZONTAL_RADIOBUTTONS,
                    TrackerInputType.VERTICAL_RADIOBUTTONS,
                    TrackerInputType.HORIZONTAL_CHECKBOXES,
                    TrackerInputType.VERTICAL_CHECKBOXES,
                    -> {
                        item.value?.let {
                            if (it == "true" || it == "false") {
                                map[item.uid] = "${item.label}: $it"
                            }
                        }
                    }

                    TrackerInputType.PERCENTAGE -> {
                        item.value?.let {
                            map[item.uid] = it.toPercentage()
                        }
                    }

                    else -> {
                        map[item.uid] = (item.value ?: "")
                    }
                }
            }
        return map
    }

    fun onFeatureClicked(feature: Feature) {
        feature.toStringProperty()?.let {
            viewModelScope.launch {
                _mapItemClicked.emit(it)
            }
        }
    }

    fun filterVisibleMapItems(layersVisibility: Map<String, MapLayer>) {
        this.layersVisibility = layersVisibility
        fetchMapResults()
    }

    fun launchCustomIntent(
        fieldUid: FieldUid,
        customIntentUid: CustomIntentUid,
    ) {
        viewModelScope.launch {
            searchRepositoryKt.getCustomIntent(fieldUid)?.let { customIntentModel ->
                _searchActions.send(
                    TrackerInputAction.LaunchCustomIntent(
                        fieldUid = fieldUid,
                        customIntentModel = customIntentModel,
                    ),
                )
            }
        }
    }

    fun launchScan(
        fieldUid: String,
        optionSet: String?,
        renderType: TrackerInputType,
    ) {
        val scanType =
            if (renderType == TrackerInputType.QR_CODE) {
                TrackerInputType.QR_CODE
            } else {
                TrackerInputType.BAR_CODE
            }

        viewModelScope.launch {
            _searchActions.send(
                TrackerInputAction.Scan(
                    fieldUid = fieldUid,
                    optionSet = optionSet,
                    renderType = scanType,
                ),
            )
        }
    }

    fun onValueChange(
        fieldUid: String,
        value: String?,
    ) {
        updateQuery(
            fieldUid,
            value?.split(","),
        )
    }

    fun onItemClick(fieldUid: FieldUid) {
        searchParametersUiState
            .copy(
                items =
                    searchParametersUiState.items.map {
                        if (it.uid == fieldUid) {
                            it.copy(focused = true)
                        } else {
                            it.copy(focused = false)
                        }
                    },
            ).let {
                searchParametersUiState = it
            }
    }

    fun handleCustomIntentResult(customIntentResult: CustomIntentResult) {
        when (customIntentResult) {
            is CustomIntentResult.Error -> {
                updateSearchParameters(
                    customIntentResult.fieldUid,
                    null,
                    resourceManager.getString(R.string.custom_intent_error),
                )
            }

            is CustomIntentResult.Success -> {
                updateSearchParameters(
                    customIntentResult.fieldUid,
                    listOf(customIntentResult.value),
                )
            }
        }
    }

    fun handleScanResult(
        fieldUid: String,
        value: String?,
    ) {
        onQrCodeScanned(
            uid = fieldUid,
            value = value,
        )
        value?.let {
            updateSearchParameters(
                uid = fieldUid,
                values = listOf(value),
            )
        }
    }

    // The following 3 function are temporary until QueryData is fully refactored for usage in
    // other places in the search feature
    fun queryDataAsMap() = queryDataList.toMap()

    private fun MutableList<QueryData>.toMap(): MutableMap<String, List<String>?> =
        this
            .associate { queryData ->
                val valueList = queryData.values
                queryData.attributeId to valueList
            }.toMutableMap()

    private fun MutableMap<String, List<String>?>.toQueryDataList(items: List<TrackerInputUiState>) =
        this
            .map { (attributeId, valuesList) ->
                QueryData(
                    attributeId = attributeId,
                    values = valuesList,
                    searchOperator = items.firstOrNull { it.uid == attributeId }?.searchOperator,
                )
            }.toMutableList()

    //
}
