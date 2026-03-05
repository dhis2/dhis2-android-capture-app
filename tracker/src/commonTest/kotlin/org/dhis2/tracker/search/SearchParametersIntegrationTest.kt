package org.dhis2.tracker.search

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.commons.resources.StringResourceProvider
import org.dhis2.tracker.input.model.TrackerInputType
import org.dhis2.tracker.input.ui.mapper.toTrackerInputUiState
import org.dhis2.tracker.input.ui.state.supportingTextList
import org.dhis2.tracker.search.data.SearchParametersRepository
import org.dhis2.tracker.search.domain.FetchSearchParameters
import org.dhis2.tracker.search.model.FetchSearchParametersData
import org.dhis2.tracker.search.model.SearchOperator
import org.dhis2.tracker.search.model.SearchParameterModel
import org.dhis2.tracker.search.model.hasLabel
import org.dhis2.tracker.search.ui.state.SearchParametersUiState
import org.dhis2.tracker.search.ui.viewmodel.SearchParametersViewModel
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextState
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class SearchParametersIntegrationTest {
    private val testDispatcher = StandardTestDispatcher()
    private val dispatcher =
        Dispatcher(
            io = testDispatcher,
            main = testDispatcher,
            default = testDispatcher,
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

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `given min character limit is configured, when user enters exactly min characters, then search proceeds successfully`() =
        runTest(testDispatcher) {
            // Given
            val min = 2
            val teiTypeUid = "teiType123"
            val searchParameter =
                buildSearchParameter(
                    uid = "field1",
                    minCharactersToSearch = min,
                )
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
        runTest(testDispatcher) {
            // Given
            val min = 3
            val teiTypeUid = "teiType123"
            val errorMessage = "Minimum $min characters required"
            val searchParameter =
                buildSearchParameter(
                    uid = "field1",
                    minCharactersToSearch = min,
                )
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

    @Test
    fun `given a configured search operator, when user clicks field, then search operator message is shown`() =
        runTest(testDispatcher) {
            // Given
            val teiTypeUid = "teiType123"
            val searchParameters =
                listOf(
                    buildSearchParameter(
                        uid = "field1",
                        minCharactersToSearch = 0,
                        searchOperator = SearchOperator.EQ,
                    ),
                    buildSearchParameter(
                        uid = "field2",
                        minCharactersToSearch = 0,
                        searchOperator = SearchOperator.LIKE,
                    ),
                    buildSearchParameter(
                        uid = "field3",
                        minCharactersToSearch = 0,
                        searchOperator = SearchOperator.SW,
                    ),
                    buildSearchParameter(
                        uid = "field4",
                        minCharactersToSearch = 0,
                        searchOperator = SearchOperator.EW,
                    ),
                )

            whenever(repository.getSearchParametersByTrackedEntityType(teiTypeUid))
                .thenReturn(searchParameters)

            val fetchResult =
                fetchSearchParameters(FetchSearchParametersData(teiTypeUid = teiTypeUid))
            assertTrue(fetchResult.isSuccess)

            fetchResult
                .getOrNull()
                ?.forEach { searchParam ->
                    // Simulate the user clicking the field (focused = true)
                    val item = searchParam.toTrackerInputUiState().copy(focused = true)
                    val operator = searchParam.searchOperator

                    // Mimic what the Composable layer does: resolve a label only when the
                    // operator has one (stringResource is not available in unit tests).
                    val operatorLabel = if (operator?.hasLabel() == true) "operator label" else null
                    val supportingText = item.supportingTextList(searchOperatorLabel = operatorLabel)

                    if (operator?.hasLabel() == true) {
                        // EQ, SW, EW – a DEFAULT supporting text entry must be present
                        assertNotNull(supportingText)
                        assertTrue(supportingText.any { it.state == SupportingTextState.DEFAULT })
                    } else {
                        // LIKE – no label, no error/warning/description → null
                        assertNull(supportingText)
                    }
                }
        }

    @Test
    fun `given a configured search operator with error, when user clicks field, supporting text color in Red`() =
        runTest(testDispatcher) {
            // Given
            val teiTypeUid = "teiType123"
            val searchParameters =
                listOf(
                    buildSearchParameter(
                        uid = "field1",
                        minCharactersToSearch = 0,
                        searchOperator = SearchOperator.EQ,
                    ),
                )

            whenever(repository.getSearchParametersByTrackedEntityType(teiTypeUid))
                .thenReturn(searchParameters)

            val fetchResult =
                fetchSearchParameters(FetchSearchParametersData(teiTypeUid = teiTypeUid))
            assertTrue(fetchResult.isSuccess)

            val itemWithError =
                fetchResult
                    .getOrNull()!!
                    .map { searchParam ->
                        searchParam.toTrackerInputUiState().copy(error = "any Error", focused = true)
                    }
            viewModel.updateFromExternal(SearchParametersUiState(items = itemWithError))
            val uiState = viewModel.uiState.first()
            val supportingListData =
                uiState
                    .items
                    .first()
                    .supportingTextList(null)
                    ?.firstOrNull()
            assertNotNull(supportingListData)
            assertEquals(SupportingTextState.ERROR, supportingListData.state)
        }

    @Test
    fun `given a configured search operator with warning, when user clicks field, supporting text color in Orange`() =
        runTest(testDispatcher) {
            // Given
            val teiTypeUid = "teiType123"
            val searchParameters =
                listOf(
                    buildSearchParameter(
                        uid = "field1",
                        minCharactersToSearch = 0,
                        searchOperator = SearchOperator.EQ,
                    ),
                )

            whenever(repository.getSearchParametersByTrackedEntityType(teiTypeUid))
                .thenReturn(searchParameters)

            val fetchResult =
                fetchSearchParameters(FetchSearchParametersData(teiTypeUid = teiTypeUid))
            assertTrue(fetchResult.isSuccess)

            val itemWithError =
                fetchResult
                    .getOrNull()!!
                    .map { searchParam ->
                        searchParam.toTrackerInputUiState().copy(warning = "any Warning", focused = true)
                    }
            viewModel.updateFromExternal(SearchParametersUiState(items = itemWithError))
            val uiState = viewModel.uiState.first()
            val supportingListData =
                uiState.items
                    .first()
                    .supportingTextList(null)
                    ?.firstOrNull()
            assertNotNull(supportingListData)
            assertEquals(SupportingTextState.WARNING, supportingListData.state)
        }

    private fun buildSearchParameter(
        uid: String,
        minCharactersToSearch: Int,
        searchOperator: SearchOperator? = null,
    ): SearchParameterModel =
        SearchParameterModel(
            uid = uid,
            label = "First Name",
            inputType = TrackerInputType.TEXT,
            optionSet = null,
            customIntentUid = null,
            minCharactersToSearch = minCharactersToSearch,
            searchOperator = searchOperator,
            isUnique = false,
        )
}
