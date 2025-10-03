package org.dhis2.mobile.login.pin.domain.usecase

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.login.pin.data.SessionRepository
import org.dhis2.mobile.login.pin.domain.model.PinResult
import org.junit.Before
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ValidatePinUseCaseTest {
    private lateinit var useCase: ValidatePinUseCase
    private val repository: SessionRepository = mock()

    @Before
    fun setUp() {
        useCase = ValidatePinUseCase(repository)
    }

    @Test
    fun `invoke returns Success when PIN matches stored PIN`() =
        runTest {
            // Given
            val pin = "1234"
            val storedPin = "1234"
            whenever(repository.getStoredPin()).thenReturn(storedPin)
            whenever(repository.setSessionLocked(false)).then { }
            whenever(repository.setPinEnabled(true)).then { }

            // When
            val result = useCase(pin, currentAttempts = 0)

            // Then
            assertTrue(result is PinResult.Success)
            verify(repository).setSessionLocked(false)
            verify(repository).setPinEnabled(true)
        }

    @Test
    fun `invoke returns Failed with attempts left when PIN is incorrect`() =
        runTest {
            // Given
            val pin = "0000"
            val storedPin = "1234"
            whenever(repository.getStoredPin()).thenReturn(storedPin)

            // When - First attempt
            val result = useCase(pin, currentAttempts = 0)

            // Then
            assertTrue(result is PinResult.Failed)
            assertEquals(2, result.attemptsLeft)
        }

    @Test
    fun `invoke returns TooManyAttempts when max attempts exceeded`() =
        runTest {
            // Given
            val pin = "0000"
            val storedPin = "1234"
            whenever(repository.getStoredPin()).thenReturn(storedPin)

            // When - Third attempt (last one)
            val result = useCase(pin, currentAttempts = 2)

            // Then
            assertTrue(result is PinResult.TooManyAttempts)
        }

    @Test
    fun `invoke returns NoPinStored when no PIN is configured`() =
        runTest {
            // Given
            whenever(repository.getStoredPin()).thenReturn(null)

            // When
            val result = useCase("1234", currentAttempts = 0)

            // Then
            assertTrue(result is PinResult.NoPinStored)
        }

    @Test
    fun `invoke with second attempt returns correct attempts left`() =
        runTest {
            // Given
            val pin = "0000"
            val storedPin = "1234"
            whenever(repository.getStoredPin()).thenReturn(storedPin)

            // When - Second attempt
            val result = useCase(pin, currentAttempts = 1)

            // Then
            assertTrue(result is PinResult.Failed)
            assertEquals(1, result.attemptsLeft)
        }
}
