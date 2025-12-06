package org.dhis2.usescases.videoGuide.video

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.dhis2.usescases.videoGuide.VideoGuideRepository
import org.dhis2.usescases.videoGuide.domain.model.VideoItem

class VideoPlayerViewModel(
    private val repository: VideoGuideRepository,
) : ViewModel() {
    private val _videoItem = MutableLiveData<VideoItem?>()
    val videoItem: LiveData<VideoItem?> = _videoItem

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadVideo(videoId: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            _errorMessage.postValue(null)
            try {
                // まず個別取得を試す
                val video = repository.getVideoById(videoId)
                
                // 個別取得が失敗した場合、一覧から探す（フォールバック）
                // if (video == null) {
                //     val videoList = repository.getVideoList()
                //     video = videoList.find { it.id == videoId }
                // }
                
                if (video != null) {
                    _videoItem.postValue(video)
                } else {
                    _errorMessage.postValue("Video not found")
                }
            } catch (e: Exception) {
                // エラーが発生した場合も、一覧から探す（フォールバック）
                // try {
                //     val videoList = repository.getVideoList()
                //     val video = videoList.find { it.id == videoId }
                //     if (video != null) {
                //         _videoItem.postValue(video)
                //     } else {
                //         _errorMessage.postValue("Failed to load video: ${e.message}")
                //     }
                // } catch (fallbackException: Exception) {
                //     _errorMessage.postValue("Failed to load video: ${e.message}")
                // }
                _errorMessage.postValue("Failed to load video: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}

