package org.dhis2.usescases.videoGuide.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DownloadedVideoDao {
    @Query("SELECT * FROM downloaded_videos")
    suspend fun getAll(): List<DownloadedVideoEntity>

    @Query("SELECT * FROM downloaded_videos WHERE videoId = :videoId")
    suspend fun getById(videoId: String): DownloadedVideoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(video: DownloadedVideoEntity)

    @Delete
    suspend fun delete(video: DownloadedVideoEntity)

    @Query("DELETE FROM downloaded_videos WHERE videoId = :videoId")
    suspend fun deleteById(videoId: String)
}

