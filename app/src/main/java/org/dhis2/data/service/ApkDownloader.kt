package org.dhis2.data.service

import java.io.File

/**
 * Downloads app update APKs through the platform download service into app-specific storage.
 *
 * Kept as a seam so the download flow in [ApkUpdateRepository] can be unit tested without the
 * Android framework.
 */
interface ApkDownloader {
    /**
     * Deletes any previously downloaded update and enqueues [url] to be saved as [fileName].
     *
     * @return the id of the enqueued download.
     */
    fun enqueue(
        url: String,
        fileName: String,
    ): Long

    /** Current status of the download identified by [downloadId]. */
    fun status(downloadId: Long): ApkDownloadStatus

    /** Cancels the download identified by [downloadId] and deletes any partial file. */
    fun remove(downloadId: Long)

    /** Location where [fileName] is stored once downloaded. */
    fun downloadedFile(fileName: String): File

    /**
     * Starts listening for download completion broadcasts. [onComplete] receives the id carried by
     * each broadcast, which is untrusted: callers must check it against the id they enqueued.
     *
     * @return a function that stops listening. It is safe to call it more than once.
     */
    fun listenForCompletion(onComplete: (downloadId: Long) -> Unit): () -> Unit
}

sealed interface ApkDownloadStatus {
    data object Running : ApkDownloadStatus

    data object Successful : ApkDownloadStatus

    data class Failed(
        val reason: Int?,
    ) : ApkDownloadStatus
}
