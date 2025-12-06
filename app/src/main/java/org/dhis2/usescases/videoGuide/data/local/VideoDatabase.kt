package org.dhis2.usescases.videoGuide.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DownloadedVideoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class VideoDatabase : RoomDatabase() {
    abstract fun downloadedVideoDao(): DownloadedVideoDao
}

