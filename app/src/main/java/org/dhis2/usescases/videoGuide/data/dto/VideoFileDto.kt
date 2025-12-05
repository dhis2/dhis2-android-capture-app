package org.dhis2.usescases.videoGuide.data.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VideoFileDto(
    val id: String,
    val type: String,
    val attributes: VideoFileAttributesDto,
)

@JsonClass(generateAdapter = true)
data class VideoFileAttributesDto(
    val uri: VideoFileUriDto,
)

@JsonClass(generateAdapter = true)
data class VideoFileUriDto(
    val url: String,
)

