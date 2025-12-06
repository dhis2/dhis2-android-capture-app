package org.dhis2.usescases.videoGuide

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.dhis2.usescases.videoGuide.domain.model.VideoItem
import org.dhis2.usescases.videoGuide.video.VideoDownloadManager
import androidx.media3.exoplayer.offline.Download
import timber.log.Timber
import java.io.File

class VideoGuideViewModel(
    private val repository: VideoGuideRepository,
    private val downloadManager: VideoDownloadManager,
    private val context: Context,
) : ViewModel() {
    private val _videoList = MutableLiveData<List<VideoItem>>()
    val videoList: LiveData<List<VideoItem>> = _videoList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // ダウンロード状態
    val downloadStates: LiveData<Map<String, Download>> = downloadManager.downloadStates
    val downloadProgress: LiveData<Map<String, Int>> = downloadManager.downloadProgress

    // ダウンロード完了時の監視用Observer
    private val downloadStatesObserver = Observer<Map<String, Download>> { downloads ->
        downloads.values.forEach { download ->
            if (download.state == Download.STATE_COMPLETED) {
                viewModelScope.launch {
                    val video = repository.getVideoById(download.request.id)
                    if (video != null) {
                        // Media3では、キャッシュディレクトリのパスを保存（実際のファイルパスは不要）
                        val cachePath = File(context.cacheDir, "video_downloads").absolutePath
                        repository.saveDownloadedVideo(video, cachePath)
                        Timber.d("Download completed and saved to database: ${video.id}")
                    }
                }
            }
        }
    }

    init {
        loadVideos()
        // ダウンロード状態の監視を開始
        downloadStates.observeForever(downloadStatesObserver)
    }

    override fun onCleared() {
        super.onCleared()
        // Observerを削除してメモリリークを防止
        downloadStates.removeObserver(downloadStatesObserver)
    }

    fun loadVideos() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val videos = repository.getVideoList()
                _videoList.postValue(videos)
            } catch (e: Exception) {
                _videoList.postValue(emptyList())
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    /**
     * 動画のダウンロードを開始
     */
    fun startDownload(videoId: String) {
        viewModelScope.launch {
            val video = _videoList.value?.find { it.id == videoId }
            if (video != null) {
                downloadManager.downloadVideo(video)
            } else {
                // 動画が見つからない場合は、Repositoryから取得を試みる
                val videoFromRepo = repository.getVideoById(videoId)
                if (videoFromRepo != null) {
                    downloadManager.downloadVideo(videoFromRepo)
                }
            }
        }
    }

    /**
     * ダウンロードをキャンセル
     */
    fun cancelDownload(videoId: String) {
        downloadManager.cancelDownload(videoId)
    }

    /**
     * ダウンロード状態を確認
     */
    fun checkDownloadState(videoId: String): Download? {
        return downloadManager.getDownloadState(videoId)
    }

    /**
     * ダウンロード済み動画のリストを取得
     */
    fun getDownloadedVideos() {
        viewModelScope.launch {
            try {
                val downloadedVideos = repository.getDownloadedVideoList()
                // 必要に応じて、ダウンロード済み動画のリストを更新
            } catch (e: Exception) {
                // エラーハンドリング
            }
        }
    }
}

