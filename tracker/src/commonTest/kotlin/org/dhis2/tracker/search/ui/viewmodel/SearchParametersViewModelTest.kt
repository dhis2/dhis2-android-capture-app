package org.dhis2.tracker.relationships.org.dhis2.tracker.search.ui.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.mobile.commons.resources.StringResourceProvider
import org.dhis2.tracker.input.ui.state.TrackerInputUiState
import org.dhis2.tracker.search.ui.state.SearchParametersUiState
import org.dhis2.tracker.search.ui.viewmodel.SearchParametersViewModel
import org.junit.Assert
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

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

    @Test
    fun `onValidateSearch should set validationResult to true when all items are valid`() =
        runTest {
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
                )

            viewModel.updateFromExternal(SearchParametersUiState(items = validItems))

            // When
            viewModel.onValidateSearch()
            advanceUntilIdle()

            // Then
            val validationResult = viewModel.validationResult.first()
            assertEquals(validationResult, true)
        }

    @Test
    fun `onValidateSearch should set validationResult to false when items have insufficient characters`() =
        runTest {
            // Given
            val invalidItems =
                listOf(
                    provideItem(
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
            assertNotEquals(validationResult, true)

            // Also check that error is set on the item
            val uiState = viewModel.uiState.first()
            assertEquals(3, uiState.items.size)
            Assert.assertTrue(uiState.items[0].error?.isNotEmpty() == true) // Error should be set
        }

    @Test
    fun `onValidateMinCharacters should return false and set errors when items have insufficient characters`() =
        runTest {
            // Given
            val invalidItems =
                listOf(
                    provideItem(
                        uid = "test1",
                        label = "Test 1",
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
            val result = viewModel.onValidateMinCharacters()

            // Then
            assertFalse(result)

            // Check that errors are set on invalid items
            val uiState = viewModel.uiState.first()
            assertEquals(3, uiState.items.size)
            assertEquals(uiState.items[0].error?.isNotEmpty(), true) // test1 should have error
            assertNull(uiState.items[1].error) // test2 should have no error
            assertEquals(uiState.items[2].error?.isNotEmpty(), true) // test3 should have error
        }

    @Test
    fun `onValidateMinCharacters should ignore empty values`() =
        runTest {
            // Given
            val itemsWithEmptyValues =
                listOf(
                    provideItem(
                        uid = "test1",
                        label = "Test 1",
                        value = "",
                        minCharactersToSearch = 3,
                    ),
                    provideItem(
                        uid = "test2",
                        label = "Test 2",
                        value = null,
                        minCharactersToSearch = 3,
                    ),
                    provideItem(
                        uid = "test3",
                        label = "Test 3",
                        value = "abc",
                        minCharactersToSearch = 3,
                    ),
                )

            viewModel.updateFromExternal(SearchParametersUiState(items = itemsWithEmptyValues))

            // When
            val result = viewModel.onValidateMinCharacters()

            // Then
            assertTrue(result)

            // Check that all errors are cleared
            val uiState = viewModel.uiState.first()
            assertTrue(uiState.items.all { it.error == null })
        }

    @Test
    fun `resetValidationResult should set validationResult to null`() =
        runTest {
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

    @Test
    fun `onValidateMinCharacters should clear existing errors when validation passes`() =
        runTest {
            // Given
            val itemsWithExistingErrors =
                listOf(
                    provideItem(
                        uid = "test1",
                        label = "Test 1",
                        value = "abc",
                        minCharactersToSearch = 3,
                    ),
                )

            viewModel.updateFromExternal(SearchParametersUiState(items = itemsWithExistingErrors))

            // When
            val result = viewModel.onValidateMinCharacters()

            // Then
            assertTrue(result)

            // Check that existing error is cleared
            val uiState = viewModel.uiState.first()
            assertNull(uiState.items[0].error)
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
