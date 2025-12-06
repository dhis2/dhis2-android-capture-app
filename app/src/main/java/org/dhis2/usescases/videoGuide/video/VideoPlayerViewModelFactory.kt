package org.dhis2.usescases.videoGuide.video

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dhis2.usescases.videoGuide.VideoGuideRepository

class VideoPlayerViewModelFactory(
    private val repository: VideoGuideRepository,
    private val downloadManager: VideoDownloadManager,
    private val context: Context,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VideoPlayerViewModel(repository, downloadManager, context) as T
    }
}

