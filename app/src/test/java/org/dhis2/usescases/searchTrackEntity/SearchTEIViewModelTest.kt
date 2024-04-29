package org.dhis2.usescases.searchTrackEntity

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.mapbox.geojson.BoundingBox
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.search.SearchParametersModel
import org.dhis2.form.model.ActionType
import org.dhis2.form.model.RowAction
import org.dhis2.maps.geometry.mapper.EventsByProgramStage
import org.dhis2.maps.usecases.MapStyleConfiguration
import org.dhis2.usescases.searchTrackEntity.listView.SearchResult.SearchResultType
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SearchTEIViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: SearchTEIViewModel
    private val initialProgram = "programUid"
    private val initialQuery = mutableMapOf<String, String>()
    private val repository: SearchRepository = mock()
    private val pageConfigurator: SearchPageConfigurator = mock()
    private val mapDataRepository: MapDataRepository = mock()
    private val networkUtils: NetworkUtils = mock()
    private val mapStyleConfiguration: MapStyleConfiguration = mock()

    @ExperimentalCoroutinesApi
    private val testingDispatcher = StandardTestDispatcher()

    @ExperimentalCoroutinesApi
    @Before
    fun setUp() {
        Dispatchers.setMain(testingDispatcher)
        whenever(pageConfigurator.initVariables()) doReturn pageConfigurator
        setCurrentProgram(testingProgram())
        whenever(repository.canCreateInProgramWithoutSearch()) doReturn true
        whenever(repository.getTrackedEntityType()) doReturn testingTrackedEntityType()
        whenever(repository.filtersApplyOnGlobalSearch()) doReturn true
        viewModel = SearchTEIViewModel(
            initialProgram,
            initialQuery,
            repository,
            pageConfigurator,
            mapDataRepository,
            networkUtils,
            object : DispatcherProvider {
                override fun io(): CoroutineDispatcher {
                    return testingDispatcher
                }

                override fun computation(): CoroutineDispatcher {
                    return testingDispatcher
                }

                override fun ui(): CoroutineDispatcher {
                    return testingDispatcher
                }
            },
            mapStyleConfiguration,
        )
        testingDispatcher.scheduler.advanceUntilIdle()
    }

    @ExperimentalCoroutinesApi
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Should set SearchList if displayFrontPageList is true`() {
        viewModel.setListScreen()

        val screenState = viewModel.screenState.value
        assertTrue(screenState is SearchList)
    }

    @Test
    fun `Should set SearchList if displayFrontPageList is false and can create`() {
        setCurrentProgram(testingProgram(displayFrontPageList = false))
        setAllowCreateBeforeSearch(true)
        viewModel.setListScreen()

        val screenState = viewModel.screenState.value
        assertTrue(screenState is SearchList)
    }

    @Test
    fun `Should set SearchForm if displayFrontPageList is false and can not create`() {
        setCurrentProgram(testingProgram(displayFrontPageList = false))
        setAllowCreateBeforeSearch(false)
        viewModel.setListScreen()

        val screenState = viewModel.screenState.value
        assertTrue(screenState is SearchList)
        assertTrue((screenState as SearchList).searchForm.isOpened)
    }

    @Test
    fun `Should set Map screen`() {
        viewModel.setMapScreen()

        val screenState = viewModel.screenState.value
        assertTrue(screenState?.screenState == SearchScreenState.MAP)
    }

    @Test
    fun `Should set Analytics screen`() {
        viewModel.setAnalyticsScreen()

        val screenState = viewModel.screenState.value
        assertTrue(screenState is SearchAnalytics)
    }

    @Test
    fun `Should set Search screen in portrait`() {
        viewModel.setSearchScreen()

        val screenState = viewModel.screenState.value
        assertTrue(screenState is SearchList)
        assertTrue((screenState as SearchList).searchForm.isOpened)
    }

    @Test
    fun `Should set Search screen in landscape`() {
        viewModel.setSearchScreen()

        val screenState = viewModel.screenState.value
        assertTrue(screenState is SearchList)
    }

    @Test
    fun `Should set previous screen`() {
        viewModel.setListScreen()
        viewModel.setSearchScreen()
        viewModel.setPreviousScreen()

        val screenStateA = viewModel.screenState.value
        assertTrue(screenStateA?.screenState == SearchScreenState.LIST)

        viewModel.setMapScreen()
        viewModel.setSearchScreen()
        viewModel.setPreviousScreen()

        val screenStateB = viewModel.screenState.value
        assertTrue(screenStateB?.screenState == SearchScreenState.MAP)
    }

    @Test
    fun `Should update query data`() {
        viewModel.updateQueryData(
            RowAction(
                id = "testingUid",
                value = "testingValue",
                type = ActionType.ON_SAVE,
            ),
        )

        val queryData = viewModel.queryData

        assertTrue(queryData.isNotEmpty())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `Should return local results LiveData if not searching and displayInList is true`() {
        val testingProgram = testingProgram()
        setCurrentProgram(testingProgram)
        viewModel.fetchListResults {}
        testingDispatcher.scheduler.advanceUntilIdle()
        verify(repository).searchTrackedEntities(
            SearchParametersModel(
                selectedProgram = testingProgram,
                queryData = mutableMapOf(),
            ),
            false,
        )
    }

    @Test
    fun `Should return null if not searching and displayInList is false`() {
        val testingProgram = testingProgram(displayFrontPageList = false)
        setCurrentProgram(testingProgram)
        viewModel.fetchListResults {}

        verify(repository, times(0)).searchTrackedEntities(
            SearchParametersModel(
                selectedProgram = testingProgram,
                queryData = mutableMapOf(),
            ),
            true,
        )

        verify(repository, times(0)).searchTrackedEntities(
            SearchParametersModel(
                selectedProgram = testingProgram,
                queryData = mutableMapOf(),
            ),
            false,
        )
    }

    @Test
    fun `Should return null global results if not searching`() {
        viewModel.fetchListResults {
            assertTrue(it == null)
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `Should fetch map results`() {
        whenever(
            mapDataRepository.getTrackerMapData(
                testingProgram(),
                viewModel.queryData,
            ),
        ) doReturn TrackerMapData(
            mutableListOf(),
            EventsByProgramStage("tag", mapOf()),
            hashMapOf(),
            BoundingBox.fromLngLats(
                0.0,
                0.0,
                0.0,
                0.0,
            ),
            mutableListOf(),
            mutableMapOf(),
        )

        viewModel.fetchMapResults()
        testingDispatcher.scheduler.advanceUntilIdle()
        val mapResult = viewModel.mapResults.value
        assertTrue(mapResult != null)
    }

    @Test
    fun `Should use callback to perform min attributes warning`() {
        setCurrentProgram(testingProgram())
        viewModel.onSearchClick {
            assertTrue(true)
        }
    }

    @Test
    fun `Should search for list result`() {
        setCurrentProgram(testingProgram())
        viewModel.setListScreen()
        viewModel.setSearchScreen()
        viewModel.updateQueryData(
            RowAction(
                id = "testingUid",
                value = "testingValue",
                type = ActionType.ON_SAVE,
            ),
        )
        viewModel.onSearchClick {
            assertTrue(false)
        }

        assertTrue(viewModel.refreshData.value != null)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `Should search for map result`() {
        whenever(
            mapDataRepository.getTrackerMapData(
                testingProgram(),
                viewModel.queryData,
            ),
        ) doReturn TrackerMapData(
            mutableListOf(),
            EventsByProgramStage("tag", mapOf()),
            hashMapOf(),
            BoundingBox.fromLngLats(
                0.0,
                0.0,
                0.0,
                0.0,
            ),
            mutableListOf(),
            mutableMapOf(),
        )
        setCurrentProgram(testingProgram())
        viewModel.setMapScreen()
        viewModel.setSearchScreen()
        viewModel.updateQueryData(
            RowAction(
                id = "testingUid",
                value = "testingValue",
                type = ActionType.ON_SAVE,
            ),
        )
        viewModel.onSearchClick {
            assertTrue(false)
        }

        testingDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.refreshData.value != null)
        verify(mapDataRepository).getTrackerMapData(
            testingProgram(),
            viewModel.queryData,
        )
    }

    @Test
    fun `Should filter query data for new program`() {
        viewModel.queryDataByProgram("programUid")
        verify(repository).filterQueryForProgram(viewModel.queryData, "programUid")
    }

    @Test
    fun `Should enroll on click`() {
        viewModel.onEnrollClick()
        testingDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.legacyInteraction.value is LegacyInteraction.OnEnrollClick)
    }

    @Test
    fun `Should add relationship`() {
        viewModel.onAddRelationship("teiUd", "relationshipTypeUid", false)
        testingDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.legacyInteraction.value is LegacyInteraction.OnAddRelationship)
    }

    @Test
    fun `Should show sync icon`() {
        viewModel.onSyncIconClick("teiUid")
        testingDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.legacyInteraction.value is LegacyInteraction.OnSyncIconClick)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `Should downloadTei`() {
        viewModel.onDownloadTei("teiUid", null)
        testingDispatcher.scheduler.advanceUntilIdle()
        verify(repository).download("teiUid", null, null)
    }

    @Test
    fun `Should click on TEI`() {
        viewModel.onTeiClick("teiUid", null, true)
        testingDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.legacyInteraction.value is LegacyInteraction.OnTeiClick)
    }

    @Test
    fun `Should return no more result for displayInList true`() {
        viewModel.onDataLoaded(2)
        viewModel.dataResult.value?.apply {
            assertTrue(isNotEmpty())
            assertTrue(size == 1)
            assertTrue(first().type == SearchResultType.NO_MORE_RESULTS_OFFLINE)
        }
    }

    @Test
    fun `Should return search or create results for displayInList true`() {
        setAllowCreateBeforeSearch(true)
        viewModel.onDataLoaded(0)
        viewModel.dataResult.value?.apply {
            assertTrue(isNotEmpty())
            assertTrue(size == 1)
            assertTrue(first().type == SearchResultType.SEARCH_OR_CREATE)
        }
    }

    @Test
    fun `Should return no more results offline and not set SearchScreen for displayInList true`() {
        setAllowCreateBeforeSearch(false)
        viewModel.onDataLoaded(0)
        viewModel.dataResult.value?.apply {
            assertTrue(isNotEmpty())
            assertTrue(size == 1)
            assertTrue(first().type == SearchResultType.NO_MORE_RESULTS_OFFLINE)
        }
        assertTrue(viewModel.screenState.value !is SearchList)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `Should return too many results for search`() {
        setCurrentProgram(testingProgram(maxTeiCountToReturn = 1))
        setAllowCreateBeforeSearch(false)
        performSearch()
        viewModel.onDataLoaded(2)
        viewModel.dataResult.value?.apply {
            assertTrue(isNotEmpty())
            assertTrue(size == 1)
            assertTrue(first().type == SearchResultType.TOO_MANY_RESULTS)
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `Should return search outside result for search`() {
        setCurrentProgram(testingProgram(maxTeiCountToReturn = 1))
        setAllowCreateBeforeSearch(false)
        whenever(
            repository.filterQueryForProgram(viewModel.queryData, null),
        ) doReturn mapOf("field" to "value")

        performSearch()
        viewModel.onDataLoaded(1)
        viewModel.dataResult.value?.apply {
            assertTrue(isNotEmpty())
            assertTrue(size == 1)
            assertTrue(first().type == SearchResultType.SEARCH_OUTSIDE)
        }
    }

    @Test
    fun `Should return unable to search outside result for search`() {
        setCurrentProgram(testingProgram(maxTeiCountToReturn = 1))
        setAllowCreateBeforeSearch(false)
        whenever(repository.filterQueryForProgram(viewModel.queryData, null)) doReturn mapOf()
        whenever(repository.trackedEntityTypeFields()) doReturn listOf("Field_1", "Field_2")

        performSearch()
        viewModel.onDataLoaded(1)
        viewModel.dataResult.value?.apply {
            assertTrue(isNotEmpty())
            assertTrue(size == 1)
            assertTrue(first().type == SearchResultType.UNABLE_SEARCH_OUTSIDE)
        }
    }

    @Test
    fun `Should return no more results for global search`() {
        setCurrentProgram(testingProgram(maxTeiCountToReturn = 1))
        setAllowCreateBeforeSearch(false)
        performSearch()
        viewModel.onDataLoaded(1, 1)
        viewModel.dataResult.value?.apply {
            assertTrue(isNotEmpty())
            assertTrue(size == 1)
            assertTrue(first().type == SearchResultType.NO_MORE_RESULTS)
        }
    }

    @Test
    fun `Should return no results for search`() {
        setCurrentProgram(testingProgram())
        setAllowCreateBeforeSearch(false)
        performSearch()
        viewModel.onDataLoaded(0, 0)
        viewModel.dataResult.value?.apply {
            assertTrue(isNotEmpty())
            assertTrue(size == 1)
            assertTrue(first().type == SearchResultType.NO_RESULTS)
        }
    }

    @Test
    fun `Should return init search`() {
        setCurrentProgram(testingProgram(displayFrontPageList = false))
        setAllowCreateBeforeSearch(false)
        viewModel.onDataLoaded(0, null)
        viewModel.dataResult.value?.apply {
            assertTrue(isNotEmpty())
            assertTrue(size == 1)
            assertTrue(first().type == SearchResultType.SEARCH)
        }
    }

    @Test
    fun `Should return no more results for global search when filter do not apply for it`() {
        setCurrentProgram(testingProgram(maxTeiCountToReturn = 1))
        setAllowCreateBeforeSearch(false)
        whenever(repository.filtersApplyOnGlobalSearch()) doReturn false
        performSearch()
        viewModel.onDataLoaded(1, 1)
        viewModel.dataResult.value?.apply {
            assertTrue(isNotEmpty())
            assertTrue(size == 1)
            assertTrue(first().type == SearchResultType.NO_MORE_RESULTS)
        }
    }

    @Test
    fun `Should close keyboard and filters`() {
        viewModel.onBackPressed(
            isPortrait = true,
            searchOrFilterIsOpen = true,
            keyBoardIsOpen = true,
            goBackCallback = { assertTrue(false) },
            closeSearchOrFilterCallback = { assertTrue(true) },
            closeKeyboardCallback = { assertTrue(true) },
        )
    }

    @Test
    fun `Should close filters`() {
        viewModel.onBackPressed(
            isPortrait = true,
            searchOrFilterIsOpen = true,
            keyBoardIsOpen = false,
            goBackCallback = { assertTrue(false) },
            closeSearchOrFilterCallback = { assertTrue(true) },
            closeKeyboardCallback = { assertTrue(false) },
        )
    }

    @Test
    fun `Should close keyboard and go back`() {
        viewModel.onBackPressed(
            isPortrait = true,
            searchOrFilterIsOpen = false,
            keyBoardIsOpen = true,
            goBackCallback = { assertTrue(true) },
            closeSearchOrFilterCallback = { assertTrue(false) },
            closeKeyboardCallback = { assertTrue(true) },
        )
    }

    @Test
    fun `Should go back`() {
        viewModel.onBackPressed(
            isPortrait = true,
            searchOrFilterIsOpen = false,
            keyBoardIsOpen = false,
            goBackCallback = { assertTrue(true) },
            closeSearchOrFilterCallback = { assertTrue(false) },
            closeKeyboardCallback = { assertTrue(false) },
        )
    }

    @Test
    fun `Should display navigation bar`() {
        viewModel.setListScreen()
        assertTrue(viewModel.canDisplayBottomNavigationBar())
        viewModel.setMapScreen()
        assertTrue(viewModel.canDisplayBottomNavigationBar())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `Should return break the glass result when downloading`() {
        whenever(
            repository.download(
                "teiUid",
                null,
                null,
            ),
        ) doReturn TeiDownloadResult.BreakTheGlassResult("teiUid", null)

        viewModel.onDownloadTei("teiUid", null)
        testingDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.downloadResult.value is TeiDownloadResult.BreakTheGlassResult)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `Should enroll tei in current program`() {
        whenever(
            repository.download(
                "teiUid",
                null,
                null,
            ),
        ) doReturn TeiDownloadResult.TeiToEnroll("teiUid")

        viewModel.onDownloadTei("teiUid", null)
        testingDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.downloadResult.value == null)
        testingDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.legacyInteraction.value is LegacyInteraction.OnEnroll)
    }

    @Test
    fun `should return selected program uid and set theme`() {
        val programs = listOf(
            ProgramSpinnerModel("program1", "program1", false),
            ProgramSpinnerModel("program2", "program2", false),
        )

        viewModel.onProgramSelected(2, programs) {
            assertTrue(it == "program2")
        }
        verify(repository).setCurrentTheme(programs[1])
    }

    @Test
    fun `should return first program uid and set theme`() {
        val programs = listOf(
            ProgramSpinnerModel("program1", "program1", false),
        )

        viewModel.onProgramSelected(2, programs) {
            assertTrue(it == "program1")
        }
        verify(repository).setCurrentTheme(programs[0])
    }

    @Test
    fun `should return null uid and set theme`() {
        viewModel.onProgramSelected(0, listOf()) {
            assertTrue(it == null)
        }
        verify(repository).setCurrentTheme(null)
    }

    private fun testingProgram(
        displayFrontPageList: Boolean = true,
        minAttributesToSearch: Int = 1,
        maxTeiCountToReturn: Int? = null,
    ) = Program.builder()
        .uid("initialProgram")
        .displayName("programName")
        .displayFrontPageList(displayFrontPageList)
        .minAttributesRequiredToSearch(minAttributesToSearch)
        .trackedEntityType(TrackedEntityType.builder().uid("teTypeUid").build())
        .apply {
            maxTeiCountToReturn?.let {
                maxTeiCountToReturn(maxTeiCountToReturn)
            }
        }
        .build()

    private fun testingTrackedEntityType() = TrackedEntityType.builder()
        .uid("teiTypeUid")
        .displayName("teTypeName")
        .build()

    @ExperimentalCoroutinesApi
    private fun performSearch() {
        viewModel.updateQueryData(
            RowAction(
                id = "testingUid",
                value = "testingValue",
                type = ActionType.ON_SAVE,
            ),
        )
        viewModel.setListScreen()
        viewModel.setSearchScreen()
        viewModel.onSearchClick()
        testingDispatcher.scheduler.advanceUntilIdle()
    }

    private fun setAllowCreateBeforeSearch(allow: Boolean) {
        whenever(
            repository.canCreateInProgramWithoutSearch(),
        ) doReturn allow
    }

    private fun setCurrentProgram(program: Program) {
        whenever(
            repository.getProgram(initialProgram),
        ) doReturn program
    }
}
