package org.dhis2.data.service

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

/**
 * [ApkDownloader] backed by [DownloadManager].
 *
 * Updates are written to the app-specific external directory `updates/`, which no other app can
 * write to on any supported API level and which needs no storage permission.
 */
class AndroidApkDownloader(
    context: Context,
) : ApkDownloader {
    private val appContext = context.applicationContext
    private val downloadManager =
        appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    override fun enqueue(
        url: String,
        fileName: String,
    ): Long {
        updatesDir().listFiles()?.forEach { it.delete() }
        val request =
            DownloadManager
                .Request(url.toUri())
                .setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE,
                ).setTitle(fileName)
                .setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED,
                ).setAllowedOverMetered(true)
                .setAllowedOverRoaming(false)
                .setDestinationInExternalFilesDir(appContext, null, "$UPDATES_DIR/$fileName")
        return downloadManager.enqueue(request)
    }

    override fun status(downloadId: Long): ApkDownloadStatus =
        downloadManager
            .query(DownloadManager.Query().setFilterById(downloadId))
            ?.use { cursor ->
                if (!cursor.moveToFirst()) return@use ApkDownloadStatus.Failed(reason = null)
                val status =
                    cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> ApkDownloadStatus.Successful
                    DownloadManager.STATUS_FAILED ->
                        ApkDownloadStatus.Failed(
                            reason =
                                cursor.getInt(
                                    cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON),
                                ),
                        )
                    else -> ApkDownloadStatus.Running
                }
            } ?: ApkDownloadStatus.Failed(reason = null)

    override fun remove(downloadId: Long) {
        downloadManager.remove(downloadId)
    }

    override fun downloadedFile(fileName: String): File = File(updatesDir(), fileName)

    override fun listenForCompletion(onComplete: (downloadId: Long) -> Unit): () -> Unit {
        val receiver =
            object : BroadcastReceiver() {
                override fun onReceive(
                    context: Context,
                    intent: Intent?,
                ) {
                    if (intent?.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) return
                    onComplete(intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L))
                }
            }
        // ACTION_DOWNLOAD_COMPLETE is sent by the download provider, not by the system or this
        // app, so a non-exported receiver would not get it. The broadcast is therefore treated
        // as untrusted and validated by the caller against the enqueued id and its real status.
        ContextCompat.registerReceiver(
            appContext,
            receiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_EXPORTED,
        )
        val registered = AtomicBoolean(true)
        return {
            if (registered.compareAndSet(true, false)) {
                appContext.unregisterReceiver(receiver)
            }
        }
    }

    private fun updatesDir(): File {
        val externalFilesDir =
            checkNotNull(appContext.getExternalFilesDir(null)) {
                "App-specific external storage is not available"
            }
        return File(externalFilesDir, UPDATES_DIR)
    }

    private companion object {
        const val UPDATES_DIR = "updates"
    }
}
