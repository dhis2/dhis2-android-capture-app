package org.dhis2.mobile.login.pin.ui.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.mobile.login.pin.domain.model.PinError
import org.dhis2.mobile.login.pin.domain.model.PinMode
import org.dhis2.mobile.login.pin.domain.model.ValidatePinInput
import org.dhis2.mobile.login.pin.domain.usecase.ForgotPinUseCase
import org.dhis2.mobile.login.pin.domain.usecase.SavePinUseCase
import org.dhis2.mobile.login.pin.domain.usecase.ValidatePinUseCase
import org.dhis2.mobile.login.pin.ui.provider.PinResourceProvider
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PinViewModelTest {
    private lateinit var viewModel: PinViewModel
    private val savePinUseCase: SavePinUseCase = mock()
    private val validatePinUseCase: ValidatePinUseCase = mock()
    private val forgotPinUseCase: ForgotPinUseCase = mock()
    private val resourceProvider: PinResourceProvider = mock()
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        runBlocking {
            whenever(resourceProvider.getPinTitle(any())).thenReturn("")
            whenever(resourceProvider.getPinSubtitle(any())).thenReturn("")
            whenever(resourceProvider.getPrimaryButtonText(any())).thenReturn("")
            whenever(resourceProvider.getSecondaryButtonText(any())).thenReturn(null)
        }
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has no error and no active flags`() =
        runTest {
            viewModel = createViewModel()

            val state = viewModel.uiState.value
            assertNull(state.errorMessage)
            assertFalse(state.isLoading)
            assertFalse(state.isSuccess)
            assertFalse(state.isDismissed)
            assertFalse(state.isTooManyAttempts)
        }

    @Test
    fun `onPinComplete with SET mode saves PIN successfully`() =
        runTest {
            viewModel = createViewModel(PinMode.SET)
            val pin = "1234"
            whenever(savePinUseCase(pin)).thenReturn(Result.success(Unit))

            viewModel.onPinChanged(pin)
            viewModel.onPinComplete()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.isSuccess)
        }

    @Test
    fun `onPinComplete with SET mode shows error when save fails`() =
        runTest {
            viewModel = createViewModel(PinMode.SET)
            val pin = "1234"
            whenever(savePinUseCase(pin)).thenReturn(Result.failure(Exception("Save failed")))

            viewModel.onPinChanged(pin)
            viewModel.onPinComplete()
            advanceUntilIdle()

            assertEquals("Save failed", viewModel.uiState.value.errorMessage)
        }

    @Test
    fun `onPinComplete with SET mode uses resource provider when error message is null`() =
        runTest {
            viewModel = createViewModel(PinMode.SET)
            val pin = "1234"
            whenever(savePinUseCase(pin)).thenReturn(Result.failure(Exception()))
            whenever(resourceProvider.getPinErrorSaveFailed()).thenReturn("Failed to save PIN")

            viewModel.onPinChanged(pin)
            viewModel.onPinComplete()
            advanceUntilIdle()

            assertEquals("Failed to save PIN", viewModel.uiState.value.errorMessage)
        }

    @Test
    fun `onPinComplete with ASK mode validates PIN successfully`() =
        runTest {
            viewModel = createViewModel(PinMode.ASK)
            val pin = "1234"
            whenever(validatePinUseCase(ValidatePinInput(pin, 0))).thenReturn(Result.success(Unit))

            viewModel.onPinChanged(pin)
            viewModel.onPinComplete()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.isSuccess)
        }

    @Test
    fun `onPinComplete with ASK mode shows error when PIN is incorrect`() =
        runTest {
            viewModel = createViewModel(PinMode.ASK)
            val pin = "0000"
            whenever(validatePinUseCase(ValidatePinInput(pin, 0))).thenReturn(
                Result.failure(PinError.Failed(attemptsLeft = 2)),
            )
            whenever(resourceProvider.getPinErrorIncorrect()).thenReturn("Incorrect PIN")
            whenever(resourceProvider.getPinErrorWithAttempts("Incorrect PIN", 2))
                .thenReturn("Incorrect PIN. 2 attempts remaining")

            viewModel.onPinChanged(pin)
            viewModel.onPinComplete()
            advanceUntilIdle()

            assertEquals("Incorrect PIN. 2 attempts remaining", viewModel.uiState.value.errorMessage)
        }

    @Test
    fun `onPinComplete with ASK mode shows TooManyAttempts after 3 failed attempts`() =
        runTest {
            viewModel = createViewModel(PinMode.ASK)
            val pin = "0000"
            whenever(resourceProvider.getPinErrorIncorrect()).thenReturn("Incorrect PIN")

            whenever(validatePinUseCase(ValidatePinInput(pin, 0))).thenReturn(
                Result.failure(PinError.Failed(attemptsLeft = 2)),
            )
            viewModel.onPinChanged(pin)
            viewModel.onPinComplete()
            advanceUntilIdle()

            whenever(validatePinUseCase(ValidatePinInput(pin, 1))).thenReturn(
                Result.failure(PinError.Failed(attemptsLeft = 1)),
            )
            viewModel.onPinChanged(pin)
            viewModel.onPinComplete()
            advanceUntilIdle()

            whenever(validatePinUseCase(ValidatePinInput(pin, 2))).thenReturn(
                Result.failure(PinError.TooManyAttempts),
            )
            viewModel.onPinChanged(pin)
            viewModel.onPinComplete()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.isTooManyAttempts)
        }

    @Test
    fun `onPinComplete with ASK mode shows error when no PIN is stored`() =
        runTest {
            viewModel = createViewModel(PinMode.ASK)
            val pin = "1234"
            whenever(validatePinUseCase(ValidatePinInput(pin, 0))).thenReturn(
                Result.failure(PinError.NoPinStored),
            )
            whenever(resourceProvider.getPinErrorNoPinStored()).thenReturn("No PIN stored")

            viewModel.onPinChanged(pin)
            viewModel.onPinComplete()
            advanceUntilIdle()

            assertEquals("No PIN stored", viewModel.uiState.value.errorMessage)
        }

    @Test
    fun `onForgotPin executes successfully`() =
        runTest {
            viewModel = createViewModel()
            whenever(forgotPinUseCase(Unit)).thenReturn(Result.success(Unit))

            viewModel.onForgotPin()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.isDismissed)
        }

    @Test
    fun `onForgotPin shows error when it fails`() =
        runTest {
            viewModel = createViewModel()
            whenever(forgotPinUseCase(Unit)).thenReturn(Result.failure(Exception("Logout failed")))

            viewModel.onForgotPin()
            advanceUntilIdle()

            assertEquals("Logout failed", viewModel.uiState.value.errorMessage)
        }

    @Test
    fun `onForgotPin uses resource provider when error message is null`() =
        runTest {
            viewModel = createViewModel()
            whenever(forgotPinUseCase(Unit)).thenReturn(Result.failure(Exception()))
            whenever(resourceProvider.getPinErrorResetFailed()).thenReturn("Failed to reset PIN")

            viewModel.onForgotPin()
            advanceUntilIdle()

            assertEquals("Failed to reset PIN", viewModel.uiState.value.errorMessage)
        }

    @Test
    fun `resetState clears error and active flags`() =
        runTest {
            viewModel = createViewModel(PinMode.ASK)
            val pin = "0000"
            whenever(validatePinUseCase(ValidatePinInput(pin, 0))).thenReturn(
                Result.failure(PinError.Failed(attemptsLeft = 2)),
            )
            whenever(resourceProvider.getPinErrorIncorrect()).thenReturn("Incorrect PIN")
            viewModel.onPinChanged(pin)
            viewModel.onPinComplete()
            advanceUntilIdle()

            viewModel.resetState()

            val state = viewModel.uiState.value
            assertNull(state.errorMessage)
            assertFalse(state.isLoading)
            assertFalse(state.isSuccess)
            assertFalse(state.isDismissed)
            assertFalse(state.isTooManyAttempts)
        }

    @Test
    fun `resetAttempts resets the attempt counter`() =
        runTest {
            viewModel = createViewModel(PinMode.ASK)
            val pin = "0000"
            whenever(resourceProvider.getPinErrorIncorrect()).thenReturn("Incorrect PIN")
            whenever(resourceProvider.getPinErrorWithAttempts("Incorrect PIN", 2))
                .thenReturn("Incorrect PIN. 2 attempts remaining")

            // First failed attempt (attempt index 0)
            whenever(validatePinUseCase(ValidatePinInput(pin, 0))).thenReturn(
                Result.failure(PinError.Failed(attemptsLeft = 2)),
            )
            viewModel.onPinChanged(pin)
            viewModel.onPinComplete()
            advanceUntilIdle()

            // Reset attempt counter
            viewModel.resetAttempts()

            // Second attempt should restart from index 0
            whenever(validatePinUseCase(ValidatePinInput(pin, 0))).thenReturn(
                Result.failure(PinError.Failed(attemptsLeft = 2)),
            )
            viewModel.onPinChanged(pin)
            viewModel.onPinComplete()
            advanceUntilIdle()

            assertEquals("Incorrect PIN. 2 attempts remaining", viewModel.uiState.value.errorMessage)
        }

    @Test
    fun `state transitions through loading to success`() =
        runTest {
            viewModel = createViewModel(PinMode.SET)
            val pin = "1234"
            whenever(savePinUseCase(pin)).thenReturn(Result.success(Unit))

            viewModel.onPinChanged(pin)
            viewModel.onPinComplete()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertTrue(state.isSuccess)
        }

    private fun createViewModel(mode: PinMode = PinMode.ASK) =
        PinViewModel(
            mode = mode,
            savePinUseCase = savePinUseCase,
            validatePinUseCase = validatePinUseCase,
            forgotPinUseCase = forgotPinUseCase,
            resourceProvider = resourceProvider,
        )
}
