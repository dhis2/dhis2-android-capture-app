package org.dhis2.usescases.videoGuide.data.datasource

import org.dhis2.usescases.videoGuide.data.api.VideoApiService
import org.dhis2.usescases.videoGuide.data.mapper.VideoMapper
import org.dhis2.usescases.videoGuide.domain.model.VideoItem
import timber.log.Timber
import javax.inject.Inject

class DrupalVideoApiDataSource @Inject constructor(
    private val api: VideoApiService,
    private val mapper: VideoMapper,
    private val baseUrl: String,
) : VideoRemoteDataSource {

    override suspend fun getVideoList(): List<VideoItem> {
        return runCatching {
            val response = api.getVideos()
            val filesMap = mapper.createFilesMap(response.included)

            response.data.mapNotNull { media ->
                mapper.mapToDomain(media, filesMap, baseUrl)
            }
        }.onFailure { throwable ->
            Timber.e(throwable, "Failed to fetch videos from Drupal API")
        }.getOrElse { emptyList() }
    }

    override suspend fun getVideoById(videoId: String): VideoItem? {
        return runCatching {
            val response = api.getVideo(videoId)
            val filesMap = mapper.createFilesMap(response.included)

            response.data.firstOrNull()?.let { media ->
                mapper.mapToDomain(media, filesMap, baseUrl)
            }
        }.onFailure { throwable ->
            Timber.e(throwable, "Failed to fetch video by id: $videoId")
        }.getOrNull()
    }
}

