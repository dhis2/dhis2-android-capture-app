package org.dhis2.usescases.videoGuide.data.datasource

import org.dhis2.usescases.videoGuide.domain.model.VideoItem

interface VideoLocalDataSource {
    suspend fun getAllDownloadedVideos(): List<VideoItem>
    suspend fun getDownloadedVideoById(videoId: String): VideoItem?
    suspend fun saveDownloadedVideo(video: VideoItem, localFilePath: String)
    suspend fun deleteDownloadedVideo(videoId: String)
    suspend fun isDownloaded(videoId: String): Boolean
    suspend fun getLocalFilePath(videoId: String): String?
}

