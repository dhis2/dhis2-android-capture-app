package org.dhis2.mobile.login.pin.ui.viewmodel

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.mobile.login.pin.domain.model.PinError
import org.dhis2.mobile.login.pin.domain.model.PinState
import org.dhis2.mobile.login.pin.domain.model.ValidatePinInput
import org.dhis2.mobile.login.pin.domain.usecase.ForgotPinUseCase
import org.dhis2.mobile.login.pin.domain.usecase.SavePinUseCase
import org.dhis2.mobile.login.pin.domain.usecase.ValidatePinUseCase
import org.dhis2.mobile.login.pin.ui.components.PinMode
import org.dhis2.mobile.login.pin.ui.provider.PinResourceProvider
import org.junit.Before
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PinViewModelTest {
    private lateinit var viewModel: PinViewModel
    private val savePinUseCase: SavePinUseCase = mock()
    private val validatePinUseCase: ValidatePinUseCase = mock()
    private val forgotPinUseCase: ForgotPinUseCase = mock()
    private val resourceProvider: PinResourceProvider = mock()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun `initial state is Idle`() =
        runTest {
            // Given
            viewModel = createViewModel()

            // Then
            assertEquals(PinState.Idle, viewModel.uiState.value)
        }

    @Test
    fun `onPinComplete with SET mode saves PIN successfully`() =
        runTest {
            // Given
            viewModel = createViewModel()
            val pin = "1234"
            whenever(savePinUseCase(pin)).thenReturn(Result.success(Unit))

            // When
            viewModel.onPinComplete(pin, PinMode.SET)
            advanceUntilIdle()

            // Then
            assertTrue(viewModel.uiState.value is PinState.Success)
        }

    @Test
    fun `onPinComplete with SET mode shows error when save fails`() =
        runTest {
            // Given
            viewModel = createViewModel()
            val pin = "1234"
            val error = Exception("Save failed")
            whenever(savePinUseCase(pin)).thenReturn(Result.failure(error))

            // When
            viewModel.onPinComplete(pin, PinMode.SET)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is PinState.Error)
            assertEquals("Save failed", state.message)
        }

    @Test
    fun `onPinComplete with SET mode uses resource provider when error message is null`() =
        runTest {
            // Given
            viewModel = createViewModel()
            val pin = "1234"
            val error = Exception()
            whenever(savePinUseCase(pin)).thenReturn(Result.failure(error))
            whenever(resourceProvider.getPinErrorSaveFailed()).thenReturn("Failed to save PIN")

            // When
            viewModel.onPinComplete(pin, PinMode.SET)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is PinState.Error)
            assertEquals("Failed to save PIN", state.message)
        }

    @Test
    fun `onPinComplete with ASK mode validates PIN successfully`() =
        runTest {
            // Given
            viewModel = createViewModel()
            val pin = "1234"
            whenever(validatePinUseCase(ValidatePinInput(pin, 0))).thenReturn(Result.success(Unit))

            // When
            viewModel.onPinComplete(pin, PinMode.ASK)
            advanceUntilIdle()

            // Then
            assertTrue(viewModel.uiState.value is PinState.Success)
        }

    @Test
    fun `onPinComplete with ASK mode shows error when PIN is incorrect`() =
        runTest {
            // Given
            viewModel = createViewModel()
            val pin = "0000"
            whenever(validatePinUseCase(ValidatePinInput(pin, 0))).thenReturn(Result.failure(PinError.Failed(attemptsLeft = 2)))
            whenever(resourceProvider.getPinErrorIncorrect()).thenReturn("Incorrect PIN")

            // When
            viewModel.onPinComplete(pin, PinMode.ASK)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is PinState.Error)
            assertEquals("Incorrect PIN", state.message)
            assertEquals(2, state.remainingAttempts)
        }

    @Test
    fun `onPinComplete with ASK mode shows TooManyAttempts after 3 failed attempts`() =
        runTest {
            // Given
            viewModel = createViewModel()
            val pin = "0000"
            whenever(resourceProvider.getPinErrorIncorrect()).thenReturn("Incorrect PIN")

            // First attempt
            whenever(validatePinUseCase(ValidatePinInput(pin, 0))).thenReturn(Result.failure(PinError.Failed(attemptsLeft = 2)))
            viewModel.onPinComplete(pin, PinMode.ASK)
            advanceUntilIdle()

            // Second attempt
            whenever(validatePinUseCase(ValidatePinInput(pin, 1))).thenReturn(Result.failure(PinError.Failed(attemptsLeft = 1)))
            viewModel.onPinComplete(pin, PinMode.ASK)
            advanceUntilIdle()

            // Third attempt
            whenever(validatePinUseCase(ValidatePinInput(pin, 2))).thenReturn(Result.failure(PinError.TooManyAttempts))
            viewModel.onPinComplete(pin, PinMode.ASK)
            advanceUntilIdle()

            // Then
            assertTrue(viewModel.uiState.value is PinState.TooManyAttempts)
        }

    @Test
    fun `onPinComplete with ASK mode shows error when no PIN is stored`() =
        runTest {
            // Given
            viewModel = createViewModel()
            val pin = "1234"
            whenever(validatePinUseCase(ValidatePinInput(pin, 0))).thenReturn(Result.failure(PinError.NoPinStored))
            whenever(resourceProvider.getPinErrorNoPinStored()).thenReturn("No PIN stored")

            // When
            viewModel.onPinComplete(pin, PinMode.ASK)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is PinState.Error)
            assertEquals("No PIN stored", state.message)
        }

    @Test
    fun `onForgotPin executes successfully`() =
        runTest {
            // Given
            viewModel = createViewModel()
            whenever(forgotPinUseCase(Unit)).thenReturn(Result.success(Unit))

            // When
            viewModel.onForgotPin()
            advanceUntilIdle()

            // Then
            assertTrue(viewModel.uiState.value is PinState.Dismissed)
        }

    @Test
    fun `onForgotPin shows error when it fails`() =
        runTest {
            // Given
            viewModel = createViewModel()
            val error = Exception("Logout failed")
            whenever(forgotPinUseCase(Unit)).thenReturn(Result.failure(error))

            // When
            viewModel.onForgotPin()
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is PinState.Error)
            assertEquals("Logout failed", state.message)
        }

    @Test
    fun `onForgotPin uses resource provider when error message is null`() =
        runTest {
            // Given
            viewModel = createViewModel()
            val error = Exception()
            whenever(forgotPinUseCase(Unit)).thenReturn(Result.failure(error))
            whenever(resourceProvider.getPinErrorResetFailed()).thenReturn("Failed to reset PIN")

            // When
            viewModel.onForgotPin()
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state is PinState.Error)
            assertEquals("Failed to reset PIN", state.message)
        }

    @Test
    fun `resetState changes state to Idle`() =
        runTest {
            // Given
            viewModel = createViewModel()
            val pin = "0000"
            whenever(validatePinUseCase(ValidatePinInput(pin, 0))).thenReturn(Result.failure(PinError.Failed(attemptsLeft = 2)))
            whenever(resourceProvider.getPinErrorIncorrect()).thenReturn("Incorrect PIN")
            viewModel.onPinComplete(pin, PinMode.ASK)
            advanceUntilIdle()

            // When
            viewModel.resetState()

            // Then
            assertEquals(PinState.Idle, viewModel.uiState.value)
        }

    @Test
    fun `resetAttempts resets the attempt counter`() =
        runTest {
            // Given
            viewModel = createViewModel()
            val pin = "0000"
            whenever(resourceProvider.getPinErrorIncorrect()).thenReturn("Incorrect PIN")

            // Make a failed attempt
            whenever(validatePinUseCase(ValidatePinInput(pin, 0))).thenReturn(Result.failure(PinError.Failed(attemptsLeft = 2)))
            viewModel.onPinComplete(pin, PinMode.ASK)
            advanceUntilIdle()

            // When
            viewModel.resetAttempts()

            // Try again - should use attempts = 0
            whenever(validatePinUseCase(ValidatePinInput(pin, 0))).thenReturn(Result.failure(PinError.Failed(attemptsLeft = 2)))
            viewModel.onPinComplete(pin, PinMode.ASK)
            advanceUntilIdle()

            // Then - should still have 2 attempts left
            val state = viewModel.uiState.value
            assertTrue(state is PinState.Error)
            assertEquals(2, state.remainingAttempts)
        }

    @Test
    fun `state changes are emitted correctly`() =
        runTest {
            // Given
            viewModel = createViewModel()
            val pin = "1234"
            whenever(savePinUseCase(pin)).thenReturn(Result.success(Unit))

            // When/Then
            viewModel.uiState.test {
                // Initial state
                assertEquals(PinState.Idle, awaitItem())

                // Trigger save
                viewModel.onPinComplete(pin, PinMode.SET)

                // Loading state
                assertEquals(PinState.Loading, awaitItem())

                advanceUntilIdle()

                // Success state
                assertEquals(PinState.Success, awaitItem())
            }
        }

    private fun createViewModel() =
        PinViewModel(
            savePinUseCase = savePinUseCase,
            validatePinUseCase = validatePinUseCase,
            forgotPinUseCase = forgotPinUseCase,
            resourceProvider = resourceProvider,
        )
}
