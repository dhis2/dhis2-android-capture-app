package org.dhis2.tracker.search.ui.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.mobile.commons.resources.StringResourceProvider
import org.dhis2.tracker.input.ui.state.TrackerInputUiState
import org.dhis2.tracker.search.ui.state.SearchParametersUiState
import org.junit.Assert
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

@ExperimentalCoroutinesApi
class SearchParametersViewModelTest {
    private val testingDispatcher = StandardTestDispatcher()
    private val resourceProvider = mock<StringResourceProvider>()
    private lateinit var viewModel: SearchParametersViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testingDispatcher)
        viewModel = SearchParametersViewModel(resourceProvider)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onValidateSearch should set validationResult to true when all items are valid`() =
        runTest(testingDispatcher) {
            // Given
            val validItems =
                listOf(
                    provideItem(
                        uid = "test1",
                        label = "Test 1",
                        value = "validvalue",
                        minCharactersToSearch = 3,
                    ),
                    provideItem(
                        uid = "test2",
                        label = "Test 2",
                        value = "another",
                        minCharactersToSearch = null,
                    ),
                    provideItem(
                        uid = "test3",
                        label = "Test 3",
                        value = null,
                        minCharactersToSearch = 3,
                    ),
                )

            viewModel.updateFromExternal(SearchParametersUiState(items = validItems))

            // When
            viewModel.onValidateSearch()
            advanceUntilIdle()

            // Then
            val validationResult = viewModel.validationResult.first()
            assertEquals(true, validationResult)
        }

    @Test
    fun `onValidateSearch should set validationResult to false when items have insufficient characters`() =
        runTest(testingDispatcher) {
            // Given
            val invalidItems =
                listOf(
                    provideItem(
                        uid = "test1",
                        value = "ab",
                        minCharactersToSearch = 3,
                    ),
                    provideItem(
                        uid = "test2",
                        label = "Test 2",
                        value = "abcde",
                        minCharactersToSearch = 5,
                    ),
                    provideItem(
                        uid = "test3",
                        label = "Test 3",
                        value = "a",
                        minCharactersToSearch = 2,
                    ),
                )
            whenever(
                resourceProvider.provideString(any(), any()),
            ).thenReturn("Minimum characters required")

            viewModel.updateFromExternal(SearchParametersUiState(items = invalidItems))

            // When
            viewModel.onValidateSearch()
            advanceUntilIdle()

            // Then
            val validationResult = viewModel.validationResult.first()
            assertNotEquals(true, validationResult)

            val uiState = viewModel.uiState.first() // Also check that error is set on the item
            assertEquals(3, uiState.items.size)
            Assert.assertTrue(uiState.items[0].error?.isNotEmpty() == true) // Error should be set
        }

    @Test
    fun `resetValidationResult should set validationResult to null`() =
        runTest(testingDispatcher) {
            // Given
            viewModel.updateFromExternal(SearchParametersUiState(items = listOf()))
            viewModel.onValidateSearch()
            advanceUntilIdle()

            // When
            viewModel.resetValidationResult()

            // Then
            val validationResult = viewModel.validationResult.first()
            assertNull(validationResult)
        }

    private fun provideItem(
        uid: String = "test1",
        label: String = "Test 1",
        value: String?,
        minCharactersToSearch: Int?,
    ): TrackerInputUiState =
        TrackerInputUiState(
            uid = uid,
            label = label,
            value = value,
            focused = false,
            valueType = mock(),
            optionSet = null,
            error = null,
            warning = null,
            description = null,
            mandatory = false,
            editable = true,
            legend = null,
            orientation = mock(),
            optionSetConfiguration = null,
            customIntentUid = null,
            displayName = null,
            orgUnitSelectorScope = null,
            searchOperator = null,
            minCharactersToSearch = minCharactersToSearch,
        )
}
