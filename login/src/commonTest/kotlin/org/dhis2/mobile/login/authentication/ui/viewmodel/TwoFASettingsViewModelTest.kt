package org.dhis2.mobile.login.authentication.ui.viewmodel

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.mobile.login.authentication.domain.model.TwoFAStatus
import org.dhis2.mobile.login.authentication.domain.usecase.DisableTwoFA
import org.dhis2.mobile.login.authentication.domain.usecase.EnableTwoFA
import org.dhis2.mobile.login.authentication.domain.usecase.GetTwoFASecretCode
import org.dhis2.mobile.login.authentication.domain.usecase.GetTwoFAStatus
import org.dhis2.mobile.login.authentication.ui.mapper.TwoFAUiStateMapper
import org.dhis2.mobile.login.authentication.ui.state.TwoFAUiState
import org.junit.Before
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doReturnConsecutively
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class TwoFASettingsViewModelTest {
    private lateinit var viewModel: TwoFASettingsViewModel
    private val getTwoFAStatus: GetTwoFAStatus = mock()
    private val mapper: TwoFAUiStateMapper = mock()
    private val getTwoFASecretCode: GetTwoFASecretCode = mock()
    private val enableTwoFA: EnableTwoFA = mock()
    private val disableTwoFA: DisableTwoFA = mock()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun `TwoFAStatus is enabled`() =
        runTest {
            val enabledStatus = TwoFAStatus.Enabled()
            val disableUiState = TwoFAUiState.Disable()

            whenever(getTwoFAStatus()).thenReturn(enabledStatus)
            whenever(mapper.mapToUiState(enabledStatus)).thenReturn(disableUiState)

            viewModel =
                TwoFASettingsViewModel(
                    getTwoFAStatus,
                    getTwoFASecretCode,
                    enableTwoFA,
                    disableTwoFA,
                    mapper,
                )

            viewModel.uiState.test {
                assert(awaitItem() is TwoFAUiState.Checking)

                assert(awaitItem() == disableUiState)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `retry calls checkTwoFAStatus`() =
        runTest {
            val noConnectionUiState = TwoFAStatus.NoConnection
            val disabledStatus = TwoFAStatus.Disabled()
            val enableUiState = TwoFAUiState.Enable()

            whenever(getTwoFAStatus()) doReturnConsecutively
                listOf(
                    noConnectionUiState,
                    disabledStatus,
                )
            whenever(mapper.mapToUiState(noConnectionUiState)) doReturn TwoFAUiState.NoConnection

            whenever(mapper.mapToUiState(disabledStatus)) doReturn enableUiState

            viewModel =
                TwoFASettingsViewModel(
                    getTwoFAStatus = getTwoFAStatus,
                    getTwoFASecretCode = getTwoFASecretCode,
                    enableTwoFA = enableTwoFA,
                    disableTwoFA = disableTwoFA,
                    mapper = mapper,
                )

            viewModel.uiState.test {
                assertEquals(TwoFAUiState.Checking, awaitItem())

                assertEquals(TwoFAUiState.NoConnection, awaitItem())

                viewModel.retry()

                assertEquals(TwoFAUiState.Checking, awaitItem())

                assertEquals(enableUiState, awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
        }
}
