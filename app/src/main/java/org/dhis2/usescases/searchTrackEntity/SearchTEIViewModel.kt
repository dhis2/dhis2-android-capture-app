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
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator
import timber.log.Timber

class SearchTEIViewModel(
    private val initialProgramUid: String?,
    initialQuery: MutableMap<String, String>?,
    private val presenter: SearchTEContractsModule.Presenter,
    private val searchRepository: SearchRepository,
    private val searchNavPageConfigurator: SearchPageConfigurator,
    private val mapDataRepository: MapDataRepository,
    private val networkUtils: NetworkUtils,
    private val dispatchers: DispatcherProvider
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

    val createButtonScrollVisibility = MutableLiveData(true)
    val isScrollingDown = MutableLiveData(false)

    private var searching: Boolean = false

    private val _downloadResult = MutableLiveData<TeiDownloadResult>()
    val downloadResult: LiveData<TeiDownloadResult> = _downloadResult

    private val _dataResult = MutableLiveData<List<SearchResult>>()
    val dataResult: LiveData<List<SearchResult>> = _dataResult

    init {
        viewModelScope.launch {
            _pageConfiguration.value = searchNavPageConfigurator.initVariables()
        }
    }

    fun setListScreen() {
        val displayFrontPageList =
            searchRepository.getProgram(initialProgramUid)?.displayFrontPageList() ?: true
        val shouldOpenSearch = !displayFrontPageList &&
            !searchRepository.canCreateInProgramWithoutSearch() &&
            !searching
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

    fun refreshData() {
        onSearchClick()
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
    }

    private fun updateSearch() {
        if (_screenState.value is SearchForm) {
            _screenState.value =
                (_screenState.value as SearchForm).copy(queryHasData = queryData.isNotEmpty())
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
        } ?: true
    }

    fun canDisplayResult(itemCount: Int): Boolean {
        return searchRepository.getProgram(initialProgramUid)?.maxTeiCountToReturn()
            ?.takeIf { it != 0 }
            ?.let { maxTeiCount ->
                itemCount <= maxTeiCount
            } ?: true
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

        val isSearching = _screenState.value.takeIf { it is SearchList }?.let {
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
        }

        SearchIdlingResourceSingleton.decrement()
    }

    private fun handleDisplayInListResult(hasProgramResults: Boolean, isLandscape: Boolean) {
        val result = when {
            hasProgramResults ->
                listOf(SearchResult(SearchResult.SearchResultType.NO_MORE_RESULTS))
            !hasProgramResults && searchRepository.canCreateInProgramWithoutSearch() ->
                listOf(
                    SearchResult(
                        SearchResult.SearchResultType.SEARCH_OR_CREATE,
                        searchRepository.getTrackedEntityType().displayName()
                    )
                )
            else -> listOf()
        }

        if (result.isEmpty()) {
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
            hasGlobalResults == null && searchRepository.getProgram(initialProgramUid) != null -> {
                listOf(
                    SearchResult(
                        SearchResult.SearchResultType.SEARCH_OUTSIDE,
                        searchRepository.getProgram(initialProgramUid)?.displayName()
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
}
