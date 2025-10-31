package org.dhis2.usescases.main.domain

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.dhis2.data.service.VersionRepository
import org.dhis2.mobile.commons.error.DomainError
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.invocation.InvocationOnMock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.willAnswer
import org.mockito.stubbing.Answer

class DownloadNewVersionTest {
    private val versionRepository: VersionRepository = mock()
    private lateinit var downloadNewVersion: DownloadNewVersion

    @Before
    fun setUp() {
        downloadNewVersion = DownloadNewVersion(versionRepository)
    }

    @Test
    fun `should successfully download new version`() = runTest {
        // GIVEN
        val fakeUri: Uri = mock()
        whenever(
            versionRepository.download(
                context = any(),
                onDownloadCompleted = any(),
                onDownloading = any(),
            ),
        ).thenAnswer {
            // Simulate the callback being called upon successful download
            val onDownloadCompletedCallback = it.getArgument<(Uri) -> Unit>(1)
            onDownloadCompletedCallback.invoke(fakeUri)
        }
        val context: Context = mock()

        with(downloadNewVersion(context)) {
            assertTrue(isSuccess)
        }
    }

    @Test
    fun `should return failure if an exception is thrown`() = runTest {
        given(versionRepository.download(any(), any(), any())) willAnswer  {
            throw DomainError.DataBaseError("Test")
        }
        val context: Context = mock()

        with(downloadNewVersion(context)) {
            assertTrue(isFailure)
        }
    }

}