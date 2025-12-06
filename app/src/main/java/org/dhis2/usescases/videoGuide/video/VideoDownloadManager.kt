@file:OptIn(androidx.media3.common.util.UnstableApi::class)

package org.dhis2.usescases.videoGuide.video

import android.content.Context
import android.net.Uri
import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.offline.DownloadIndex
import androidx.lifecycle.LiveData
import org.dhis2.usescases.videoGuide.data.datasource.VideoLocalDataSource
import org.dhis2.usescases.videoGuide.domain.model.VideoItem
import timber.log.Timber

/**
 * ExoPlayerのDownloadServiceと連携して動画をダウンロードするマネージャー
 */
class VideoDownloadManager(
    private val context: Context,
    private val downloadManager: DownloadManager,
    private val downloadTracker: DownloadTracker,
    private val localDataSource: VideoLocalDataSource,
) {
    private val downloadIndex: DownloadIndex

    // ダウンロード状態と進捗を公開（downloadTrackerはprivateのまま）
    val downloadStates: LiveData<Map<String, Download>> = downloadTracker.downloadStates
    val downloadProgress: LiveData<Map<String, Int>> = downloadTracker.downloadProgress

    init {
        downloadIndex = downloadManager.downloadIndex

        // DownloadTrackerをリスナーとして追加（既に追加されている可能性があるが、重複チェックはDownloadManagerが行う）
        downloadManager.addListener(downloadTracker)
    }

    /**
     * 動画のダウンロードを開始
     */
    fun downloadVideo(videoItem: VideoItem) {
        try {
            val downloadRequest = DownloadRequest.Builder(videoItem.id, Uri.parse(videoItem.videoUrl))
                .setMimeType(MimeTypes.VIDEO_MP4)
                .setData(videoItem.title.toByteArray())
                .build()

            DownloadService.sendAddDownload(
                context,
                VideoDownloadService::class.java,
                downloadRequest,
                false // 即座に開始
            )

            Timber.d("Download started for video: ${videoItem.id}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to start download for video: ${videoItem.id}")
        }
    }

    /**
     * ダウンロードをキャンセル
     */
    fun cancelDownload(videoId: String) {
        try {
            DownloadService.sendRemoveDownload(
                context,
                VideoDownloadService::class.java,
                videoId,
                false // 即座に削除
            )
            Timber.d("Download cancelled for video: $videoId")
        } catch (e: Exception) {
            Timber.e(e, "Failed to cancel download for video: $videoId")
        }
    }

    /**
     * 特定の動画のダウンロード状態を取得
     */
    fun getDownloadState(videoId: String): Download? {
        return try {
            downloadIndex.getDownload(videoId)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get download state for video: $videoId")
            null
        }
    }

    /**
     * すべてのダウンロードを取得
     */
    fun getAllDownloads(): List<Download> {
        return try {
            val downloads = mutableListOf<Download>()
            downloadIndex.getDownloads().use { cursor ->
                while (cursor.moveToNext()) {
                    downloads.add(cursor.download)
                }
            }
            downloads
        } catch (e: Exception) {
            Timber.e(e, "Failed to get all downloads")
            emptyList()
        }
    }


    /**
     * 特定の動画のダウンロード進捗を取得
     */
    fun getDownloadProgress(videoId: String): Int {
        return downloadTracker.getDownloadProgress(videoId)
    }

    /**
     * ダウンロード完了時にRoom DBに保存
     * VideoItemの完全な情報が必要なため、外部から呼び出される
     */
    suspend fun saveDownloadedVideoToDatabase(videoItem: VideoItem, localFilePath: String) {
        try {
            localDataSource.saveDownloadedVideo(videoItem, localFilePath)
            Timber.d("Saved downloaded video to database: ${videoItem.id}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to save downloaded video to database: ${videoItem.id}")
        }
    }

    /**
     * リソースのクリーンアップ
     */
    fun release() {
        downloadManager.removeListener(downloadTracker)
        downloadTracker.release()
    }
}

