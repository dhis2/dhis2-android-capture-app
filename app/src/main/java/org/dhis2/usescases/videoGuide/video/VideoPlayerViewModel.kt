package org.dhis2.usescases.videoGuide.video

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.dhis2.usescases.videoGuide.VideoGuideRepository
import org.dhis2.usescases.videoGuide.domain.model.VideoItem
import androidx.media3.exoplayer.offline.Download
import timber.log.Timber
import java.io.File

class VideoPlayerViewModel(
    private val repository: VideoGuideRepository,
    private val downloadManager: VideoDownloadManager,
    private val context: Context,
) : ViewModel() {
    private val _videoItem = MutableLiveData<VideoItem?>()
    val videoItem: LiveData<VideoItem?> = _videoItem

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // ダウンロード状態（現在の動画）
    private val _downloadState = MutableLiveData<Download?>()
    val downloadState: LiveData<Download?> = _downloadState

    private val _downloadProgress = MutableLiveData<Int>()
    val downloadProgress: LiveData<Int> = _downloadProgress

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
        // 現在の動画のダウンロード状態も更新
        _videoItem.value?.let { video ->
            _downloadState.postValue(downloads[video.id])
            downloads[video.id]?.let { download ->
                val progress = downloadManager.getDownloadProgress(video.id)
                _downloadProgress.postValue(progress)
            }
        }
    }

    init {
        // ダウンロード状態の監視を開始
        downloadManager.downloadStates.observeForever(downloadStatesObserver)
    }

    override fun onCleared() {
        super.onCleared()
        // Observerを削除してメモリリークを防止
        downloadManager.downloadStates.removeObserver(downloadStatesObserver)
    }

    fun loadVideo(videoId: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            _errorMessage.postValue(null)
            try {
                // getVideoById()は既にローカルDBからも取得を試みるように拡張済み
                val video = repository.getVideoById(videoId)
                
                if (video != null) {
                    _videoItem.postValue(video)
                    // ダウンロード状態を確認
                    checkDownloadState()
                } else {
                    _errorMessage.postValue("Video not found")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Failed to load video: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    /**
     * 現在の動画をダウンロード
     */
    fun startDownload() {
        _videoItem.value?.let { video ->
            downloadManager.downloadVideo(video)
        }
    }

    /**
     * ダウンロードをキャンセル
     */
    fun cancelDownload() {
        _videoItem.value?.let { video ->
            downloadManager.cancelDownload(video.id)
        }
    }

    /**
     * ダウンロード状態を確認
     */
    fun checkDownloadState() {
        _videoItem.value?.let { video ->
            val state = downloadManager.getDownloadState(video.id)
            _downloadState.postValue(state)
            if (state != null) {
                val progress = downloadManager.getDownloadProgress(video.id)
                _downloadProgress.postValue(progress)
            }
        }
    }
}

