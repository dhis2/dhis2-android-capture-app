package org.dhis2.data.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.commons.error.DomainErrorMapper
import org.dhis2.usescases.main.domain.model.DownloadMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class ApkUpdateRepositoryTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private val versionRepository: VersionRepository = mock()
    private val domainErrorMapper: DomainErrorMapper = mock()
    private lateinit var apkDownloader: FakeApkDownloader

    private fun TestScope.repository(): ApkUpdateRepository {
        apkDownloader = FakeApkDownloader(tempFolder.root)
        return ApkUpdateRepository(
            versionRepository = versionRepository,
            apkDownloader = apkDownloader,
            domainErrorMapper = domainErrorMapper,
            dispatcher = Dispatcher(io = UnconfinedTestDispatcher(testScheduler)),
        )
    }

    @Test
    fun `should fail when there is no download url`() =
        runTest {
            val repository = repository()
            whenever(versionRepository.getUrl()).thenReturn(null)

            val result = repository.downloadLatestVersion()

            assertTrue(result.exceptionOrNull() is DomainError.DataNotFoundError)
            assertTrue(apkDownloader.enqueued.isEmpty())
        }

    @Test
    fun `should return the url without downloading when it is not an apk`() =
        runTest {
            val repository = repository()
            whenever(versionRepository.getUrl()).thenReturn(RELEASES_URL)

            val result = repository.downloadLatestVersion()

            assertEquals(DownloadMethod.Url(RELEASES_URL), result.getOrNull())
            assertTrue(apkDownloader.enqueued.isEmpty())
            assertEquals(0, apkDownloader.registeredListeners)
        }

    @Test
    fun `should ignore a broadcast for another download`() =
        runTest {
            val repository = repository()
            whenever(versionRepository.getUrl()).thenReturn(APK_URL)

            val download = async(UnconfinedTestDispatcher(testScheduler)) { repository.downloadLatestVersion() }
            apkDownloader.status = ApkDownloadStatus.Successful
            apkDownloader.createDownloadedFile()
            apkDownloader.broadcast(OTHER_DOWNLOAD_ID)

            assertTrue(download.isActive)
            assertEquals(1, apkDownloader.registeredListeners)

            apkDownloader.broadcast(DOWNLOAD_ID)

            assertTrue(download.await().getOrNull() is DownloadMethod.File)
        }

    @Test
    fun `should fail and stop listening when the download failed`() =
        runTest {
            val repository = repository()
            whenever(versionRepository.getUrl()).thenReturn(APK_URL)

            val download = async(UnconfinedTestDispatcher(testScheduler)) { repository.downloadLatestVersion() }
            apkDownloader.status = ApkDownloadStatus.Failed(reason = 404)
            apkDownloader.broadcast(DOWNLOAD_ID)

            assertTrue(download.await().exceptionOrNull() is DomainError.UnexpectedError)
            assertEquals(0, apkDownloader.registeredListeners)
        }

    @Test
    fun `should return the downloaded apk and stop listening when the download succeeded`() =
        runTest {
            val repository = repository()
            whenever(versionRepository.getUrl()).thenReturn(APK_URL)

            val download = async(UnconfinedTestDispatcher(testScheduler)) { repository.downloadLatestVersion() }
            apkDownloader.status = ApkDownloadStatus.Successful
            val apk = apkDownloader.createDownloadedFile()
            apkDownloader.broadcast(DOWNLOAD_ID)

            assertEquals(DownloadMethod.File(apk.absolutePath), download.await().getOrNull())
            assertEquals(listOf(APK_URL to APK_NAME), apkDownloader.enqueued)
            assertEquals(0, apkDownloader.registeredListeners)
        }

    @Test
    fun `should fail when the successful download is missing`() =
        runTest {
            val repository = repository()
            whenever(versionRepository.getUrl()).thenReturn(APK_URL)

            val download = async(UnconfinedTestDispatcher(testScheduler)) { repository.downloadLatestVersion() }
            apkDownloader.status = ApkDownloadStatus.Successful
            apkDownloader.broadcast(DOWNLOAD_ID)

            assertTrue(download.await().exceptionOrNull() is DomainError.DataNotFoundError)
            assertEquals(0, apkDownloader.registeredListeners)
        }

    @Test
    fun `should complete when the download finished before its id was known`() =
        runTest {
            val repository = repository()
            whenever(versionRepository.getUrl()).thenReturn(APK_URL)
            apkDownloader.status = ApkDownloadStatus.Successful
            val apk = apkDownloader.createDownloadedFile()

            val result = repository.downloadLatestVersion()

            assertEquals(DownloadMethod.File(apk.absolutePath), result.getOrNull())
            assertEquals(0, apkDownloader.registeredListeners)
        }

    @Test
    fun `should cancel the download and stop listening when cancelled`() =
        runTest {
            val repository = repository()
            whenever(versionRepository.getUrl()).thenReturn(APK_URL)

            val download = async(UnconfinedTestDispatcher(testScheduler)) { repository.downloadLatestVersion() }
            download.cancel()

            assertEquals(listOf(DOWNLOAD_ID), apkDownloader.removed)
            assertEquals(0, apkDownloader.registeredListeners)
        }

    @Test
    fun `should not pile up listeners on repeated downloads`() =
        runTest {
            val repository = repository()
            whenever(versionRepository.getUrl()).thenReturn(APK_URL)
            apkDownloader.status = ApkDownloadStatus.Failed(reason = null)

            repeat(3) {
                val download = async(UnconfinedTestDispatcher(testScheduler)) { repository.downloadLatestVersion() }
                download.await()
            }

            assertEquals(0, apkDownloader.registeredListeners)
        }

    private class FakeApkDownloader(
        private val dir: File,
    ) : ApkDownloader {
        var status: ApkDownloadStatus = ApkDownloadStatus.Running
        val enqueued = mutableListOf<Pair<String, String>>()
        val removed = mutableListOf<Long>()
        var registeredListeners = 0
            private set
        private val listeners = mutableListOf<(Long) -> Unit>()

        override fun enqueue(
            url: String,
            fileName: String,
        ): Long {
            enqueued += url to fileName
            return DOWNLOAD_ID
        }

        override fun status(downloadId: Long): ApkDownloadStatus = status

        override fun remove(downloadId: Long) {
            removed += downloadId
        }

        override fun downloadedFile(fileName: String): File = File(dir, fileName)

        override fun listenForCompletion(onComplete: (downloadId: Long) -> Unit): () -> Unit {
            listeners += onComplete
            registeredListeners++
            var stopped = false
            return {
                if (!stopped) {
                    stopped = true
                    listeners -= onComplete
                    registeredListeners--
                }
            }
        }

        fun broadcast(downloadId: Long) {
            listeners.toList().forEach { it(downloadId) }
        }

        fun createDownloadedFile(): File = downloadedFile(APK_NAME).apply { writeText("apk") }
    }

    private companion object {
        const val APK_NAME = "dhis2-v3.3.apk"
        const val APK_URL = "https://github.com/dhis2/dhis2-android-capture-app/releases/download/v3.3/$APK_NAME"
        const val RELEASES_URL = "https://github.com/dhis2/dhis2-android-capture-app/releases"
        const val DOWNLOAD_ID = 42L
        const val OTHER_DOWNLOAD_ID = 7L
    }
}
