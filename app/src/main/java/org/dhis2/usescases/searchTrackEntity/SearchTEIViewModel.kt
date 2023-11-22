package org.dhis2.usescases.searchTrackEntity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dhis2.commons.data.SearchTeiModel
import org.dhis2.commons.idlingresource.SearchIdlingResourceSingleton
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.search.SearchParametersModel
import org.dhis2.form.model.ActionType
import org.dhis2.form.model.RowAction
import org.dhis2.maps.layer.basemaps.BaseMapStyle
import org.dhis2.maps.usecases.MapStyleConfiguration
import org.dhis2.usescases.searchTrackEntity.listView.SearchResult
import org.dhis2.usescases.searchTrackEntity.ui.UnableToSearchOutsideData
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import timber.log.Timber

const val TEI_TYPE_SEARCH_MAX_RESULTS = 5

class SearchTEIViewModel(
    private val initialProgramUid: String?,
    initialQuery: MutableMap<String, String>?,
    private val searchRepository: SearchRepository,
    private val searchNavPageConfigurator: SearchPageConfigurator,
    private val mapDataRepository: MapDataRepository,
    private val networkUtils: NetworkUtils,
    private val dispatchers: DispatcherProvider,
    private val mapStyleConfig: MapStyleConfiguration,
) : ViewModel() {

    private val _pageConfiguration = MutableLiveData<NavigationPageConfigurator>()
    val pageConfiguration: LiveData<NavigationPageConfigurator> = _pageConfiguration

    val queryData = mutableMapOf<String, String>().apply {
        initialQuery?.let { putAll(it) }
    }

    private val _legacyInteraction = MutableLiveData<LegacyInteraction?>()
    val legacyInteraction: LiveData<LegacyInteraction?> = _legacyInteraction

    private val _refreshData = MutableLiveData(Unit)
    val refreshData: LiveData<Unit> = _refreshData

    private val _mapResults = MutableLiveData<TrackerMapData>()
    val mapResults: LiveData<TrackerMapData> = _mapResults

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

    init {
        viewModelScope.launch(dispatchers.io()) {
            createButtonScrollVisibility.postValue(
                searchRepository.canCreateInProgramWithoutSearch(),
            )
            _pageConfiguration.postValue(searchNavPageConfigurator.initVariables())
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

        createButtonScrollVisibility.value = if (searching) {
            true
        } else {
            searchRepository.canCreateInProgramWithoutSearch()
        }
        _screenState.value = SearchList(
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
        )
    }

    private fun hasActiveFilters() = _filtersActive.value == true

    fun setMapScreen() {
        _screenState.value.takeIf { it?.screenState == SearchScreenState.LIST }?.let {
            searching = (it as SearchList).isSearching
        }
        _screenState.value = SearchList(
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
        )
    }

    fun setAnalyticsScreen() {
        _screenState.value = SearchAnalytics(
            previousSate = _screenState.value?.screenState ?: SearchScreenState.NONE,
        )
    }

    fun setSearchScreen() {
        _screenState.value = SearchList(
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
        _filtersActive.value = filtersActive
    }

    fun refreshData() {
        performSearch()
    }

    fun updateQueryData(rowAction: RowAction) {
        if (rowAction.type == ActionType.ON_SAVE || rowAction.type == ActionType.ON_TEXT_CHANGE) {
            if (rowAction.value != null) {
                queryData[rowAction.id] = rowAction.value!!
            } else {
                queryData.remove(rowAction.id)
            }
            updateSearch()
        } else if (rowAction.type == ActionType.ON_CLEAR) {
            clearQueryData()
        }
    }

    private fun clearQueryData() {
        queryData.clear()
        updateSearch()
        performSearch()
    }

    private fun updateSearch() {
        if (_screenState.value is SearchList) {
            val currentSearchList = _screenState.value as SearchList
            _screenState.value =
                currentSearchList.copy(
                    searchForm = currentSearchList.searchForm.copy(
                        queryHasData = queryData.isNotEmpty(),
                    ),
                )
        }
    }

    fun fetchListResults(onPagedListReady: (LiveData<PagedList<SearchTeiModel>>?) -> Unit) {
        viewModelScope.launch {
            val resultPagedList = when {
                searching -> loadSearchResults()
                displayFrontPageList() -> loadDisplayInListResults()
                else -> null
            }
            onPagedListReady(resultPagedList)
        }
    }

    private suspend fun loadSearchResults() = withContext(dispatchers.io()) {
        return@withContext searchRepository.searchTrackedEntities(
            SearchParametersModel(
                selectedProgram = searchRepository.getProgram(initialProgramUid),
                queryData = queryData,
            ),
            searching && networkUtils.isOnline(),
        )
    }

    private suspend fun loadDisplayInListResults() = withContext(dispatchers.io()) {
        return@withContext searchRepository.searchTrackedEntities(
            SearchParametersModel(
                selectedProgram = searchRepository.getProgram(initialProgramUid),
                queryData = queryData,
            ),
            false,
        )
    }

    fun fetchGlobalResults(): LiveData<PagedList<SearchTeiModel>>? {
        return if (searching) {
            searchRepository.searchTrackedEntities(
                SearchParametersModel(
                    selectedProgram = null,
                    queryData = queryData,
                ),
                searching && networkUtils.isOnline(),
            )
        } else {
            null
        }
    }

    fun fetchMapResults() {
        viewModelScope.launch {
            val result = async(context = dispatchers.io()) {
                mapDataRepository.getTrackerMapData(
                    searchRepository.getProgram(initialProgramUid),
                    queryData,
                )
            }
            try {
                _mapResults.value = result.await()
            } catch (e: Exception) {
                Timber.e(e)
            }
            searching = false
        }
    }

    fun onSearchClick(onMinAttributes: (Int) -> Unit = {}) {
        searchRepository.clearFetchedList()
        performSearch(onMinAttributes)
    }

    private fun performSearch(onMinAttributes: (Int) -> Unit = {}) {
        viewModelScope.launch {
            if (canPerformSearch()) {
                searching = queryData.isNotEmpty()
                when (_screenState.value?.screenState) {
                    SearchScreenState.LIST -> {
                        SearchIdlingResourceSingleton.increment()
                        setListScreen()
                        _refreshData.value = Unit
                    }

                    SearchScreenState.MAP -> {
                        SearchIdlingResourceSingleton.increment()
                        _refreshData.value = Unit
                        setMapScreen()
                        fetchMapResults()
                    }

                    else -> searching = false
                }
            } else {
                onMinAttributes(
                    searchRepository.getProgram(initialProgramUid)
                        ?.minAttributesRequiredToSearch()
                        ?: 0,
                )
            }
        }
    }

    private fun canPerformSearch(): Boolean {
        return minAttributesToSearchCheck() || displayFrontPageList()
    }

    private fun minAttributesToSearchCheck(): Boolean {
        return searchRepository.getProgram(initialProgramUid)?.let { program ->
            program.minAttributesRequiredToSearch() ?: 0 <= queryData.size
        } ?: true
    }

    private fun displayFrontPageList(): Boolean {
        return searchRepository.getProgram(initialProgramUid)?.let { program ->
            program.displayFrontPageList() == true && queryData.isEmpty()
        } ?: false
    }

    fun canDisplayResult(itemCount: Int, onlineTooManyResults: Boolean): Boolean {
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
        _legacyInteraction.value = LegacyInteraction.OnEnrollClick(queryData)
    }

    fun onAddRelationship(teiUid: String, relationshipTypeUid: String?, online: Boolean) {
        _legacyInteraction.value = LegacyInteraction.OnAddRelationship(
            teiUid,
            relationshipTypeUid,
            online,
        )
    }

    fun onSyncIconClick(teiUid: String) {
        _legacyInteraction.value = (LegacyInteraction.OnSyncIconClick(teiUid))
    }

    fun onDownloadTei(teiUid: String, enrollmentUid: String?, reason: String? = null) {
        viewModelScope.launch {
            val result = async(dispatchers.io()) {
                searchRepository.download(teiUid, enrollmentUid, reason)
            }
            try {
                val downloadResult = result.await()
                if (downloadResult is TeiDownloadResult.TeiToEnroll) {
                    _legacyInteraction.value = LegacyInteraction.OnEnroll(
                        initialProgramUid,
                        downloadResult.teiUid,
                        queryData,
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
        _legacyInteraction.value = LegacyInteraction.OnTeiClick(teiUid, enrollmentUid, online)
    }

    fun onDataLoaded(
        programResultCount: Int,
        globalResultCount: Int? = null,
        isLandscape: Boolean = false,
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
            handleDisplayInListResult(hasProgramResults, isLandscape)
        } else {
            handleInitWithoutData()
        }

        SearchIdlingResourceSingleton.decrement()
    }

    private fun handleDisplayInListResult(hasProgramResults: Boolean, isLandscape: Boolean) {
        val result = when {
            !hasProgramResults && searchRepository.canCreateInProgramWithoutSearch() ->
                listOf(
                    SearchResult(
                        SearchResult.SearchResultType.SEARCH_OR_CREATE,
                        searchRepository.getTrackedEntityType().displayName(),
                    ),
                )

            else -> listOf(SearchResult(SearchResult.SearchResultType.NO_MORE_RESULTS_OFFLINE))
        }

        if (result.isEmpty() && _filtersActive.value == false) {
            setSearchScreen()
        }

        _dataResult.value = result
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
        _dataResult.value = result
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
        _dataResult.value = result
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

    fun mapDataFetched() {
        SearchIdlingResourceSingleton.decrement()
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
        _filtersOpened.value = filtersOpened
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
            _screenState.value = it
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
        _legacyInteraction.value = null
    }
}
