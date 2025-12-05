package org.dhis2.usescases.videoGuide.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VideoMediaDto(
    val id: String,
    val type: String,
    val attributes: VideoMediaAttributesDto,
    val relationships: VideoMediaRelationshipsDto? = null,
)

@JsonClass(generateAdapter = true)
data class VideoMediaAttributesDto(
    val name: String,
    val description: String? = null,
)

@JsonClass(generateAdapter = true)
data class VideoMediaRelationshipsDto(
    @Json(name = "field_media_video_file")
    val fieldMediaVideoFile: VideoFileRelationshipDto? = null,
)

@JsonClass(generateAdapter = true)
data class VideoFileRelationshipDto(
    val data: VideoFileReferenceDto,
)

@JsonClass(generateAdapter = true)
data class VideoFileReferenceDto(
    val id: String,
    val type: String,
)

