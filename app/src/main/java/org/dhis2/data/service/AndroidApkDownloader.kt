package org.dhis2.data.service

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.util.concurrent.atomic.AtomicBoolean

/**
 * [ApkDownloader] backed by [DownloadManager].
 *
 * No destination is set on the request, so the download service keeps the file in its own private
 * cache, which no app can access directly. Once finished, the file is read by its download id and
 * copied to `updates/` in internal storage, which no other app can read or write on any API level.
 * External storage is never used.
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
        val privateDir = privateUpdatesDir()
        if (privateDir.exists() && !privateDir.deleteRecursively()) {
            // Not fatal: the new download overwrites a file with the same name.
            Timber.w("Could not delete the previously downloaded app update")
        }
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

    override fun moveToPrivateStorage(
        downloadId: Long,
        fileName: String,
    ): File? {
        val download =
            try {
                downloadManager.openDownloadedFile(downloadId)
            } catch (notFound: FileNotFoundException) {
                Timber.w(notFound, "The download service has no file for the app update")
                return null
            }
        val privateDir = privateUpdatesDir()
        check(privateDir.isDirectory || privateDir.mkdirs()) {
            "Could not create the app update directory"
        }
        val apk = File(privateDir, fileName)
        ParcelFileDescriptor.AutoCloseInputStream(download).use { input ->
            apk.outputStream().use { output -> input.copyTo(output) }
        }
        // Frees the download service's copy and dismisses its notification.
        downloadManager.remove(downloadId)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            // Before API 24 the installer only accepts file:// URIs, so it must be able to read
            // the APK directly. filesDir itself is already traversable by other users.
            check(privateDir.setExecutable(true, false) && apk.setReadable(true, false)) {
                "Could not make the app update readable by the package installer"
            }
        }
        return apk
    }

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

    private fun privateUpdatesDir(): File = File(appContext.filesDir, UPDATES_DIR)

    private companion object {
        const val UPDATES_DIR = "updates"
    }
}
