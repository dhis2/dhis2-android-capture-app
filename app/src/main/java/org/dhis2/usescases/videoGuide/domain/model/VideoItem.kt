package org.dhis2.usescases.videoGuide.domain.model

data class VideoItem(
    val id: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String?,
    val videoUrl: String,
    val duration: String? = null,
)

