package org.dhis2.usescases.videoGuide

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.dhis2.usescases.videoGuide.domain.model.VideoItem

class VideoGuideViewModel(
    private val repository: VideoGuideRepository,
) : ViewModel() {
    private val _videoList = MutableLiveData<List<VideoItem>>()
    val videoList: LiveData<List<VideoItem>> = _videoList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadVideos()
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
}

