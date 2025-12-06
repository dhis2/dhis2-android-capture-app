package org.dhis2.usescases.videoGuide

import org.dhis2.usescases.videoGuide.data.datasource.VideoLocalDataSource
import org.dhis2.usescases.videoGuide.data.datasource.VideoRemoteDataSource
import org.dhis2.usescases.videoGuide.domain.model.VideoItem
import javax.inject.Inject

class VideoGuideRepository @Inject constructor(
    private val dataSource: VideoRemoteDataSource,
    private val localDataSource: VideoLocalDataSource,
) {
    suspend fun getVideoList(): List<VideoItem> {
        return dataSource.getVideoList()
    }

    suspend fun getVideoById(videoId: String): VideoItem? {
        // まずローカルDBから取得を試みる
        val localVideo = localDataSource.getDownloadedVideoById(videoId)
        if (localVideo != null) {
            return localVideo
        }
        // ローカルにない場合はリモートから取得
        return dataSource.getVideoById(videoId)
    }

    suspend fun getDownloadedVideoById(videoId: String): VideoItem? {
        return localDataSource.getDownloadedVideoById(videoId)
    }

    suspend fun getDownloadedVideoList(): List<VideoItem> {
        return localDataSource.getAllDownloadedVideos()
    }

    suspend fun isVideoDownloaded(videoId: String): Boolean {
        return localDataSource.isDownloaded(videoId)
    }

    suspend fun getLocalFilePath(videoId: String): String? {
        return localDataSource.getLocalFilePath(videoId)
    }

    suspend fun saveDownloadedVideo(videoItem: VideoItem, localFilePath: String) {
        localDataSource.saveDownloadedVideo(videoItem, localFilePath)
    }
}

