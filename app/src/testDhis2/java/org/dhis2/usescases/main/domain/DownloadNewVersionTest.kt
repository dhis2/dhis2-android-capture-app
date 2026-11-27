package org.dhis2.usescases.main.domain

import android.content.Context
import kotlinx.coroutines.test.runTest
import org.dhis2.data.service.ApkUpdateRepository
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.usescases.main.domain.model.DownloadMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class DownloadNewVersionTest {
    private val apkUpdateRepository: ApkUpdateRepository = mock()
    private val context: Context = mock()
    private val downloadNewVersion = DownloadNewVersion(apkUpdateRepository)

    @Test
    fun `should return the downloaded file`() =
        runTest {
            val file = DownloadMethod.File("/updates/dhis2.apk")
            whenever(apkUpdateRepository.downloadLatestVersion()).thenReturn(Result.success(file))

            val result = downloadNewVersion(context)

            assertEquals(file, result.getOrNull())
        }

    @Test
    fun `should return the url when the update is not an apk`() =
        runTest {
            val url = DownloadMethod.Url("https://example.org/releases")
            whenever(apkUpdateRepository.downloadLatestVersion()).thenReturn(Result.success(url))

            val result = downloadNewVersion(context)

            assertEquals(url, result.getOrNull())
        }

    @Test
    fun `should return the repository failure`() =
        runTest {
            val error = DomainError.UnexpectedError("download failed")
            whenever(apkUpdateRepository.downloadLatestVersion()).thenReturn(Result.failure(error))

            val result = downloadNewVersion(context)

            assertEquals(error, result.exceptionOrNull())
        }

    @Test
    fun `should return failure if an exception is thrown`() =
        runTest {
            whenever(apkUpdateRepository.downloadLatestVersion()).thenAnswer {
                throw IllegalStateException("storage not available")
            }

            val result = downloadNewVersion(context)

            assertTrue(result.exceptionOrNull() is IllegalStateException)
        }
}
