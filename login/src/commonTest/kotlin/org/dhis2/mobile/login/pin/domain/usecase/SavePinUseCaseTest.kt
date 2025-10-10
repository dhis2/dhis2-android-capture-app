package org.dhis2.mobile.login.pin.domain.usecase

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.login.pin.data.SessionRepository
import org.junit.Before
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertTrue

class SavePinUseCaseTest {
    private lateinit var useCase: SavePinUseCase
    private val repository: SessionRepository = mock()

    @Before
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
}
