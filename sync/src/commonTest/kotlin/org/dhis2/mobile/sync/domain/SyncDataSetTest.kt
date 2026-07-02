package org.dhis2.mobile.sync.domain

import kotlinx.coroutines.runBlocking
import org.dhis2.mobile.sync.data.SyncDataSetRepository
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertTrue

class SyncDataSetTest {
    private val repository: SyncDataSetRepository = mock()
    private val useCase = SyncDataSet(repository)

    @Test
    fun `should return success and call all repository methods in order`() =
        runBlocking {
            val dataSetUid = "dataSetUid"

            val result = useCase(dataSetUid)

            assertTrue(result.isSuccess)
            verify(repository).uploadDataSet(dataSetUid)
            verify(repository).uploadCompleteRegistration(dataSetUid)
            verify(repository).downloadDataSet(dataSetUid)
        }

    @Test
    fun `should return failure when uploadDataSet throws`() =
        runBlocking {
            val dataSetUid = "dataSetUid"
            whenever(repository.uploadDataSet(dataSetUid)).thenThrow(RuntimeException("upload error"))

            val result = useCase(dataSetUid)

            assertTrue(result.isFailure)
        }

    @Test
    fun `should return failure when uploadCompleteRegistration throws`() =
        runBlocking {
            val dataSetUid = "dataSetUid"
            whenever(repository.uploadCompleteRegistration(dataSetUid))
                .thenThrow(RuntimeException("registration error"))

            val result = useCase(dataSetUid)

            assertTrue(result.isFailure)
        }

    @Test
    fun `should return failure when downloadDataSet throws`() =
        runBlocking {
            val dataSetUid = "dataSetUid"
            whenever(repository.downloadDataSet(dataSetUid))
                .thenThrow(RuntimeException("download error"))

            val result = useCase(dataSetUid)

            assertTrue(result.isFailure)
        }
}
