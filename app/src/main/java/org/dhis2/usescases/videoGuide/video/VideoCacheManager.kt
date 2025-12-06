@file:OptIn(androidx.media3.common.util.UnstableApi::class)

package org.dhis2.usescases.videoGuide.video

import android.content.Context
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

/**
 * SimpleCacheのシングルトン管理クラス
 * 同じキャッシュディレクトリに対して複数のSimpleCacheインスタンスを作成することを防ぐ
 */
object VideoCacheManager {
    @Volatile
    private var simpleCacheInstance: SimpleCache? = null

    @Synchronized
    fun getOrCreateSimpleCache(context: Context): SimpleCache {
        if (simpleCacheInstance == null) {
            val databaseProvider = androidx.media3.database.StandaloneDatabaseProvider(context)
            simpleCacheInstance = SimpleCache(
                File(context.cacheDir, "video_downloads"),
                NoOpCacheEvictor(),
                databaseProvider
            )
        }
        return simpleCacheInstance!!
    }

    /**
     * SimpleCacheインスタンスをリリース（テスト用など）
     */
    @Synchronized
    fun releaseCache() {
        simpleCacheInstance?.release()
        simpleCacheInstance = null
    }
}

