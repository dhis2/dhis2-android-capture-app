package org.dhis2.mobile.sync.domain

import kotlinx.coroutines.runBlocking
import org.dhis2.mobile.sync.data.SyncEventRepository
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertTrue

class SyncEventTest {
    private val repository: SyncEventRepository = mock()
    private val useCase = SyncEvent(repository)

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
}
