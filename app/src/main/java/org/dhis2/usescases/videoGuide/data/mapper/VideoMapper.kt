package org.dhis2.usescases.videoGuide.data.mapper

import org.dhis2.usescases.videoGuide.data.dto.VideoFileDto
import org.dhis2.usescases.videoGuide.data.dto.VideoMediaDto
import org.dhis2.usescases.videoGuide.domain.model.VideoItem
import javax.inject.Inject

class VideoMapper @Inject constructor() {

    fun createFilesMap(included: List<VideoFileDto>?): Map<String, String> {
        val files = included ?: emptyList()
        return files
            .filter { it.type == "file--file" }
            .associate { file ->
                file.id to file.attributes.uri.url
            }
    }

    fun mapToDomain(
        media: VideoMediaDto,
        filesMap: Map<String, String>,
        baseUrl: String,
    ): VideoItem? {
        // relationshipsがnullの場合はスキップ
        val fileId = media.relationships?.fieldMediaVideoFile?.data?.id ?: return null

        val relativePath = filesMap[fileId] ?: return null

        val videoUrl = if (relativePath.startsWith("http")) {
            relativePath
        } else {
            "$baseUrl$relativePath"
        }

        return VideoItem(
            id = media.id,
            title = media.attributes.name,
            description = media.attributes.description ?: "",
            thumbnailUrl = null,
            videoUrl = videoUrl,
            duration = null,
        )
    }
}

