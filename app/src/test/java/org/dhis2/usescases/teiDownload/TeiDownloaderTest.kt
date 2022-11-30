package org.dhis2.usescases.teiDownload

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.nhaarman.mockitokotlin2.willAnswer
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.metadata.usecases.FileResourceConfiguration
import org.dhis2.metadata.usecases.ProgramConfiguration
import org.dhis2.metadata.usecases.TrackedEntityInstanceConfiguration
import org.dhis2.usescases.searchTrackEntity.TeiDownloadResult
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.hisp.dhis.android.core.maintenance.D2ErrorComponent
import org.hisp.dhis.android.core.trackedentity.internal.TrackedEntityInstanceDownloader
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TeiDownloaderTest {
    private val programConfiguration: ProgramConfiguration = mock()
    private val teiConfiguration: TrackedEntityInstanceConfiguration = mock()
    private val fileConfiguration: FileResourceConfiguration = mock()
    private val resources: ResourceManager = mock()
    private val trackedEntityDownloader: TrackedEntityInstanceDownloader = mock()

    private fun teiDownloader(programUid: String?) = TeiDownloader(
        programConfiguration,
        teiConfiguration,
        fileConfiguration,
        programUid,
        resources
    )

    @Before
    fun setUp() {
        whenever(teiConfiguration.downloader(any(), anyOrNull())) doReturn trackedEntityDownloader
    }

    @Test
    fun shouldReturnTeiNotDownloadedForProgram() {
        whenever(teiConfiguration.hasBeenDownloaded("teiUid")) doReturn false
        val result = teiDownloader("programUid").download("teiUid", null, null)
        assertTrue(result is TeiDownloadResult.TeiNotDownloaded)
    }

    @Test
    fun shouldReturnTeiNotDownloadedForNullProgram() {
        whenever(teiConfiguration.hasBeenDownloaded("teiUid")) doReturn false
        val result = teiDownloader(null).download("teiUid", null, null)
        assertTrue(result is TeiDownloadResult.TeiNotDownloaded)
    }

    @Test
    fun shouldReturnDownloadResultForProgram() {
        whenever(teiConfiguration.hasBeenDownloaded("teiUid")) doReturn true
        whenever(teiConfiguration.hasEnrollmentInProgram("teiUid", "programUid")) doReturn true
        whenever(teiConfiguration.enrollmentUid("teiUid", "programUid")) doReturn "enrollmentUid"
        val result = teiDownloader("programUid").download("teiUid", null, null)
        assertTrue(result is TeiDownloadResult.DownloadedResult)
    }

    @Test
    fun shouldReturnDownloadedResultForNullProgram() {
        whenever(teiConfiguration.hasBeenDownloaded("teiUid")) doReturn true
        whenever(teiConfiguration.hasEnrollmentInProgram("teiUid", "programUid")) doReturn false
        val result = teiDownloader(null).download("teiUid", null, null)
        assertTrue(result is TeiDownloadResult.DownloadedResult)
    }

    @Test
    fun shouldReturnTeiToEnrollForProgram() {
        whenever(teiConfiguration.hasBeenDownloaded("teiUid")) doReturn true
        whenever(teiConfiguration.hasEnrollmentInProgram("teiUid", "programUid")) doReturn false
        whenever(programConfiguration.canEnrollNewTei("programUid")) doReturn true
        val result = teiDownloader("programUid").download("teiUid", null, null)
        assertTrue(result is TeiDownloadResult.TeiToEnroll)
    }

    @Test
    fun shouldReturnBreakTheGlassResult() {
        given(teiConfiguration.downloadAndOverwrite(trackedEntityDownloader)) willAnswer {
            throw D2Error.builder()
                .errorCode(
                    D2ErrorCode.OWNERSHIP_ACCESS_DENIED
                )
                .errorComponent(D2ErrorComponent.Server)
                .errorDescription("description")
                .build()
        }
        val result = teiDownloader("programUid").download("teiUid", null, null)
        assertTrue(result is TeiDownloadResult.BreakTheGlassResult)
    }

    @Test
    fun shouldReturnBreakTheGlassResultWithCause() {
        given(teiConfiguration.downloadAndOverwrite(trackedEntityDownloader)) willAnswer {
            throw Exception(
                D2Error.builder().errorCode(
                    D2ErrorCode.OWNERSHIP_ACCESS_DENIED
                )
                    .errorComponent(D2ErrorComponent.Server)
                    .errorDescription("description")
                    .build()
            )
        }
        val result = teiDownloader("programUid").download("teiUid", null, null)
        assertTrue(result is TeiDownloadResult.BreakTheGlassResult)
    }

    @Test
    fun shouldReturnErrorResult() {
        given(teiConfiguration.downloadAndOverwrite(trackedEntityDownloader)) willAnswer {
            throw Exception("errorMessage")
        }
        val result = teiDownloader("programUid").download("teiUid", null, null)
        assertTrue(result is TeiDownloadResult.ErrorResult)
    }

    @Test
    fun shouldDownloadWithBreakTheGlass() {
        given(teiConfiguration.downloadAndOverwrite(trackedEntityDownloader)) willAnswer {
            throw D2Error.builder()
                .errorCode(
                    D2ErrorCode.OWNERSHIP_ACCESS_DENIED
                )
                .errorComponent(D2ErrorComponent.Server)
                .errorDescription("description")
                .build()
        }
        val downloader = teiDownloader("programUid")
        downloader.download("teiUid", null, null)
        downloader.download("teiUid", null, "because")

        verify(teiConfiguration).downloadWithReason(
            trackedEntityDownloader,
            "teiUid",
            "programUid",
            "because"
        )
        verify(fileConfiguration).download()
    }
}
