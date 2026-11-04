package org.dhis2.data.service

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.commons.error.DomainErrorMapper
import org.dhis2.mobile.commons.error.withDomainErrors
import org.dhis2.usescases.main.domain.model.DownloadMethod
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.resume

/**
 * Downloads the latest app version advertised by the server.
 *
 * The completion broadcast is untrusted: only a broadcast carrying the id of the download this
 * repository enqueued is considered, and the download status is then read back from the download
 * service, so a spoofed or failed download never reaches the installer.
 */
class ApkUpdateRepository(
    private val versionRepository: VersionRepository,
    private val apkDownloader: ApkDownloader,
    private val domainErrorMapper: DomainErrorMapper,
    private val dispatcher: Dispatcher,
) {
    /**
     * Downloads the latest version. Returns [DownloadMethod.File] with the downloaded APK, or
     * [DownloadMethod.Url] when the server link is not an APK and has to be opened in a browser.
     * Cancelling the caller cancels the download and stops listening for its completion.
     */
    suspend fun downloadLatestVersion(): Result<DownloadMethod> =
        withContext(dispatcher.io) {
            val url =
                domainErrorMapper.withDomainErrors { versionRepository.getUrl() }
                    ?: return@withContext Result.failure(
                        DomainError.DataNotFoundError("No download URL for the latest app version"),
                    )
            val fileName = url.substringAfterLast("/")
            if (fileName.endsWith(APK_EXTENSION, ignoreCase = true)) {
                awaitDownload(url, fileName)
            } else {
                Result.success(DownloadMethod.Url(url))
            }
        }

    private suspend fun awaitDownload(
        url: String,
        fileName: String,
    ): Result<DownloadMethod> =
        suspendCancellableCoroutine { continuation ->
            val downloadId = AtomicLong(NO_DOWNLOAD)
            val finished = AtomicBoolean(false)
            lateinit var stopListening: () -> Unit

            fun finish(result: Result<DownloadMethod>) {
                if (finished.compareAndSet(false, true)) {
                    stopListening()
                    continuation.resume(result)
                }
            }

            fun checkStatus(id: Long) {
                when (val status = apkDownloader.status(id)) {
                    ApkDownloadStatus.Running -> Unit
                    ApkDownloadStatus.Successful -> finish(downloadedApk(fileName))
                    is ApkDownloadStatus.Failed ->
                        finish(
                            Result.failure(
                                DomainError.UnexpectedError(
                                    "App update download failed (reason: ${status.reason})",
                                ),
                            ),
                        )
                }
            }

            stopListening =
                apkDownloader.listenForCompletion { completedId ->
                    val expectedId = downloadId.get()
                    if (expectedId != NO_DOWNLOAD && completedId == expectedId) {
                        checkStatus(completedId)
                    }
                }

            continuation.invokeOnCancellation {
                stopListening()
                downloadId.get().takeIf { it != NO_DOWNLOAD }?.let(apkDownloader::remove)
            }

            val id =
                try {
                    apkDownloader.enqueue(url, fileName)
                } catch (error: Exception) {
                    stopListening()
                    throw error
                }
            downloadId.set(id)

            if (continuation.isActive) {
                // The download may have finished before its id was known to the listener.
                checkStatus(id)
            } else {
                apkDownloader.remove(id)
            }
        }

    private fun downloadedApk(fileName: String): Result<DownloadMethod> {
        val apk = apkDownloader.downloadedFile(fileName)
        return if (apk.exists()) {
            Result.success(DownloadMethod.File(apk.absolutePath))
        } else {
            Result.failure(DomainError.DataNotFoundError("Downloaded app update not found"))
        }
    }

    private companion object {
        const val APK_EXTENSION = ".apk"
        const val NO_DOWNLOAD = -1L
    }
}
