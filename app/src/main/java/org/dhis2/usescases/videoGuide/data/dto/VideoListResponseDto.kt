package org.dhis2.usescases.videoGuide.data.dto

import com.squareup.moshi.JsonClass

// included配列には異なるタイプのエンティティが含まれる可能性があるため、
// 汎用的なDTOとして定義
@JsonClass(generateAdapter = true)
data class VideoListResponseDto(
    val data: List<VideoMediaDto>,
    val included: List<VideoIncludedDto>? = null,
)

// included配列の各要素を表す汎用DTO
// 実際の型はtypeフィールドで判別
@JsonClass(generateAdapter = true)
data class VideoIncludedDto(
    val id: String,
    val type: String,
    val attributes: Map<String, Any>? = null,
    val relationships: Map<String, Any>? = null,
)

