package org.dhis2.usescases.videoGuide.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_videos")
data class DownloadedVideoEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val description: String,
    val videoUrl: String,
    val thumbnailUrl: String?,
    val localFilePath: String, // ダウンロード先のローカルパス
    val downloadedAt: Long, // ダウンロード日時（ミリ秒）
    val fileSize: Long, // ファイルサイズ（バイト）
    val duration: Long? = null, // 動画の長さ（ミリ秒）
    val tag: String? = null, // タグ
    val category: String? = null, // カテゴリ
)

