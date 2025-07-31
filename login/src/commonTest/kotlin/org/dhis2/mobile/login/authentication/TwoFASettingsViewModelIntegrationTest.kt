package org.dhis2.mobile.login.authentication

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.mobile.login.authentication.domain.model.TwoFAStatus
import org.dhis2.mobile.login.authentication.domain.repository.TwoFARepository
import org.dhis2.mobile.login.authentication.domain.usecase.GetTwoFAStatus
import org.dhis2.mobile.login.authentication.ui.mapper.TwoFAUiStateMapper
import org.dhis2.mobile.login.authentication.ui.state.TwoFAUiState
import org.dhis2.mobile.login.authentication.ui.viewmodel.TwoFASettingsViewModel
import org.junit.jupiter.api.DisplayName
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TwoFASettingsViewModelIntegrationTest {

    // Test dependencies
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepository: TwoFARepository
    private lateinit var useCase: GetTwoFAStatus
    private lateinit var mapper: TwoFAUiStateMapper
    private lateinit var viewModel: TwoFASettingsViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mock repository
        mockRepository = mock()

        // Real use case - this is what we're integration testing
        useCase = GetTwoFAStatus(mockRepository)

        // Real mapper - testing the actual mapping logic
        mapper = TwoFAUiStateMapper()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @DisplayName("Should emit Loading then Enabled states when 2FA is enabled")
    fun testEnabledState() = runTest {
        // Given
        whenever(mockRepository.getTwoFAStatus()).thenReturn(
            flowOf(TwoFAStatus.Enabled()),
        )

        // When
        viewModel = TwoFASettingsViewModel(useCase, mapper)

        // Then
        viewModel.uiState.test {
            // First emission should be Checking (initial state)
            assertEquals(TwoFAUiState.Checking, awaitItem())

            // Second emission should be Enabled
            val enabledState = awaitItem()
            assertTrue(enabledState is TwoFAUiState.Enabled)
            assertEquals(null, enabledState.errorMessage)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    @DisplayName("Should emit Loading then Disabled states when 2FA is disabled")
    fun testDisabledState() = runTest {
        // Given
        val errorMessage = "Disabled for maintenance"
        whenever(mockRepository.getTwoFAStatus()).thenReturn(
            flowOf(TwoFAStatus.Disabled(errorMessage)),
        )

        // When
        viewModel = TwoFASettingsViewModel(useCase, mapper)

        // Then
        viewModel.uiState.test {
            // First emission should be Checking (initial state)
            assertEquals(TwoFAUiState.Checking, awaitItem())

            // Second emission should be Disabled with error message
            val disabledState = awaitItem()
            assertTrue(disabledState is TwoFAUiState.Disabled)
            assertEquals(errorMessage, disabledState.errorMessage)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    @DisplayName("Should emit Loading then NoConnection state when there's no connection")
    fun testNoConnectionState() = runTest {
        // Given
        whenever(mockRepository.getTwoFAStatus()).thenReturn(
            flowOf(TwoFAStatus.NoConnection),
        )

        // When
        viewModel = TwoFASettingsViewModel(useCase, mapper)

        // Then
        viewModel.uiState.test {
            // First emission should be Checking (initial state)
            assertEquals(TwoFAUiState.Checking, awaitItem())

            // Second emission should be NoConnection
            val noConnectionState = awaitItem()
            assertEquals(TwoFAUiState.NoConnection, noConnectionState)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
