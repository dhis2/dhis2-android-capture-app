package org.dhis2.usescases.searchTrackEntity

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
import com.mapbox.geojson.Feature
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dhis2.R
import org.dhis2.commons.data.SearchTeiModel
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.idlingresource.SearchIdlingResourceSingleton
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.search.SearchParametersModel
import org.dhis2.form.model.FieldUiModelImpl
import org.dhis2.form.ui.intent.FormIntent
import org.dhis2.form.ui.provider.DisplayNameProvider
import org.dhis2.maps.extensions.toStringProperty
import org.dhis2.maps.layer.MapLayer
import org.dhis2.maps.layer.basemaps.BaseMapStyle
import org.dhis2.maps.usecases.MapStyleConfiguration
import org.dhis2.usescases.searchTrackEntity.listView.SearchResult
import org.dhis2.usescases.searchTrackEntity.searchparameters.model.SearchParametersUiState
import org.dhis2.usescases.searchTrackEntity.ui.UnableToSearchOutsideData
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator
import org.hisp.dhis.android.core.arch.helpers.Result
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import timber.log.Timber
import java.text.ParseException

const val TEI_TYPE_SEARCH_MAX_RESULTS = 5

class SearchTEIViewModel(
    val initialProgramUid: String?,
    initialQuery: MutableMap<String, String>?,
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
) : ViewModel() {

    private var layersVisibility: Map<String, MapLayer> = emptyMap()

    private val _pageConfiguration = MutableLiveData<NavigationPageConfigurator>()
    val pageConfiguration: LiveData<NavigationPageConfigurator> = _pageConfiguration

    val queryData = mutableMapOf<String, String>().apply {
        initialQuery?.let { putAll(it) }
    }

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
    private val _filtersActive = MutableLiveData(false)

    private val _downloadResult = MutableLiveData<TeiDownloadResult>()
    val downloadResult: LiveData<TeiDownloadResult> = _downloadResult

    private val _dataResult = MutableLiveData<List<SearchResult>>()
    val dataResult: LiveData<List<SearchResult>> = _dataResult

    private val _filtersOpened = MutableLiveData(false)
    val filtersOpened: LiveData<Boolean> = _filtersOpened

    private val _teTypeName = MutableLiveData("")
    val teTypeName: LiveData<String> = _teTypeName

    var uiState by mutableStateOf(SearchParametersUiState())

    private var fetchJob: Job? = null

    init {
        viewModelScope.launch(dispatchers.io()) {
            createButtonScrollVisibility.postValue(
                searchRepository.canCreateInProgramWithoutSearch(),
            )
            _pageConfiguration.postValue(searchNavPageConfigurator.initVariables())

            _teTypeName.postValue(
                searchRepository.trackedEntityType.displayName(),
            )
        }
    }

    fun setListScreen() {
        _screenState.value.takeIf { it?.screenState == SearchScreenState.MAP }?.let {
            searching = (it as SearchList).isSearching
        }
        val displayFrontPageList =
            searchRepository.getProgram(initialProgramUid)?.displayFrontPageList() ?: true
        val shouldOpenSearch = !displayFrontPageList &&
            !searchRepository.canCreateInProgramWithoutSearch() &&
            !searching &&
            _filtersActive.value == false

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
                displayFrontPageList = searchRepository.getProgram(initialProgramUid)
                    ?.displayFrontPageList()
                    ?: false,
                canCreateWithoutSearch = searchRepository.canCreateInProgramWithoutSearch(),
                isSearching = searching,
                searchForm = SearchForm(
                    queryHasData = queryData.isNotEmpty(),
                    minAttributesToSearch = searchRepository.getProgram(initialProgramUid)
                        ?.minAttributesRequiredToSearch()
                        ?: 1,
                    isForced = shouldOpenSearch,
                    isOpened = shouldOpenSearch,
                ),
                searchFilters = SearchFilters(
                    hasActiveFilters = hasActiveFilters(),
                    isOpened = filterIsOpen(),
                ),
            ),
        )
    }

    private fun hasActiveFilters() = _filtersActive.value == true

    fun setMapScreen() {
        _screenState.value.takeIf { it?.screenState == SearchScreenState.LIST }?.let {
            searching = (it as SearchList).isSearching
        }
        _screenState.postValue(
            SearchList(
                previousSate = _screenState.value?.screenState ?: SearchScreenState.NONE,
                listType = SearchScreenState.MAP,
                displayFrontPageList = searchRepository.getProgram(initialProgramUid)
                    ?.displayFrontPageList()
                    ?: false,
                canCreateWithoutSearch = searchRepository.canCreateInProgramWithoutSearch(),
                isSearching = searching,
                searchForm = SearchForm(
                    queryHasData = queryData.isNotEmpty(),
                    minAttributesToSearch = searchRepository.getProgram(initialProgramUid)
                        ?.minAttributesRequiredToSearch()
                        ?: 1,
                    isForced = false,
                    isOpened = false,
                ),
                searchFilters = SearchFilters(
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
                displayFrontPageList = searchRepository.getProgram(initialProgramUid)
                    ?.displayFrontPageList()
                    ?: false,
                canCreateWithoutSearch = searchRepository.canCreateInProgramWithoutSearch(),
                isSearching = searching,
                searchForm = SearchForm(
                    queryHasData = queryData.isNotEmpty(),
                    minAttributesToSearch = searchRepository.getProgram(initialProgramUid)
                        ?.minAttributesRequiredToSearch()
                        ?: 1,
                    isForced = false,
                    isOpened = true,
                ),
                searchFilters = SearchFilters(
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
            else -> {}
        }
    }

    fun updateActiveFilters(filtersActive: Boolean) {
        if (_filtersActive.value != filtersActive) searchRepository.clearFetchedList()
        _filtersActive.postValue(filtersActive)
    }

    fun refreshData() {
        performSearch()
    }

    private fun updateQuery(uid: String, value: String?) {
        if (value.isNullOrEmpty()) {
            queryData.remove(uid)
        } else {
            queryData[uid] = value
        }

        updateSearchParameters(uid, value)
        updateSearch()
    }

    private fun updateSearchParameters(uid: String, value: String?) {
        val updatedItems = uiState.items.map {
            if (it.uid == uid) {
                (it as FieldUiModelImpl).copy(
                    value = value,
                    displayName = displayNameProvider.provideDisplayName(
                        valueType = it.valueType,
                        value = value,
                        optionSet = it.optionSet,
                        periodType = it.periodSelector?.type,
                    ),
                )
            } else {
                it
            }
        }
        uiState = uiState.copy(items = updatedItems)
    }

    fun clearQueryData() {
        queryData.clear()
        clearSearchParameters()
        updateSearch()
        performSearch()
    }

    private fun clearSearchParameters() {
        val updatedItems = uiState.items.map {
            (it as FieldUiModelImpl).copy(value = null, displayName = null)
        }
        uiState = uiState.copy(
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
                    searchForm = currentSearchList.searchForm.copy(
                        queryHasData = queryData.isNotEmpty(),
                    ),
                ),
            )
        }
        uiState = uiState.copy(searchEnabled = queryData.isNotEmpty())
    }

    fun fetchListResults(onPagedListReady: (Flow<PagingData<SearchTeiModel>>?) -> Unit) {
        SearchIdlingResourceSingleton.increment()
        viewModelScope.launch(dispatchers.io()) {
            val resultPagedList = async {
                when {
                    searching -> loadSearchResults().cachedIn(viewModelScope)
                    displayFrontPageList() -> loadDisplayInListResults().cachedIn(viewModelScope)
                    else -> null
                }
            }
            try {
                onPagedListReady(resultPagedList.await())
            } catch (e: Exception) {
                Timber.e(e)
            } finally {
                SearchIdlingResourceSingleton.decrement()
            }
        }
    }

    private suspend fun loadSearchResults() = withContext(dispatchers.io()) {
        val searchParametersModel = SearchParametersModel(
            selectedProgram = searchRepository.getProgram(initialProgramUid),
            queryData = queryData,
        )
        val getPagingData = searchRepositoryKt.searchTrackedEntities(
            searchParametersModel,
            searching && networkUtils.isOnline(),
        )

        return@withContext getPagingData.map { pagingData ->
            pagingData.map { item ->
                withContext(dispatchers.io()) {
                    if (
                        searching && networkUtils.isOnline() &&
                        filterManager.stateFilters.isEmpty()
                    ) {
                        searchRepository.transform(
                            item,
                            searchParametersModel.selectedProgram,
                            false,
                            filterManager.sortingItem,
                        )
                    } else {
                        searchRepository.transform(
                            item,
                            searchParametersModel.selectedProgram,
                            true,
                            filterManager.sortingItem,
                        )
                    }
                }
            }
        }
    }

    private suspend fun loadDisplayInListResults() = withContext(dispatchers.io()) {
        val searchParametersModel = SearchParametersModel(
            selectedProgram = searchRepository.getProgram(initialProgramUid),
            queryData = queryData,
        )
        val getPagingData = searchRepositoryKt.searchTrackedEntities(
            searchParametersModel,
            false,
        )

        return@withContext getPagingData.map { pagingData ->
            pagingData.map { item ->
                withContext(dispatchers.io()) {
                    searchRepository.transform(
                        item,
                        searchParametersModel.selectedProgram,
                        true,
                        filterManager.sortingItem,
                    )
                }
            }
        }
    }

    suspend fun fetchGlobalResults() = withContext(dispatchers.io()) {
        val searchParametersModel = SearchParametersModel(
            selectedProgram = searchRepository.getProgram(initialProgramUid),
            queryData = queryData,
        )
        val getPagingData = searchRepositoryKt.searchTrackedEntities(
            searchParametersModel,
            searching && networkUtils.isOnline(),
        )

        return@withContext if (searching) {
            getPagingData.map { pagingData ->
                pagingData.map { item ->
                    withContext(dispatchers.io()) {
                        if (
                            searching && networkUtils.isOnline() &&
                            filterManager.stateFilters.isEmpty()
                        ) {
                            searchRepository.transform(
                                item,
                                searchParametersModel.selectedProgram,
                                false,
                                filterManager.sortingItem,
                            )
                        } else {
                            searchRepository.transform(
                                item,
                                searchParametersModel.selectedProgram,
                                true,
                                filterManager.sortingItem,
                            )
                        }
                    }
                }
            }
        } else {
            null
        }
    }

    fun fetchMapResults() {
        SearchIdlingResourceSingleton.increment()
        viewModelScope.launch {
            val result = async(context = dispatchers.io()) {
                mapDataRepository.getTrackerMapData(
                    searchRepository.getProgram(initialProgramUid),
                    queryData,
                    layersVisibility,
                )
            }

            try {
                val data = result.await()
                _mapResults.send(data)
            } catch (e: Exception) {
                Timber.e(e)
            } finally {
                SearchIdlingResourceSingleton.decrement()
            }
            searching = false
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
                    searching = queryData.isNotEmpty()
                    uiState = uiState.copy(
                        clearSearchEnabled = queryData.isNotEmpty(),
                        searchedItems = getFriendlyQueryData(),
                    )

                    when (_screenState.value?.screenState) {
                        SearchScreenState.LIST -> {
                            setListScreen()
                        fetchListResults { flow ->
                            flow?.let {
                                _refreshData.postValue(Unit)
                                }
                            }
                        }

                        SearchScreenState.MAP -> {
                            _refreshData.postValue(Unit)
                        setMapScreen()
                        fetchMapResults()
                    }

                        else -> searching = false
                    }
                } else {
                    val minAttributesToSearch = searchRepository.getProgram(initialProgramUid)
                        ?.minAttributesRequiredToSearch()
                        ?: 0
                    val message = resourceManager.getString(
                        R.string.search_min_num_attr,
                        minAttributesToSearch,
                    )
                    uiState = uiState.copy(minAttributesMessage = message)
                    uiState.updateMinAttributeWarning(true)
                    setSearchScreen()
                    _refreshData.postValue(Unit)
                }
            } catch (e: Exception) {
                Timber.d(e.message)
            }
        }
    }

    private fun canPerformSearch(): Boolean {
        return minAttributesToSearchCheck() || displayFrontPageList()
    }

    private fun minAttributesToSearchCheck(): Boolean {
        return searchRepository.getProgram(initialProgramUid)?.let { program ->
            (program.minAttributesRequiredToSearch() ?: 0) <= queryData.size
        } ?: true
    }

    private fun displayFrontPageList(): Boolean {
        return searchRepository.getProgram(initialProgramUid)?.let { program ->
            program.displayFrontPageList() == true && queryData.isEmpty()
        } ?: false
    }

    private fun canDisplayResult(itemCount: Int, onlineTooManyResults: Boolean): Boolean {
        return !onlineTooManyResults && when (initialProgramUid) {
            null -> itemCount <= TEI_TYPE_SEARCH_MAX_RESULTS
            else ->
                searchRepository.getProgram(initialProgramUid)?.maxTeiCountToReturn()
                    ?.takeIf { it != 0 }
                    ?.let { maxTeiCount ->
                        itemCount <= maxTeiCount
                    } ?: true
        }
    }

    fun queryDataByProgram(programUid: String?): MutableMap<String, String> {
        return searchRepository.filterQueryForProgram(queryData, programUid)
    }

    fun onEnrollClick() {
        _legacyInteraction.postValue(LegacyInteraction.OnEnrollClick(queryData))
    }

    fun onAddRelationship(teiUid: String, relationshipTypeUid: String?, online: Boolean) {
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

    fun onDownloadTei(teiUid: String, enrollmentUid: String?, reason: String? = null) {
        viewModelScope.launch {
            val result = async(dispatchers.io()) {
                searchRepository.download(teiUid, enrollmentUid, reason)
            }
            try {
                val downloadResult = result.await()
                if (downloadResult is TeiDownloadResult.TeiToEnroll) {
                    _legacyInteraction.postValue(
                        LegacyInteraction.OnEnroll(
                            initialProgramUid,
                            downloadResult.teiUid,
                            queryData,
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

    fun onTeiClick(teiUid: String, enrollmentUid: String?, online: Boolean) {
        _legacyInteraction.postValue(
            LegacyInteraction.OnTeiClick(teiUid, enrollmentUid, online),
        )
    }

    fun onDataLoaded(
        programResultCount: Int,
        globalResultCount: Int? = null,
        onlineErrorCode: D2ErrorCode? = null,
    ) {
        val canDisplayResults = canDisplayResult(
            programResultCount,
            onlineErrorCode == D2ErrorCode.MAX_TEI_COUNT_REACHED,
        )
        val hasProgramResults = programResultCount > 0
        val hasGlobalResults = globalResultCount?.let { it > 0 }

        val isSearching = _screenState.value?.takeIf { it is SearchList }?.let {
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
        val result = when {
            !hasProgramResults && searchRepository.canCreateInProgramWithoutSearch() ->
                listOf(
                    SearchResult(
                        SearchResult.SearchResultType.SEARCH_OR_CREATE,
                        searchRepository.trackedEntityType.displayName(),
                    ),
                )

            else -> listOf(SearchResult(SearchResult.SearchResultType.NO_MORE_RESULTS_OFFLINE))
        }

        if (result.isEmpty() && _filtersActive.value == false) {
            setSearchScreen()
        }

        _dataResult.postValue(result)
    }

    private fun handleSearchResult(
        canDisplayResults: Boolean,
        hasProgramResults: Boolean,
        hasGlobalResults: Boolean?,
    ) {
        val result = when {
            !canDisplayResults -> {
                listOf(SearchResult(SearchResult.SearchResultType.TOO_MANY_RESULTS))
            }

            hasGlobalResults == null && searchRepository.getProgram(initialProgramUid) != null &&
                searchRepository.filterQueryForProgram(queryData, null).isNotEmpty() &&
                searchRepository.filtersApplyOnGlobalSearch() -> {
                listOf(
                    SearchResult(
                        SearchResult.SearchResultType.SEARCH_OUTSIDE,
                        searchRepository.getProgram(initialProgramUid)?.displayName(),

                    ),
                )
            }

            hasGlobalResults == null && searchRepository.getProgram(initialProgramUid) != null &&
                searchRepository.trackedEntityTypeFields().isNotEmpty() &&
                searchRepository.filtersApplyOnGlobalSearch() -> {
                listOf(
                    SearchResult(
                        type = SearchResult.SearchResultType.UNABLE_SEARCH_OUTSIDE,
                        uiData = UnableToSearchOutsideData(
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
        val result = when (searchRepository.canCreateInProgramWithoutSearch()) {
            true -> listOf(
                SearchResult(
                    SearchResult.SearchResultType.SEARCH_OR_CREATE,
                    searchRepository.trackedEntityType.displayName(),
                ),
            )

            false -> listOf(
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
        val searchScreenIsForced = _screenState.value?.let {
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
                uiState.onBackPressed(true)
            }
        } else if (keyBoardIsOpen) {
            closeKeyboardCallback()
            goBackCallback()
        } else {
            goBackCallback()
        }
    }

    fun canDisplayBottomNavigationBar(): Boolean {
        return _screenState.value?.let {
            it is SearchList
        } ?: false
    }

    fun onProgramSelected(
        programIndex: Int,
        programs: List<ProgramSpinnerModel>,
        onProgramChanged: (selectedProgramUid: String?) -> Unit,
    ) {
        val selectedProgram = when {
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

    fun isBottomNavigationBarVisible(): Boolean {
        return _pageConfiguration.value?.let {
            it.displayMapView() || it.displayAnalytics()
        } ?: false
    }

    fun setFiltersOpened(filtersOpened: Boolean) {
        _filtersOpened.postValue(filtersOpened)
    }

    fun onFiltersClick(isLandscape: Boolean) {
        _screenState.value.takeIf { it is SearchList }?.let {
            val currentScreen = (it as SearchList)
            val filterFieldsVisible = !currentScreen.searchFilters.isOpened
            currentScreen.copy(
                searchForm = currentScreen.searchForm.copy(
                    isOpened = if (filterFieldsVisible) {
                        false
                    } else {
                        isLandscape
                    },
                ),
                searchFilters = SearchFilters(
                    hasActiveFilters = hasActiveFilters(),
                    isOpened = filterFieldsVisible,
                ),
            )
        }?.let {
            _screenState.postValue(it)
        }
    }

    fun searchOrFilterIsOpen(): Boolean {
        return _screenState.value?.takeIf { it is SearchList }?.let {
            val currentScreen = it as SearchList
            currentScreen.searchForm.isOpened || currentScreen.searchFilters.isOpened
        } ?: false
    }

    fun filterIsOpen(): Boolean {
        return _screenState.value?.takeIf { it is SearchList }?.let {
            val currentScreen = it as SearchList
            currentScreen.searchFilters.isOpened
        } ?: false
    }

    fun fetchMapStyles(): List<BaseMapStyle> {
        return mapStyleConfig.fetchMapStyles()
    }

    fun onLegacyInteractionConsumed() {
        _legacyInteraction.postValue(null)
    }

    fun fetchSearchParameters(
        programUid: String?,
        teiTypeUid: String,
    ) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            val fieldUiModels =
                searchRepositoryKt.searchParameters(programUid, teiTypeUid)
            uiState = uiState.copy(items = fieldUiModels)
        }
    }

    fun onParameterIntent(formIntent: FormIntent) = when (formIntent) {
        is FormIntent.OnTextChange -> {
            updateQuery(
                formIntent.uid,
                formIntent.value,
            )
        }

        is FormIntent.OnSave -> {
            updateQuery(
                formIntent.uid,
                formIntent.value,
            )
        }

        is FormIntent.OnQrCodeScanned -> {
            onQrCodeScanned(formIntent)
        }

        is FormIntent.OnFocus -> {
            val updatedItems = uiState.items.map { field ->
                if (field.focused && field.uid != formIntent.uid) {
                    val validation = field.value?.takeIf {
                        field.valueType in listOf(
                            ValueType.DATE, ValueType.DATETIME, ValueType.AGE, ValueType.TIME,
                        )
                    }?.let { value -> field.valueType?.validator?.validate(value) }

                    (field as FieldUiModelImpl).copy(
                        focused = false,
                        error = when (validation) {
                            is Result.Failure -> resourceManager.getString(R.string.formatting_error)
                            else -> null
                        },
                    )
                } else if (field.uid == formIntent.uid) {
                    (field as FieldUiModelImpl).copy(focused = true)
                } else {
                    field
                }
            }
            uiState = uiState.copy(items = updatedItems)
        }

        is FormIntent.ClearValue -> {
            updateQuery(
                formIntent.uid,
                null,
            )
        }

        else -> {
            // no-op
        }
    }

    private fun onQrCodeScanned(formIntent: FormIntent.OnQrCodeScanned) {
        viewModelScope.launch {
            updateQuery(
                formIntent.uid,
                formIntent.value,
            )

            searching = queryData.isNotEmpty()
            uiState = uiState.copy(
                clearSearchEnabled = queryData.isNotEmpty(),
                searchedItems = getFriendlyQueryData(),
            )

            val searchParametersModel = SearchParametersModel(
                selectedProgram = searchRepository.getProgram(initialProgramUid),
                queryData = queryData,
            )
            val isOnline = searching && networkUtils.isOnline()
            val trackedEntities = async(dispatchers.io()) {
                searchRepositoryKt.searchTrackedEntitiesImmediate(
                    searchParametersModel = searchParametersModel,
                    isOnline = isOnline,
                )
            }.await()

            if (trackedEntities.isEmpty() || trackedEntities.size > 1) return@launch

            val tei = trackedEntities.first()
            val searchTeiModel = withContext(dispatchers.io()) {
                searchRepository.transform(
                    /* searchItem = */
                    tei,
                    /* selectedProgram = */
                    searchParametersModel.selectedProgram,
                    /* offlineOnly = */
                    !(isOnline && filterManager.stateFilters.isEmpty()),
                    /* sortingItem = */
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
        val updatedItems = uiState.items.map {
            if (it.focused) {
                (it as FieldUiModelImpl).copy(focused = false)
            } else {
                it
            }
        }
        uiState = uiState.copy(items = updatedItems)
    }

    fun getFriendlyQueryData(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        uiState.items.filter { !it.value.isNullOrEmpty() }
            .forEach { item ->
                when (item.valueType) {
                    ValueType.ORGANISATION_UNIT, ValueType.MULTI_TEXT -> {
                        map[item.uid] = (item.displayName ?: "")
                    }

                    ValueType.DATE, ValueType.AGE -> {
                        item.value?.let {
                            if (it.isNotEmpty()) {
                                val date = try {
                                    DateUtils.oldUiDateFormat().parse(it)
                                } catch (e: ParseException) {
                                    null
                                }
                                map[item.uid] = date?.let {
                                    DateUtils.uiDateFormat().format(date)
                                } ?: it
                            }
                        }
                    }

                    ValueType.DATETIME -> {
                        item.value?.let {
                            if (it.isNotEmpty()) {
                                val date = try {
                                    DateUtils.databaseDateFormatNoSeconds().parse(it)
                                } catch (e: ParseException) {
                                    null
                                }
                                map[item.uid] = date?.let {
                                    DateUtils.uiDateTimeFormat().format(date)
                                } ?: it
                            }
                        }
                    }

                    ValueType.BOOLEAN -> {
                        map[item.uid] = "${item.label}: ${item.value}"
                    }

                    ValueType.TRUE_ONLY -> {
                        item.value?.let {
                            if (it == "true") {
                                map[item.uid] = item.label
                            }
                        }
                    }

                    ValueType.PERCENTAGE -> {
                        map[item.uid] = "${item.value}%"
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
}
