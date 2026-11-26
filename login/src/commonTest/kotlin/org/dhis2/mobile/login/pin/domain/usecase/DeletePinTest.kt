package org.dhis2.mobile.login.pin.domain.usecase

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.login.pin.data.SessionRepository
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DeletePinTest {
    private lateinit var useCase: DeletePin
    private val repository: SessionRepository = mock()

    @BeforeTest
    fun setUp() {
        useCase = DeletePin(repository)
    }

    @Test
    fun `invoke deletes PIN and unlocks session`() =
        runTest {
            // Given
            whenever(repository.deletePin()).then { }
            whenever(repository.setSessionLocked(false)).then { }

            // When
            val result = useCase(Unit)

            // Then
            assertTrue(result.isSuccess)
            verify(repository).deletePin()
            verify(repository).setSessionLocked(false)
        }

    @Test
    fun `invoke returns failure when repository throws exception`() =
        runTest {
            // Given
            val exception = RuntimeException("delete failed")
            whenever(repository.deletePin()).thenThrow(exception)

            // When
            val result = useCase(Unit)

            // Then
            assertTrue(result.isFailure)
            verify(repository).deletePin()
            verify(repository, never()).setSessionLocked(any())
        }

    @Test
    fun `invoke rethrows cancellation`() =
        runTest {
            // Given
            whenever(repository.deletePin()).thenThrow(CancellationException("cancelled"))

            // When / Then
            assertFailsWith<CancellationException> { useCase(Unit) }
            verify(repository, never()).setSessionLocked(any())
        }
}
