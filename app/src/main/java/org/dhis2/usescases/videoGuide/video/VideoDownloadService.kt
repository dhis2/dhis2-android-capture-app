@file:OptIn(androidx.media3.common.util.UnstableApi::class)

package org.dhis2.usescases.videoGuide.video

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadService
import org.dhis2.R

/**
 * ExoPlayerのDownloadServiceを継承したフォアグラウンドサービス
 * 動画のダウンロードを管理する
 */
class VideoDownloadService : DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    DOWNLOAD_NOTIFICATION_CHANNEL_ID,
    R.string.download_notification_channel_name,
    0
) {
    companion object {
        private const val FOREGROUND_NOTIFICATION_ID = 1
        private const val DOWNLOAD_NOTIFICATION_CHANNEL_ID = "video_download_channel"
        private const val DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL = 1000L

        private var downloadManagerInstance: DownloadManager? = null

        /**
         * DownloadManagerインスタンスを設定
         * VideoDownloadManagerから呼び出される
         */
        fun setDownloadManager(downloadManager: DownloadManager) {
            downloadManagerInstance = downloadManager
        }
    }

    override fun getDownloadManager(): DownloadManager {
        return downloadManagerInstance
            ?: throw IllegalStateException("DownloadManager is not initialized. Call VideoDownloadService.setDownloadManager() first.")
    }

    override fun getForegroundNotification(
        downloads: MutableList<androidx.media3.exoplayer.offline.Download>,
        notMetRequirements: Int,
    ): Notification {
        return createNotification(downloads, notMetRequirements)
    }

    /**
     * ダウンロード通知を作成
     */
    private fun createNotification(
        downloads: MutableList<androidx.media3.exoplayer.offline.Download>,
        notMetRequirements: Int,
    ): Notification {
        createNotificationChannel()

        val activeDownloadCount = downloads.count { it.state == androidx.media3.exoplayer.offline.Download.STATE_DOWNLOADING }
        
        val title = if (activeDownloadCount > 0) {
            applicationContext.getString(R.string.downloading_videos, activeDownloadCount)
        } else {
            applicationContext.getString(R.string.download_service_running)
        }

        val contentText = when {
            activeDownloadCount > 0 -> {
                val downloading = downloads.firstOrNull { it.state == androidx.media3.exoplayer.offline.Download.STATE_DOWNLOADING }
                downloading?.let { download ->
                    val progress = if (download.contentLength > 0) {
                        ((download.bytesDownloaded * 100) / download.contentLength).toInt()
                    } else {
                        0
                    }
                    applicationContext.getString(R.string.download_progress, progress)
                } ?: applicationContext.getString(R.string.download_in_progress)
            }
            downloads.isNotEmpty() -> {
                applicationContext.getString(R.string.download_queued)
            }
            else -> {
                applicationContext.getString(R.string.download_service_running)
            }
        }

        val builder = NotificationCompat.Builder(this, DOWNLOAD_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sync)
            .setContentTitle(title)
            .setContentText(contentText)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        // ダウンロード中の場合は進捗バーを表示
        val downloading = downloads.firstOrNull { it.state == androidx.media3.exoplayer.offline.Download.STATE_DOWNLOADING }
        if (downloading != null && downloading.contentLength > 0) {
            val progress = ((downloading.bytesDownloaded * 100) / downloading.contentLength).toInt()
            builder.setProgress(100, progress, false)
        } else {
            builder.setProgress(0, 0, true) // 不確定な進捗
        }

        return builder.build()
    }

    /**
     * 通知チャンネルを作成（Android 8.0以降）
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                DOWNLOAD_NOTIFICATION_CHANNEL_ID,
                getString(R.string.download_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.download_notification_channel_description)
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun getScheduler(): androidx.media3.exoplayer.scheduler.Scheduler? {
        return null // 即座にダウンロードを開始
    }
}

