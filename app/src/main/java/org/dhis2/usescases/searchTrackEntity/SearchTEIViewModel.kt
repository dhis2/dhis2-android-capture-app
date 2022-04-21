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
import org.dhis2.data.search.SearchParametersModel
import org.dhis2.form.model.ActionType
import org.dhis2.form.model.DispatcherProvider
import org.dhis2.form.model.RowAction
import org.dhis2.usescases.searchTrackEntity.listView.SearchResult
import org.dhis2.usescases.searchTrackEntity.ui.UnableToSearchOutsideData
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator
import org.hisp.dhis.android.core.program.Program
import timber.log.Timber

const val TEI_TYPE_SEARCH_MAX_RESULTS = 5

class SearchTEIViewModel(
    private val initialProgramUid: String?,
    initialQuery: MutableMap<String, String>?,
    private val presenter: SearchTEContractsModule.Presenter,
    private val searchRepository: SearchRepository,
    private val searchNavPageConfigurator: SearchPageConfigurator,
    private val mapDataRepository: MapDataRepository,
    private val networkUtils: NetworkUtils,
    private val dispatchers: DispatcherProvider,
    private val customDispatcher: dispatch.core.DispatcherProvider
) : ViewModel() {

    private val _pageConfiguration = MutableLiveData<NavigationPageConfigurator>()
    val pageConfiguration: LiveData<NavigationPageConfigurator> = _pageConfiguration

    val queryData = mutableMapOf<String, String>().apply {
        initialQuery?.let { putAll(it) }
    }

    private val _refreshData = MutableLiveData(Unit)
    val refreshData: LiveData<Unit> = _refreshData

    private val _mapResults = MutableLiveData<TrackerMapData>()
    val mapResults: LiveData<TrackerMapData> = _mapResults

    private val _screenState = MutableLiveData<SearchTEScreenState>()
    val screenState: LiveData<SearchTEScreenState> = _screenState

    val createButtonScrollVisibility = MutableLiveData(false)
    val isScrollingDown = MutableLiveData(false)

    private var searching: Boolean = false
    private var _filtersActive = MutableLiveData(false)

    private val _downloadResult = MutableLiveData<TeiDownloadResult>()
    val downloadResult: LiveData<TeiDownloadResult> = _downloadResult

    private val _dataResult = MutableLiveData<List<SearchResult>>()
    val dataResult: LiveData<List<SearchResult>> = _dataResult

    private val _filtersOpened = MutableLiveData(false)
    val filtersOpened: LiveData<Boolean> = _filtersOpened

    init {
        viewModelScope.launch {
            createButtonScrollVisibility.value = searchRepository.canCreateInProgramWithoutSearch()
            _pageConfiguration.value = searchNavPageConfigurator.initVariables()
        }
    }

    fun setListScreen() {
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
        _screenState.value = when {
            shouldOpenSearch ->
                SearchForm(
                    previousSate = _screenState.value?.screenState ?: SearchScreenState.LIST,
                    queryHasData = queryData.isNotEmpty(),
                    minAttributesToSearch = searchRepository.getProgram(initialProgramUid)
                        ?.minAttributesRequiredToSearch()
                        ?: 1,
                    isForced = true
                )
            else ->
                SearchList(
                    previousSate = _screenState.value?.screenState ?: SearchScreenState.NONE,
                    listType = SearchScreenState.LIST,
                    displayFrontPageList = searchRepository.getProgram(initialProgramUid)
                        ?.displayFrontPageList()
                        ?: false,
                    canCreateWithoutSearch = searchRepository.canCreateInProgramWithoutSearch(),
                    queryHasData = queryData.isNotEmpty(),
                    minAttributesToSearch = searchRepository.getProgram(initialProgramUid)
                        ?.minAttributesRequiredToSearch()
                        ?: 0,
                    isSearching = searching
                )
        }
    }

    fun setMapScreen() {
        _screenState.value = SearchList(
            previousSate = _screenState.value?.screenState ?: SearchScreenState.NONE,
            listType = SearchScreenState.MAP,
            displayFrontPageList = searchRepository.getProgram(initialProgramUid)
                ?.displayFrontPageList()
                ?: false,
            canCreateWithoutSearch = searchRepository.canCreateInProgramWithoutSearch(),
            queryHasData = queryData.isNotEmpty(),
            minAttributesToSearch = searchRepository.getProgram(initialProgramUid)
                ?.minAttributesRequiredToSearch()
                ?: 0,
            isSearching = searching
        )
    }

    fun setAnalyticsScreen() {
        _screenState.value = SearchAnalytics(
            previousSate = _screenState.value?.screenState ?: SearchScreenState.NONE
        )
    }

    fun setSearchScreen(isLandscapeMode: Boolean) {
        if (isLandscapeMode) {
            setListScreen()
        } else {
            _screenState.value = SearchForm(
                previousSate = _screenState.value?.screenState ?: SearchScreenState.NONE,
                queryHasData = queryData.isNotEmpty(),
                minAttributesToSearch = searchRepository.getProgram(initialProgramUid)
                    ?.minAttributesRequiredToSearch()
                    ?: 0
            )
        }
    }

    fun setPreviousScreen(isLandscapeMode: Boolean) {
        when (_screenState.value?.previousSate) {
            SearchScreenState.LIST -> setListScreen()
            SearchScreenState.MAP -> setMapScreen()
            SearchScreenState.SEARCHING -> setSearchScreen(isLandscapeMode)
            SearchScreenState.ANALYTICS -> setAnalyticsScreen()
            else -> {
            }
        }
    }

    fun updateActiveFilters(filtersActive: Boolean) {
        _filtersActive.value = filtersActive
    }

    fun refreshData() {
        onSearchClick()
    }

    fun updateQueryData(rowAction: RowAction, isLandscape: Boolean = false) {
        if (rowAction.type == ActionType.ON_SAVE || rowAction.type == ActionType.ON_TEXT_CHANGE) {
            if (rowAction.value != null) {
                queryData[rowAction.id] = rowAction.value!!
            } else {
                queryData.remove(rowAction.id)
            }
            updateSearch(isLandscape)
        } else if (rowAction.type == ActionType.ON_CLEAR) {
            clearQueryData(isLandscape)
        }
    }

    private fun clearQueryData(isLandscape: Boolean = false) {
        queryData.clear()
        updateSearch(isLandscape)
    }

    private fun updateSearch(isLandscape: Boolean) {
        when (isLandscape) {
            true -> if (_screenState.value is SearchList) {
                _screenState.value =
                    (_screenState.value as SearchList).copy(queryHasData = queryData.isNotEmpty())
            }
            false -> if (_screenState.value is SearchForm) {
                _screenState.value =
                    (_screenState.value as SearchForm).copy(queryHasData = queryData.isNotEmpty())
            }
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
                queryData = queryData
            ),
            searching && networkUtils.isOnline()
        )
    }

    private suspend fun loadDisplayInListResults() = withContext(dispatchers.io()) {
        return@withContext searchRepository.searchTrackedEntities(
            SearchParametersModel(
                selectedProgram = searchRepository.getProgram(initialProgramUid),
                queryData = queryData
            ),
            false
        )
    }

    fun fetchGlobalResults(): LiveData<PagedList<SearchTeiModel>>? {
        return if (searching) {
            searchRepository.searchTrackedEntities(
                SearchParametersModel(
                    selectedProgram = null,
                    queryData = queryData
                ),
                searching && networkUtils.isOnline()
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
                    queryData
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
        viewModelScope.launch {
            if (canPerformSearch()) {
                searching = queryData.isNotEmpty()
                val currentScreenState = if (_screenState.value is SearchForm) {
                    _screenState.value?.previousSate
                } else {
                    _screenState.value?.screenState
                }

                when (currentScreenState) {
                    SearchScreenState.LIST -> {
                        SearchIdlingResourceSingleton.increment()
                        setListScreen()
                        _refreshData.value = Unit
                    }
                    SearchScreenState.MAP -> {
                        SearchIdlingResourceSingleton.increment()
                        setMapScreen()
                        fetchMapResults()
                    }
                    else -> searching = false
                }
            } else {
                onMinAttributes(
                    searchRepository.getProgram(initialProgramUid)
                        ?.minAttributesRequiredToSearch()
                        ?: 0
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

    fun canDisplayResult(itemCount: Int): Boolean {
        return when (initialProgramUid) {
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
        presenter.onEnrollClick(HashMap(queryData))
    }

    fun onAddRelationship(teiUid: String, relationshipTypeUid: String?, online: Boolean) {
        presenter.addRelationship(teiUid, relationshipTypeUid, online)
    }

    fun onSyncIconClick(teiUid: String) {
        presenter.onSyncIconClick(teiUid)
    }

    fun onDownloadTei(teiUid: String, enrollmentUid: String?, reason: String? = null) {
        viewModelScope.launch {
            val result = async(dispatchers.io()) {
                searchRepository.download(teiUid, enrollmentUid, reason)
            }
            try {
                val downloadResult = result.await()
                if (downloadResult is TeiDownloadResult.TeiToEnroll) {
                    presenter.enroll(
                        searchRepository.getProgram(initialProgramUid)?.uid(),
                        downloadResult.teiUid,
                        hashMapOf<String, String>().apply { putAll(queryData) }
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
        presenter.onTEIClick(teiUid, enrollmentUid, online)
    }

    fun onDataLoaded(
        programResultCount: Int,
        globalResultCount: Int? = null,
        isLandscape: Boolean = false
    ) {
        val canDisplayResults = canDisplayResult(programResultCount)
        val hasProgramResults = programResultCount > 0
        val hasGlobalResults = globalResultCount?.let { it > 0 }

        val isSearching = _screenState.value?.takeIf { it is SearchList }?.let {
            (it as SearchList).isSearching
        } ?: false

        if (isSearching) {
            handleSearchResult(
                canDisplayResults,
                hasProgramResults,
                hasGlobalResults
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
                        searchRepository.getTrackedEntityType().displayName()
                    )
                )
            else -> listOf(SearchResult(SearchResult.SearchResultType.NO_MORE_RESULTS_OFFLINE))
        }

        if (result.isEmpty() && _filtersActive.value == false) {
            setSearchScreen(isLandscape)
        }

        _dataResult.value = result
    }

    private fun handleSearchResult(
        canDisplayResults: Boolean,
        hasProgramResults: Boolean,
        hasGlobalResults: Boolean?
    ) {
        val result = when {
            !canDisplayResults -> {
                listOf(SearchResult(SearchResult.SearchResultType.TOO_MANY_RESULTS))
            }
            hasGlobalResults == null && searchRepository.getProgram(initialProgramUid) != null &&
                searchRepository.filterQueryForProgram(queryData, null).isNotEmpty() -> {
                listOf(
                    SearchResult(
                        SearchResult.SearchResultType.SEARCH_OUTSIDE,
                        searchRepository.getProgram(initialProgramUid)?.displayName()

                    )
                )
            }
            hasGlobalResults == null && searchRepository.getProgram(initialProgramUid) != null &&
                searchRepository.trackedEntityTypeFields().isNotEmpty() -> {
                listOf(
                    SearchResult(
                        type = SearchResult.SearchResultType.UNABLE_SEARCH_OUTSIDE,
                        uiData = UnableToSearchOutsideData(
                            trackedEntityTypeAttributes =
                                searchRepository.trackedEntityTypeFields(),
                            trackedEntityTypeName =
                                searchRepository.trackedEntityType.displayName()!!
                        )
                    )
                )
            }
            hasProgramResults || hasGlobalResults == true ->
                listOf(SearchResult(SearchResult.SearchResultType.NO_MORE_RESULTS))
            else ->
                listOf(SearchResult(SearchResult.SearchResultType.NO_RESULTS))
        }
        _dataResult.value = result
    }

    private fun handleInitWithoutData() {
        val result = when (searchRepository.canCreateInProgramWithoutSearch()) {
            true -> listOf(
                SearchResult(
                    SearchResult.SearchResultType.SEARCH_OR_CREATE,
                    searchRepository.trackedEntityType.displayName()
                )
            )
            false -> listOf(
                SearchResult(
                    SearchResult.SearchResultType.SEARCH,
                    searchRepository.trackedEntityType.displayName()
                )
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
        closeKeyboardCallback: () -> Unit
    ) {
        val searchScreenIsForced = _screenState.value?.let {
            if (it is SearchForm) {
                it.isForced
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
            it is SearchList || it is SearchMap
        } ?: false
    }

    fun mapDataFetched() {
        SearchIdlingResourceSingleton.decrement()
    }

    fun onProgramSelected(
        programIndex: Int,
        programs: List<Program>,
        onProgramChanged: (selectedProgramUid: String?) -> Unit
    ) {
        val selectedProgram = when {
            programIndex > 0 ->
                programs.takeIf { it.size > 1 }?.let { it[programIndex - 1] }
                    ?: programs.first()
            else -> null
        }
        searchRepository.setCurrentTheme(selectedProgram)

        if (selectedProgram?.uid() != initialProgramUid) {
            onProgramChanged(selectedProgram?.uid())
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
}
