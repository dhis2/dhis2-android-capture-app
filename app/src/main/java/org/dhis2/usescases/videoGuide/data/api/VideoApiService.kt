package org.dhis2.usescases.videoGuide.data.api

import org.dhis2.usescases.videoGuide.data.dto.VideoListResponseDto
import org.dhis2.usescases.videoGuide.data.dto.VideoResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface VideoApiService {
    @GET("jsonapi/media/video")
    suspend fun getVideos(
        @Query("include") include: String = "field_media_video_file,thumbnail,field_video_thumbnail.field_media_image,field_video_category,field_video_tag",
    ): VideoListResponseDto

    @GET("jsonapi/media/video/{id}")
    suspend fun getVideo(
        @Path("id") id: String,
        @Query("include") include: String = "field_media_video_file,thumbnail,field_video_thumbnail.field_media_image,field_video_category,field_video_tag",
    ): VideoResponseDto
}

