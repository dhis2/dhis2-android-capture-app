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

class ForgotPinUseCaseTest {
    private lateinit var useCase: ForgotPinUseCase
    private val repository: SessionRepository = mock()

    @BeforeTest
    fun setUp() {
        useCase = ForgotPinUseCase(repository)
    }

    @Test
    fun `invoke deletes PIN, logs out user and unlocks session`() =
        runTest {
            // Given
            whenever(repository.deletePin()).then { }
            whenever(repository.logout()).then { }
            whenever(repository.setSessionLocked(false)).then { }

            // When
            val result = useCase(Unit)

            // Then
            assertTrue(result.isSuccess)
            verify(repository).deletePin()
            verify(repository).logout()
            verify(repository).setSessionLocked(false)
        }

    @Test
    fun `invoke returns failure when repository throws exception`() =
        runTest {
            // Given
            val exception = RuntimeException("Logout failed")
            whenever(repository.deletePin()).then { }
            whenever(repository.logout()).thenThrow(exception)

            // When
            val result = useCase(Unit)

            // Then
            assertTrue(result.isFailure)
            verify(repository).deletePin()
            verify(repository).logout()
        }

    @Test
    fun `GIVEN a repository failing with a DomainError WHEN the PIN is forgotten THEN the same error is reported as a failure`() =
        runTest {
            // Given - DomainError extends Throwable, so it used to escape the use case and crash
            // the ViewModel scope instead of failing the Result.
            // Note: mockito's doThrow rejects a DomainError as a "checked exception", hence thenAnswer.
            val error = DomainError.DatabaseError("The PIN could not be deleted")
            whenever(repository.deletePin()).thenAnswer { throw error }

            // When
            val result = useCase(Unit)

            // Then
            assertTrue(result.isFailure)
            assertSame(error, result.exceptionOrNull())
        }

    @Test
    fun `GIVEN a logout failing with an expired session WHEN the PIN is forgotten THEN the renewal error survives`() =
        runTest {
            // Given - anything less than the same instance and the session renewal dialog never fires
            val error = DomainError.SessionRenewalRequiredError("Your session has expired")
            whenever(repository.deletePin()).then { }
            whenever(repository.logout()).thenAnswer { throw error }

            // When
            val result = useCase(Unit)

            // Then
            assertSame(error, result.exceptionOrNull())
        }

    @Test
    fun `GIVEN a PIN operation in flight WHEN the coroutine is cancelled THEN the cancellation propagates`() =
        runTest {
            // Given - cancellation is not a failure, so it must not become a failed Result
            val cancellation = CancellationException("the screen was closed")
            whenever(repository.deletePin()).thenAnswer { throw cancellation }

            // When & Then
            assertSame(cancellation, assertFailsWith<CancellationException> { useCase(Unit) })
        }
}
