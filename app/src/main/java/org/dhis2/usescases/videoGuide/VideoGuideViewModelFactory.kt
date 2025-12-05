package org.dhis2.usescases.videoGuide

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class VideoGuideViewModelFactory(
    private val repository: VideoGuideRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VideoGuideViewModel(repository) as T
    }
}

