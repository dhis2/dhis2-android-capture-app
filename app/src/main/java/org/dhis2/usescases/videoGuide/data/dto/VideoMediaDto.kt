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
    @Json(name = "field_description")
    val fieldDescription: String? = null,
    @Json(name = "field_title")
    val fieldTitle: String? = null,
)

@JsonClass(generateAdapter = true)
data class VideoMediaRelationshipsDto(
    @Json(name = "field_media_video_file")
    val fieldMediaVideoFile: VideoFileRelationshipDto? = null,
    // サムネイル（直接ファイル参照）
    @Json(name = "thumbnail")
    val thumbnail: VideoRelationshipDto? = null,
    // サムネイル（メディア参照）
    @Json(name = "field_video_thumbnail")
    val fieldVideoThumbnail: VideoRelationshipDto? = null,
    // カテゴリ（タクソノミー用語への参照、単一）
    @Json(name = "field_video_category")
    val fieldVideoCategory: VideoRelationshipDto? = null,
    // タグ（タクソノミー用語への参照、単一）
    @Json(name = "field_video_tag")
    val fieldVideoTag: VideoRelationshipDto? = null,
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

// 単一のrelationship用（thumbnail、categoryなど）
@JsonClass(generateAdapter = true)
data class VideoRelationshipDto(
    val data: VideoReferenceDto? = null,
)

@JsonClass(generateAdapter = true)
data class VideoReferenceDto(
    val id: String,
    val type: String,
)

