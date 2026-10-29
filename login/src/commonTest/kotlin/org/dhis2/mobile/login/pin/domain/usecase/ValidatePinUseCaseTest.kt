package org.dhis2.mobile.login.pin.domain.usecase

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.login.pin.data.SessionRepository
import org.dhis2.mobile.login.pin.domain.model.PinError
import org.dhis2.mobile.login.pin.domain.model.PinVerification
import org.dhis2.mobile.login.pin.domain.model.ValidatePinInput
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ValidatePinUseCaseTest {
    private lateinit var useCase: ValidatePinUseCase
    private val repository: SessionRepository = mock()

    @BeforeTest
    fun setUp() {
        useCase = ValidatePinUseCase(repository)
    }

    @Test
    fun `invoke returns Success when PIN matches stored PIN`() =
        runTest {
            // Given
            val pin = "1234"
            whenever(repository.verifyPin(pin)).thenReturn(PinVerification.Match)

            // When
            val result = useCase(ValidatePinInput(pin, 0))

            // Then
            assertTrue(result.isSuccess)
        }

    @Test
    fun `invoke returns Failed with attempts left when PIN is incorrect`() =
        runTest {
            // Given
            val pin = "0000"
            whenever(repository.verifyPin(pin)).thenReturn(PinVerification.Mismatch)

            // When - First attempt
            val result = useCase(ValidatePinInput(pin, 0))

            // Then
            assertTrue(result.isFailure)
            val err = result.exceptionOrNull()
            assertTrue(err is PinError.Failed)
            assertEquals(2, err.attemptsLeft)
        }

    @Test
    fun `invoke returns TooManyAttempts when max attempts exceeded`() =
        runTest {
            // Given
            val pin = "0000"
            whenever(repository.verifyPin(pin)).thenReturn(PinVerification.Mismatch)

            // When - Third attempt (last one)
            val result = useCase(ValidatePinInput(pin, 2))

            // Then
            assertTrue(result.isFailure)
            val err = result.exceptionOrNull()
            assertTrue(err is PinError.TooManyAttempts)
        }

    @Test
    fun `invoke returns NoPinStored when no PIN is configured`() =
        runTest {
            // Given
            whenever(repository.verifyPin("1234")).thenReturn(PinVerification.NoPinStored)

            // When
            val result = useCase(ValidatePinInput("1234", 0))

            // Then
            assertTrue(result.isFailure)
            val err = result.exceptionOrNull()
            assertTrue(err is PinError.NoPinStored)
        }

    @Test
    fun `invoke with second attempt returns correct attempts left`() =
        runTest {
            // Given
            val pin = "0000"
            whenever(repository.verifyPin(pin)).thenReturn(PinVerification.Mismatch)

            // When - Second attempt
            val result = useCase(ValidatePinInput(pin, 1))

            // Then
            assertTrue(result.isFailure)
            val err = result.exceptionOrNull()
            assertTrue(err is PinError.Failed)
            assertEquals(1, err.attemptsLeft)
        }

    @Test
    fun `invoke returns the failure when the PIN cannot be verified`() =
        runTest {
            // Given
            val error = IllegalStateException("Keystore unavailable")
            whenever(repository.verifyPin("1234")).thenThrow(error)

            // When
            val result = useCase(ValidatePinInput("1234", 0))

            // Then
            assertSame(error, result.exceptionOrNull())
        }
}
