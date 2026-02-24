package org.dhis2.tracker.search

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.commons.resources.StringResourceProvider
import org.dhis2.tracker.input.model.TrackerInputType
import org.dhis2.tracker.input.ui.mapper.toTrackerInputUiState
import org.dhis2.tracker.search.data.SearchParametersRepository
import org.dhis2.tracker.search.domain.FetchSearchParameters
import org.dhis2.tracker.search.model.FetchSearchParametersData
import org.dhis2.tracker.search.model.SearchOperator
import org.dhis2.tracker.search.model.SearchParameterModel
import org.dhis2.tracker.search.ui.state.SearchParametersUiState
import org.dhis2.tracker.search.ui.viewmodel.SearchParametersViewModel
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class SearchParametersIntegrationTest {
    private val testDispatcher = StandardTestDispatcher()
    private val dispatcher =
        Dispatcher(
            io = UnconfinedTestDispatcher(),
            main = Dispatchers.Unconfined,
            default = Dispatchers.Unconfined,
        )

    private val repository: SearchParametersRepository = mock()
    private val resourceProvider: StringResourceProvider = mock()

    private lateinit var fetchSearchParameters: FetchSearchParameters
    private lateinit var viewModel: SearchParametersViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fetchSearchParameters = FetchSearchParameters(dispatcher, repository)
        viewModel = SearchParametersViewModel(resourceProvider)
    }

    @Test
    fun `given min character limit is configured, when user enters exactly min characters, then search proceeds successfully`() =
        runTest {
            // Given
            val min = 2
            val teiTypeUid = "teiType123"
            val searchParameter = buildSearchParameter(minCharactersToSearch = min)
            whenever(repository.getSearchParametersByTrackedEntityType(teiTypeUid))
                .thenReturn(listOf(searchParameter))

            val fetchResult =
                fetchSearchParameters(FetchSearchParametersData(teiTypeUid = teiTypeUid))
            assertTrue(fetchResult.isSuccess)

            // When - user enters exactly min characters
            val itemWithExactMinChars =
                fetchResult
                    .getOrNull()!!
                    .map { it.toTrackerInputUiState().copy(value = "a".repeat(min)) }
            viewModel.updateFromExternal(SearchParametersUiState(items = itemWithExactMinChars))
            viewModel.onValidateSearch()
            advanceUntilIdle()

            // Then - search proceeds successfully
            val validationResult = viewModel.validationResult.first()
            assertEquals(true, validationResult)

            // And no error message is shown
            val uiState = viewModel.uiState.first()
            assertTrue(uiState.items.all { it.error == null })
        }

    @Test
    fun `given min character limit, when user enters fewer than min characters, then search fails and error is shown`() =
        runTest {
            // Given
            val min = 3
            val teiTypeUid = "teiType123"
            val errorMessage = "Minimum $min characters required"
            val searchParameter = buildSearchParameter(minCharactersToSearch = min)
            whenever(repository.getSearchParametersByTrackedEntityType(teiTypeUid))
                .thenReturn(listOf(searchParameter))
            whenever(resourceProvider.provideString(any(), any())).thenReturn(errorMessage)

            val fetchResult =
                fetchSearchParameters(FetchSearchParametersData(teiTypeUid = teiTypeUid))
            assertTrue(fetchResult.isSuccess)

            // When - user enters fewer than min characters
            val itemWithInsufficientChars =
                fetchResult
                    .getOrNull()!!
                    .map { it.toTrackerInputUiState().copy(value = "a".repeat(min - 1)) }
            viewModel.updateFromExternal(SearchParametersUiState(items = itemWithInsufficientChars))
            viewModel.onValidateSearch()
            advanceUntilIdle()

            // Then - search proceeds unsuccessfully
            val validationResult = viewModel.validationResult.first()
            assertEquals(false, validationResult)

            // And an error message is shown
            val uiState = viewModel.uiState.first()
            assertNull(uiState.items.firstOrNull { it.error == null })
            assertEquals(errorMessage, uiState.items.first().error)
        }

    private fun buildSearchParameter(minCharactersToSearch: Int): SearchParameterModel =
        SearchParameterModel(
            uid = "field1",
            label = "First Name",
            inputType = TrackerInputType.TEXT,
            optionSet = null,
            customIntentUid = null,
            minCharactersToSearch = minCharactersToSearch,
            searchOperator = SearchOperator.LIKE,
            isUnique = false,
        )
}
