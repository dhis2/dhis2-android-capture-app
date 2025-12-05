package org.dhis2.usescases.videoGuide.data.datasource

import org.dhis2.usescases.videoGuide.domain.model.VideoItem
import javax.inject.Inject

class DummyVideoDataSource @Inject constructor() : VideoRemoteDataSource {
    private val dummy = listOf(
        VideoItem(
            id = "1",
            title = "サンプル動画",
            description = "デモ用動画",
            thumbnailUrl = null,
            videoUrl = "https://example.com/sample.mp4",
        ),
    )

    override suspend fun getVideoList() = dummy

    override suspend fun getVideoById(videoId: String) = dummy.find { it.id == videoId }
}

