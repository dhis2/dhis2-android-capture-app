package org.dhis2.mobile.login.authentication.ui.viewmodel

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.mobile.login.authentication.domain.model.TwoFAStatus
import org.dhis2.mobile.login.authentication.domain.usecase.GetTwoFAStatus
import org.dhis2.mobile.login.authentication.ui.mapper.TwoFAUiStateMapper
import org.dhis2.mobile.login.authentication.ui.state.TwoFAUiState
import org.junit.Before
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TwoFASettingsViewModelTest {

    private lateinit var viewModel: TwoFASettingsViewModel
    private val getTwoFAStatus: GetTwoFAStatus = mock()
    private val mapper: TwoFAUiStateMapper = mock()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun `init emits noConnection and checkTwoFAStatus emits Enabled`() = runTest {
        val noConnectionStatus = TwoFAStatus.NoConnection
        val noConnectionUiState = TwoFAUiState.NoConnection
        val enabledStatus = TwoFAStatus.Enabled()
        val disableUiState = TwoFAUiState.Disable()

        whenever(getTwoFAStatus()).thenReturn(flowOf(noConnectionStatus))
        whenever(mapper.mapToUiState(noConnectionStatus)).thenReturn(noConnectionUiState)

        viewModel = TwoFASettingsViewModel(getTwoFAStatus, mapper)

        viewModel.uiState.test {
            assert(awaitItem() is TwoFAUiState.Checking)

            assert(awaitItem() == noConnectionUiState)

            whenever(getTwoFAStatus()).thenReturn(flowOf(enabledStatus))
            whenever(mapper.mapToUiState(enabledStatus)).thenReturn(disableUiState)

            viewModel.checkTwoFAStatus()

            assert(awaitItem() is TwoFAUiState.Checking)

            assert(awaitItem() == disableUiState)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `retry calls checkTwoFAStatus`() = runTest {
        val noConnectionStatus = TwoFAStatus.NoConnection
        val noConnectionUiState = TwoFAUiState.NoConnection
        val disabledStatus = TwoFAStatus.Disabled()
        val enableUiState = TwoFAUiState.Enable()

        whenever(getTwoFAStatus()).thenReturn(flowOf(noConnectionStatus))
        whenever(mapper.mapToUiState(noConnectionStatus)).thenReturn(noConnectionUiState)

        viewModel = TwoFASettingsViewModel(getTwoFAStatus, mapper)

        viewModel.uiState.test {
            assert(awaitItem() is TwoFAUiState.Checking)

            assert(awaitItem() == noConnectionUiState)

            whenever(getTwoFAStatus()).thenReturn(flowOf(disabledStatus))
            whenever(mapper.mapToUiState(disabledStatus)).thenReturn(enableUiState)

            viewModel.retry()

            assert(awaitItem() is TwoFAUiState.Checking)

            assert(awaitItem() == enableUiState)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
