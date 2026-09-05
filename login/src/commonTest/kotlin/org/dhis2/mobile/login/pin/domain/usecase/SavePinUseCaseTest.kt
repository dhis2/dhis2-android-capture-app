package org.dhis2.mobile.login.pin.domain.usecase

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.login.pin.data.SessionRepository
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

class SavePinUseCaseTest {
    private lateinit var useCase: SavePinUseCase
    private val repository: SessionRepository = mock()

    @BeforeTest
    fun setUp() {
        useCase = SavePinUseCase(repository)
    }

    @Test
    fun `invoke saves PIN and configures session correctly`() =
        runTest {
            // Given
            val pin = "1234"
            whenever(repository.savePin(pin)).then { }
            whenever(repository.setSessionLocked(true)).then { }

            // When
            val result = useCase(pin)

            // Then
            assertTrue(result.isSuccess)
            verify(repository).savePin(pin)
            verify(repository).setSessionLocked(true)
        }

    @Test
    fun `invoke returns failure when repository throws exception`() =
        runTest {
            // Given
            val pin = "1234"
            val exception = RuntimeException("Save failed")
            whenever(repository.savePin(pin)).thenThrow(exception)

            // When
            val result = useCase(pin)

            // Then
            assertTrue(result.isFailure)
            verify(repository).savePin(pin)
        }

    @Test
    fun `GIVEN a repository failing with a DomainError WHEN the PIN is saved THEN the same error is reported as a failure`() =
        runTest {
            // Given - DomainError extends Throwable, so it used to escape the use case and crash
            // the ViewModel scope instead of failing the Result.
            // Note: mockito's doThrow rejects a DomainError as a "checked exception", hence thenAnswer.
            val pin = "1234"
            val error = DomainError.DatabaseError("The PIN could not be saved")
            whenever(repository.savePin(pin)).thenAnswer { throw error }

            // When
            val result = useCase(pin)

            // Then
            assertTrue(result.isFailure)
            assertSame(error, result.exceptionOrNull())
        }

    @Test
    fun `GIVEN a save failing with an expired session WHEN the PIN is saved THEN the renewal error survives`() =
        runTest {
            // Given - anything less than the same instance and the session renewal dialog never fires
            val pin = "1234"
            val error = DomainError.SessionRenewalRequiredError("Your session has expired")
            whenever(repository.savePin(pin)).thenAnswer { throw error }

            // When
            val result = useCase(pin)

            // Then
            assertSame(error, result.exceptionOrNull())
        }

    @Test
    fun `GIVEN a PIN operation in flight WHEN the coroutine is cancelled THEN the cancellation propagates`() =
        runTest {
            // Given - cancellation is not a failure, so it must not become a failed Result
            val pin = "1234"
            val cancellation = CancellationException("the screen was closed")
            whenever(repository.savePin(pin)).thenAnswer { throw cancellation }

            // When & Then
            assertSame(cancellation, assertFailsWith<CancellationException> { useCase(pin) })
        }
}
