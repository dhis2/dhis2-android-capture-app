package org.dhis2.usescases.settings.ui.viewmodels

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.mobile.commons.domain.invoke
import org.dhis2.usescases.settings.domain.GetSharedData
import org.dhis2.usescases.settings.domain.GetSyncErrors
import org.dhis2.usescases.settings.models.ErrorViewModel
import org.dhis2.usescases.settings.models.SyncErrorLogUiState
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SyncErrorLogViewModelTest {
    private val testingDispatcher = UnconfinedTestDispatcher()

    private val getSyncErrors: GetSyncErrors = mock()
    private val getSharedData: GetSharedData = mock()
    private val dispatcherProvider: DispatcherProvider =
        mock {
            on { io() } doReturn testingDispatcher
        }

    private lateinit var viewModel: SyncErrorLogViewModel

    private val errorModel =
        ErrorViewModel(
            creationDate = null,
            creationDateLabel = "label-1",
            errorCode = "1",
            errorDescription = "description-1",
            errorComponent = "component-1",
        )

    @Before
    fun setUp() {
        Dispatchers.setMain(testingDispatcher)
        viewModel =
            SyncErrorLogViewModel(
                getSyncErrors = getSyncErrors,
                getSharedData = getSharedData,
                dispatchers = dispatcherProvider,
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // With the io/main dispatchers backed by the same UnconfinedTestDispatcher, loadErrorLog()
    // completes synchronously before the flow is collected, so the transient Loading value is
    // never observed by a subscriber — only the resulting Log state is.
    @Test
    fun `should load error log on start`() =
        runTest {
            whenever(getSyncErrors.invoke()) doReturn Result.success(listOf(errorModel))

            viewModel.errorLogUiState.test {
                assertEquals(SyncErrorLogUiState.Log(listOf(errorModel)), awaitItem())
            }
        }

    @Test
    fun `should default to empty list when loading errors fails`() =
        runTest {
            whenever(getSyncErrors.invoke()) doReturn Result.failure(RuntimeException("error"))

            viewModel.errorLogUiState.test {
                assertEquals(SyncErrorLogUiState.Log(emptyList()), awaitItem())
            }
        }

    @Test
    fun `should switch to selection mode`() =
        runTest {
            whenever(getSyncErrors.invoke()) doReturn Result.success(listOf(errorModel))

            viewModel.errorLogUiState.test {
                awaitItem() // Log

                viewModel.initSelectionMode()

                assertEquals(SyncErrorLogUiState.Selection(listOf(errorModel)), awaitItem())
            }
        }

    @Test
    fun `should exit selection mode and clear selection`() =
        runTest {
            val selectedError = errorModel.copy(isSelected = true)
            whenever(getSyncErrors.invoke()) doReturn Result.success(listOf(selectedError))

            viewModel.errorLogUiState.test {
                awaitItem() // Log

                viewModel.initSelectionMode()
                awaitItem() // Selection

                viewModel.exitSelectionMode()

                val item = awaitItem() as SyncErrorLogUiState.Log
                assertEquals(false, item.errorList.first().isSelected)
            }
        }

    @Test
    fun `should toggle selection for given index`() =
        runTest {
            val secondError = errorModel.copy(errorCode = "2")
            whenever(getSyncErrors.invoke()) doReturn Result.success(listOf(errorModel, secondError))

            viewModel.errorLogUiState.test {
                awaitItem() // Log

                viewModel.initSelectionMode()
                awaitItem() // Selection

                viewModel.setSelected(1)

                val item = awaitItem() as SyncErrorLogUiState.Selection
                assertEquals(false, item.errorList[0].isSelected)
                assertEquals(true, item.errorList[1].isSelected)
            }
        }

    @Test
    fun `should emit share event with shared data when sharing log`() =
        runTest {
            val selectedError = errorModel.copy(isSelected = true)
            whenever(getSyncErrors.invoke()) doReturn Result.success(listOf(selectedError))
            whenever(getSharedData(listOf(selectedError))) doReturn Result.success("shared-json")

            viewModel.errorLogUiState.test {
                awaitItem() // Log

                viewModel.initSelectionMode()
                awaitItem() // Selection

                cancelAndIgnoreRemainingEvents()
            }

            viewModel.shareEvent.test {
                viewModel.onShareLog()
                assertEquals("shared-json", awaitItem())
            }
        }
}
