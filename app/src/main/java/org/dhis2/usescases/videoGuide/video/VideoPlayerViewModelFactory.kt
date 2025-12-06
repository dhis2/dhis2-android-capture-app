package org.dhis2.usescases.videoGuide.video

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dhis2.usescases.videoGuide.VideoGuideRepository

class VideoPlayerViewModelFactory(
    private val repository: VideoGuideRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VideoPlayerViewModel(repository) as T
    }
}

