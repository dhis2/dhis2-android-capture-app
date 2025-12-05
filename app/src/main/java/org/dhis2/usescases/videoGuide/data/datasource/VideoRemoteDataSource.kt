package org.dhis2.usescases.videoGuide.data.datasource

import org.dhis2.usescases.videoGuide.domain.model.VideoItem

interface VideoRemoteDataSource {
    suspend fun getVideoList(): List<VideoItem>
    suspend fun getVideoById(videoId: String): VideoItem?
}

