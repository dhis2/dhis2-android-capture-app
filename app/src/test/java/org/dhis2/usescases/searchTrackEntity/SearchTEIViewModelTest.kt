package org.dhis2.usescases.searchTrackEntity

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.mapbox.geojson.BoundingBox
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.data.search.SearchParametersModel
import org.dhis2.form.model.ActionType
import org.dhis2.form.model.DispatcherProvider
import org.dhis2.form.model.RowAction
import org.dhis2.maps.geometry.mapper.EventsByProgramStage
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SearchTEIViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: SearchTEIViewModel
    private val initialProgram = "programUid"
    private val initialQuery = mutableMapOf<String, String>()
    private val presenter: SearchTEContractsModule.Presenter = mock()
    private val repository: SearchRepository = mock()
    private val pageConfigurator: SearchPageConfigurator = mock()
    private val mapDataRepository: MapDataRepository = mock()
    private val networkUtils: NetworkUtils = mock()

    @ExperimentalCoroutinesApi
    private val testingDispatcher = StandardTestDispatcher()

    @ExperimentalCoroutinesApi
    @Before
    fun setUp() {
        Dispatchers.setMain(testingDispatcher)
        whenever(pageConfigurator.initVariables()) doReturn pageConfigurator
        whenever(repository.getProgram("programUid")) doReturn testingProgram()
        viewModel = SearchTEIViewModel(
            initialProgram,
            initialQuery,
            presenter,
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
            }
        )
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
    fun `Should set SearchForm if displayFrontPageList is false`() {
        viewModel.setCurrentProgram(testingProgram(displayFrontPageList = false))
        viewModel.setListScreen()

        val screenState = viewModel.screenState.value
        assertTrue(screenState is SearchForm)
    }

    @Test
    fun `Should set Map screen`() {
        viewModel.setMapScreen()

        val screenState = viewModel.screenState.value
        assertTrue(screenState is SearchMap)
    }

    @Test
    fun `Should set Analytics screen`() {
        viewModel.setAnalyticsScreen()

        val screenState = viewModel.screenState.value
        assertTrue(screenState is SearchAnalytics)
    }

    @Test
    fun `Should set Search screen in portrait`() {
        viewModel.setSearchScreen(isLandscapeMode = false)

        val screenState = viewModel.screenState.value
        assertTrue(screenState is SearchForm)
    }

    @Test
    fun `Should set Search screen in landscape`() {
        viewModel.setSearchScreen(isLandscapeMode = true)

        val screenState = viewModel.screenState.value
        assertTrue(screenState is SearchList)
    }

    @Test
    fun `Should set previous screen`() {
        viewModel.setListScreen()
        viewModel.setSearchScreen(isLandscapeMode = false)
        viewModel.setPreviousScreen(isLandscapeMode = false)

        val screenStateA = viewModel.screenState.value
        assertTrue(screenStateA is SearchList)

        viewModel.setMapScreen()
        viewModel.setSearchScreen(isLandscapeMode = false)
        viewModel.setPreviousScreen(isLandscapeMode = false)

        val screenStateB = viewModel.screenState.value
        assertTrue(screenStateB is SearchMap)
    }

    @Test
    fun `Should update query data`() {
        viewModel.updateQueryData(
            RowAction(
                id = "testingUid",
                value = "testingValue",
                type = ActionType.ON_SAVE
            )
        )

        val queryData = viewModel.queryData

        assertTrue(queryData.isNotEmpty())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `Should return local results LiveData if not searching and displayInList is true`() {
        val testingProgram = testingProgram()
        viewModel.setCurrentProgram(testingProgram)
        viewModel.fetchListResults {}
        testingDispatcher.scheduler.advanceUntilIdle()
        verify(repository).searchTrackedEntities(
            SearchParametersModel(
                selectedProgram = testingProgram,
                queryData = mutableMapOf()
            ),
            false
        )
    }

    @Test
    fun `Should return null if not searching and displayInList is false`() {
        val testingProgram = testingProgram(displayFrontPageList = false)
        viewModel.setCurrentProgram(testingProgram)
        viewModel.fetchListResults {}

        verify(repository, times(0)).searchTrackedEntities(
            SearchParametersModel(
                selectedProgram = testingProgram,
                queryData = mutableMapOf()
            ),
            true
        )

        verify(repository, times(0)).searchTrackedEntities(
            SearchParametersModel(
                selectedProgram = testingProgram,
                queryData = mutableMapOf()
            ),
            false
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
                viewModel.selectedProgram.value,
                viewModel.queryData
            )
        ) doReturn TrackerMapData(
            mutableListOf(),
            EventsByProgramStage("tag", mapOf()),
            hashMapOf(),
            BoundingBox.fromLngLats(
                0.0,
                0.0,
                0.0,
                0.0
            ),
            mutableListOf(),
            mutableMapOf()
        )

        viewModel.fetchMapResults()
        testingDispatcher.scheduler.advanceUntilIdle()
        val mapResult = viewModel.mapResults.value
        assertTrue(mapResult != null)
    }

    @Test
    fun `Should use callback to perform min attributes warning`() {
        viewModel.setCurrentProgram(testingProgram())
        viewModel.onSearchClick {
            assertTrue(true)
        }
    }

    @Test
    fun `Should search for list result`() {
        viewModel.setCurrentProgram(testingProgram())
        viewModel.setListScreen()
        viewModel.setSearchScreen(isLandscapeMode = false)
        viewModel.updateQueryData(
            RowAction(
                id = "testingUid",
                value = "testingValue",
                type = ActionType.ON_SAVE
            )
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
                viewModel.selectedProgram.value,
                viewModel.queryData
            )
        ) doReturn TrackerMapData(
            mutableListOf(),
            EventsByProgramStage("tag", mapOf()),
            hashMapOf(),
            BoundingBox.fromLngLats(
                0.0,
                0.0,
                0.0,
                0.0
            ),
            mutableListOf(),
            mutableMapOf()
        )
        viewModel.setCurrentProgram(testingProgram())
        viewModel.setMapScreen()
        viewModel.setSearchScreen(isLandscapeMode = false)
        viewModel.updateQueryData(
            RowAction(
                id = "testingUid",
                value = "testingValue",
                type = ActionType.ON_SAVE
            )
        )
        viewModel.onSearchClick {
            assertTrue(false)
        }

        testingDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.refreshData.value != null)
        verify(mapDataRepository).getTrackerMapData(
            viewModel.selectedProgram.value,
            viewModel.queryData
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
        verify(presenter).onEnrollClick(any())
    }

    @Test
    fun `Should add relationship`() {
        viewModel.onAddRelationship("teiUd", "relationshipTypeUid", false)
        verify(presenter).addRelationship("teiUd", "relationshipTypeUid", false)
    }

    @Test
    fun `Should show sync icon`() {
        viewModel.onSyncIconClick("teiUid")
        verify(presenter).onSyncIconClick("teiUid")
    }

    @Test
    fun `Should downloadTei`() {
        viewModel.onDownloadTei("teiUid", null)
        verify(presenter).downloadTei("teiUid", null)
    }

    @Test
    fun `Should click on TEI`() {
        viewModel.onTeiClick("teiUid", null, true)
        verify(presenter).onTEIClick("teiUid", null, true)
    }

    @Test
    fun `Should close keyboard and filters`() {
        viewModel.onBackPressed(
            isPortrait = true,
            searchOrFilterIsOpen = true,
            keyBoardIsOpen = true,
            goBackCallback = { assertTrue(false) },
            closeSearchOrFilterCallback = { assertTrue(true) },
            closeKeyboardCallback = { assertTrue(true) }
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
            closeKeyboardCallback = { assertTrue(false) }
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
            closeKeyboardCallback = { assertTrue(true) }
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
            closeKeyboardCallback = { assertTrue(false) }
        )
    }

    @Test
    fun `Should display navigation bar`() {
        viewModel.setListScreen()
        assertTrue(viewModel.canDisplayBottomNavigationBar())
        viewModel.setMapScreen()
        assertTrue(viewModel.canDisplayBottomNavigationBar())
        viewModel.setSearchScreen(isLandscapeMode = false)
        assertTrue(!viewModel.canDisplayBottomNavigationBar())
    }

    private fun testingProgram(
        displayFrontPageList: Boolean = true,
        minAttributesToSearch: Int = 1,
        maxTeiCountToReturn: Int? = null
    ) = Program.builder()
        .uid("initialProgram")
        .displayFrontPageList(displayFrontPageList)
        .minAttributesRequiredToSearch(minAttributesToSearch)
        .trackedEntityType(TrackedEntityType.builder().uid("teTypeUid").build())
        .apply {
            maxTeiCountToReturn?.let {
                maxTeiCountToReturn(maxTeiCountToReturn)
            }
        }
        .build()
}
