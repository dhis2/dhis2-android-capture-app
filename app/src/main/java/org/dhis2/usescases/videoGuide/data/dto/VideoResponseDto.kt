package org.dhis2.usescases.videoGuide.data.dto

import com.squareup.moshi.JsonClass

// 個別取得用のDTO（dataが単一オブジェクト）
@JsonClass(generateAdapter = true)
data class VideoResponseDto(
    val data: VideoMediaDto,
    val included: List<VideoIncludedDto>? = null,
)

