package org.dhis2.usescases.videoGuide.data.datasource

import org.dhis2.usescases.videoGuide.data.local.DownloadedVideoDao
import org.dhis2.usescases.videoGuide.data.local.DownloadedVideoEntity
import org.dhis2.usescases.videoGuide.domain.model.VideoItem
import javax.inject.Inject

class RoomVideoLocalDataSource @Inject constructor(
    private val dao: DownloadedVideoDao,
) : VideoLocalDataSource {

    override suspend fun getAllDownloadedVideos(): List<VideoItem> {
        return dao.getAll().map { entityToVideoItem(it) }
    }

    override suspend fun getDownloadedVideoById(videoId: String): VideoItem? {
        return dao.getById(videoId)?.let { entityToVideoItem(it) }
    }

    override suspend fun saveDownloadedVideo(video: VideoItem, localFilePath: String) {
        val entity = videoItemToEntity(video, localFilePath)
        dao.insert(entity)
    }

    override suspend fun deleteDownloadedVideo(videoId: String) {
        dao.deleteById(videoId)
    }

    override suspend fun isDownloaded(videoId: String): Boolean {
        return dao.getById(videoId) != null
    }

    override suspend fun getLocalFilePath(videoId: String): String? {
        return dao.getById(videoId)?.localFilePath
    }

    /**
     * EntityをVideoItemに変換
     * 注意: localFilePathはVideoItemには含めない（フェーズ4でオフライン再生時に使用）
     */
    private fun entityToVideoItem(entity: DownloadedVideoEntity): VideoItem {
        return VideoItem(
            id = entity.videoId,
            title = entity.title,
            description = entity.description,
            thumbnailUrl = entity.thumbnailUrl,
            videoUrl = entity.videoUrl, // 元のURLを保持（オフライン再生時はlocalFilePathを使用）
            duration = entity.duration?.toString(), // LongをStringに変換
            tag = entity.tag,
            category = entity.category,
        )
    }

    /**
     * VideoItemをEntityに変換
     */
    private fun videoItemToEntity(video: VideoItem, localFilePath: String): DownloadedVideoEntity {
        return DownloadedVideoEntity(
            videoId = video.id,
            title = video.title,
            description = video.description,
            videoUrl = video.videoUrl,
            thumbnailUrl = video.thumbnailUrl,
            localFilePath = localFilePath,
            downloadedAt = System.currentTimeMillis(),
            fileSize = 0L, // フェーズ3でダウンロード時に設定
            duration = video.duration?.toLongOrNull(), // StringをLongに変換（nullの場合はnull）
            tag = video.tag,
            category = video.category,
        )
    }
}

