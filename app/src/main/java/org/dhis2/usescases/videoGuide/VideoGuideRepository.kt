package org.dhis2.usescases.videoGuide

import org.dhis2.usescases.videoGuide.data.datasource.VideoRemoteDataSource
import org.dhis2.usescases.videoGuide.domain.model.VideoItem
import javax.inject.Inject

class VideoGuideRepository @Inject constructor(
    private val dataSource: VideoRemoteDataSource,
) {
    suspend fun getVideoList(): List<VideoItem> {
        return dataSource.getVideoList()
    }

    suspend fun getVideoById(videoId: String): VideoItem? {
        return dataSource.getVideoById(videoId)
    }
}

