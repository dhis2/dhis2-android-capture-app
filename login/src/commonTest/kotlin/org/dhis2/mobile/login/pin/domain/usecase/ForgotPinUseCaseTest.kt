package org.dhis2.mobile.login.pin.domain.usecase

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.login.pin.data.SessionRepository
import org.junit.Before
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertTrue

class ForgotPinUseCaseTest {
    private lateinit var useCase: ForgotPinUseCase
    private val repository: SessionRepository = mock()

    @Before
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
}
