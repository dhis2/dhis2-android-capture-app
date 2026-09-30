package org.dhis2.mobile.login.pin.domain.usecase

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.login.pin.data.SessionRepository
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GetIsSessionLockedUseCaseTest {
    private val sessionRepository: SessionRepository = mock()
    private val useCase = GetIsSessionLockedUseCase(sessionRepository)

    @Test
    fun `GIVEN a logged-out token account THEN it is locked without reading the datastore`() =
        runTest {
            val result = useCase(requireOfflineCredentials = true)

            assertTrue(result)
            verify(sessionRepository, never()).isSessionLocked()
        }

    @Test
    fun `GIVEN a non-token account THEN it defers to the stored session-lock state`() =
        runTest {
            whenever(sessionRepository.isSessionLocked()) doReturn true
            assertTrue(useCase(requireOfflineCredentials = false))

            whenever(sessionRepository.isSessionLocked()) doReturn false
            assertFalse(useCase(requireOfflineCredentials = false))
        }
}
