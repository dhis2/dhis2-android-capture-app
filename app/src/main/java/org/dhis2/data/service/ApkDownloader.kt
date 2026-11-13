package org.dhis2.data.service

import java.io.File

/**
 * Downloads app update APKs through the platform download service.
 *
 * The download service runs in another process and cannot write to this app's internal storage,
 * so the download is kept in the download service's own private storage and copied into internal
 * storage with [moveToPrivateStorage] once it has finished. External storage, which other apps can
 * write to on API 23–29, is never used.
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

    /**
     * Copies the finished download [downloadId] into internal storage as [fileName] and removes it
     * from the download service.
     *
     * @return the APK in internal storage, or `null` if the download service has no file for it.
     */
    fun moveToPrivateStorage(
        downloadId: Long,
        fileName: String,
    ): File?

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
