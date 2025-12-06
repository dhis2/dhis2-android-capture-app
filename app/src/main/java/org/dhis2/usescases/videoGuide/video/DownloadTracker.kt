@file:OptIn(androidx.media3.common.util.UnstableApi::class)

package org.dhis2.usescases.videoGuide.video

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import timber.log.Timber

/**
 * ExoPlayerのDownloadManagerの状態を監視し、UIに通知するクラス
 */
class DownloadTracker(
    private val downloadManager: DownloadManager,
) : DownloadManager.Listener {

    private val _downloadStates = MutableLiveData<Map<String, Download>>()
    val downloadStates: LiveData<Map<String, Download>> = _downloadStates

    private val _downloadProgress = MutableLiveData<Map<String, Int>>()
    val downloadProgress: LiveData<Map<String, Int>> = _downloadProgress

    private val currentDownloads = mutableMapOf<String, Download>()

    init {
        downloadManager.addListener(this)
        updateDownloadStates()
    }

    override fun onDownloadChanged(
        downloadManager: DownloadManager,
        download: Download,
        finalException: java.lang.Exception?,
    ) {
        Timber.d("Download changed: ${download.request.id}, state: ${download.state}")
        
        when (download.state) {
            Download.STATE_COMPLETED -> {
                currentDownloads[download.request.id] = download
                Timber.d("Download completed: ${download.request.id}")
            }
            Download.STATE_DOWNLOADING -> {
                currentDownloads[download.request.id] = download
            }
            Download.STATE_FAILED -> {
                currentDownloads.remove(download.request.id)
                Timber.e(finalException, "Download failed: ${download.request.id}")
            }
            Download.STATE_REMOVING -> {
                currentDownloads.remove(download.request.id)
            }
            Download.STATE_QUEUED -> {
                currentDownloads[download.request.id] = download
            }
            Download.STATE_STOPPED -> {
                currentDownloads[download.request.id] = download
            }
            else -> {
                // その他の状態
            }
        }
        
        updateDownloadStates()
        updateDownloadProgress()
    }

    /**
     * ダウンロード状態を更新
     */
    private fun updateDownloadStates() {
        _downloadStates.postValue(currentDownloads.toMap())
    }

    /**
     * ダウンロード進捗を更新
     */
    private fun updateDownloadProgress() {
        val progressMap = currentDownloads.mapValues { (_, download) ->
            calculateProgress(download)
        }
        _downloadProgress.postValue(progressMap)
    }

    /**
     * ダウンロード進捗を計算（0-100%）
     */
    private fun calculateProgress(download: Download): Int {
        return when (download.state) {
            Download.STATE_COMPLETED -> 100
            Download.STATE_DOWNLOADING -> {
                val bytesDownloaded = download.bytesDownloaded
                val totalBytes = download.contentLength
                if (totalBytes > 0) {
                    ((bytesDownloaded * 100) / totalBytes).toInt().coerceIn(0, 100)
                } else {
                    0
                }
            }
            Download.STATE_QUEUED -> 0
            Download.STATE_STOPPED -> {
                val bytesDownloaded = download.bytesDownloaded
                val totalBytes = download.contentLength
                if (totalBytes > 0) {
                    ((bytesDownloaded * 100) / totalBytes).toInt().coerceIn(0, 100)
                } else {
                    0
                }
            }
            else -> 0
        }
    }

    /**
     * 特定の動画のダウンロード状態を取得
     */
    fun getDownloadState(videoId: String): Download? {
        return currentDownloads[videoId]
    }

    /**
     * 特定の動画のダウンロード進捗を取得
     */
    fun getDownloadProgress(videoId: String): Int {
        return getDownloadState(videoId)?.let { download ->
            calculateProgress(download)
        } ?: 0
    }

    /**
     * リソースのクリーンアップ
     */
    fun release() {
        downloadManager.removeListener(this)
        currentDownloads.clear()
    }
}

