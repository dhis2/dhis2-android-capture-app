package org.dhis2.usescases.videoGuide.data.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VideoListResponseDto(
    val data: List<VideoMediaDto>,
    val included: List<VideoFileDto>? = null,
)

