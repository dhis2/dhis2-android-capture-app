package org.dhis2.mobile.sync.domain

import kotlinx.coroutines.runBlocking
import org.dhis2.mobile.sync.data.SyncTeiRepository
import org.dhis2.mobile.sync.model.EnrollmentInfo
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertTrue

class SyncTeiTest {
    private val repository: SyncTeiRepository = mock()
    private val useCase = SyncTei(repository)

    private val enrollmentUid = "enrollmentUid"
    private val enrollmentInfo =
        EnrollmentInfo(
            uid = enrollmentUid,
            teiUid = "teiUid",
            programUid = "programUid",
        )

    @Test
    fun `should return success and call all repository methods in order`() =
        runBlocking {
            whenever(repository.getEnrollmentInfo(enrollmentUid)).thenReturn(enrollmentInfo)

            val result = useCase(enrollmentUid)

            assertTrue(result.isSuccess)
            verify(repository).getEnrollmentInfo(enrollmentUid)
            verify(repository).uploadTei(enrollmentInfo)
            verify(repository).downloadTei(enrollmentInfo)
            verify(repository).downloadFileResources(enrollmentInfo)
        }

    @Test
    fun `should return failure when getEnrollmentInfo throws`() =
        runBlocking {
            whenever(repository.getEnrollmentInfo(enrollmentUid))
                .thenThrow(RuntimeException("enrollment error"))

            val result = useCase(enrollmentUid)

            assertTrue(result.isFailure)
        }

    @Test
    fun `should return failure when uploadTei throws`() =
        runBlocking {
            whenever(repository.getEnrollmentInfo(enrollmentUid)).thenReturn(enrollmentInfo)
            whenever(repository.uploadTei(enrollmentInfo)).thenThrow(RuntimeException("upload error"))

            val result = useCase(enrollmentUid)

            assertTrue(result.isFailure)
        }

    @Test
    fun `should return failure when downloadTei throws`() =
        runBlocking {
            whenever(repository.getEnrollmentInfo(enrollmentUid)).thenReturn(enrollmentInfo)
            whenever(repository.downloadTei(enrollmentInfo)).thenThrow(RuntimeException("download error"))

            val result = useCase(enrollmentUid)

            assertTrue(result.isFailure)
        }

    @Test
    fun `should return failure when downloadFileResources throws`() =
        runBlocking {
            whenever(repository.getEnrollmentInfo(enrollmentUid)).thenReturn(enrollmentInfo)
            whenever(repository.downloadFileResources(enrollmentInfo))
                .thenThrow(RuntimeException("file resources error"))

            val result = useCase(enrollmentUid)

            assertTrue(result.isFailure)
        }
}
