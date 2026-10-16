package org.dhis2.mobile.sync.domain

import kotlinx.coroutines.runBlocking
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.commons.session.SessionRenewalNotifier
import org.dhis2.mobile.sync.data.SyncEventRepository
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SyncEventTest {
    private val repository: SyncEventRepository = mock()
    private val sessionRenewalNotifier: SessionRenewalNotifier = mock()
    private val useCase = SyncEvent(repository, sessionRenewalNotifier)

    @Test
    fun `should return success and call all repository methods in order`() =
        runBlocking {
            val eventUid = "eventUid"

            val result = useCase(eventUid)

            assertTrue(result.isSuccess)
            verify(repository).downloadEvent(eventUid)
            verify(repository).uploadEvent(eventUid)
            verify(repository).downloadFileResources(eventUid)
        }

    @Test
    fun `should return failure when downloadEvent throws`() =
        runBlocking {
            val eventUid = "eventUid"
            whenever(repository.downloadEvent(eventUid)).thenThrow(RuntimeException("download error"))

            val result = useCase(eventUid)

            assertTrue(result.isFailure)
        }

    @Test
    fun `should return failure when uploadEvent throws`() =
        runBlocking {
            val eventUid = "eventUid"
            whenever(repository.uploadEvent(eventUid)).thenThrow(RuntimeException("upload error"))

            val result = useCase(eventUid)

            assertTrue(result.isFailure)
        }

    @Test
    fun `should return failure when downloadFileResources throws`() =
        runBlocking {
            val eventUid = "eventUid"
            whenever(repository.downloadFileResources(eventUid))
                .thenThrow(RuntimeException("file resources error"))

            val result = useCase(eventUid)

            assertTrue(result.isFailure)
        }

    @Test
    fun `should request a session renewal when the tokens are no longer valid`() =
        runBlocking {
            // GIVEN - the repository reports the expired session as a domain error
            val eventUid = "eventUid"
            val error = DomainError.SessionRenewalRequiredError("Your session has expired")
            whenever(repository.uploadEvent(eventUid)).thenAnswer { throw error }

            // WHEN
            val result = useCase(eventUid)

            // THEN - the sync fails and the user is asked to log in again, instead of the error
            // escaping the use case as a DomainError is not an Exception
            assertEquals(error, result.exceptionOrNull())
            verify(sessionRenewalNotifier).notifyIfRequired(error)
        }
}
