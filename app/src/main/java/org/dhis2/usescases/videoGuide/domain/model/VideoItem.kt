package org.dhis2.usescases.videoGuide.domain.model

data class VideoItem(
    val id: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String?,
    val videoUrl: String,
    val duration: String? = null,
    val tag: String? = null,        // タグ（単一）
    val category: String? = null,   // カテゴリ
)

