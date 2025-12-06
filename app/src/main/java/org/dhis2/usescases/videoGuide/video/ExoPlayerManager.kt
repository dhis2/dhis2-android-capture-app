package org.dhis2.usescases.videoGuide.video

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.util.Util
import androidx.media3.datasource.DefaultDataSourceFactory
import androidx.media3.datasource.HttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import timber.log.Timber

/**
 * ExoPlayerインスタンスの管理とMedia3統合によるオフライン再生を行うマネージャー
 * Media3では、SimpleCacheを使用してダウンロード済みファイルを自動的に読み込む
 */
class ExoPlayerManager(
    private val context: Context,
    private val cache: SimpleCache,
) {

    private var exoPlayer: ExoPlayer? = null
    private val httpDataSourceFactory: HttpDataSource.Factory

    init {
        // HTTPデータソースファクトリ
        httpDataSourceFactory = androidx.media3.datasource.DefaultHttpDataSource.Factory()
            .setUserAgent(Util.getUserAgent(context, "DHIS2-Android-Capture"))
            .setAllowCrossProtocolRedirects(true)
    }

    /**
     * ExoPlayerインスタンスを初期化
     * Media3のCacheDataSourceFactoryを使用して、SimpleCacheから自動的に読み込む
     */
    fun initializePlayer(): ExoPlayer {
        if (exoPlayer == null) {
            // CacheDataSourceFactoryを作成（SimpleCacheを使用）
            val cacheDataSourceFactory = CacheDataSource.Factory()
                .setCache(cache)
                .setUpstreamDataSourceFactory(httpDataSourceFactory)
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

            // DefaultDataSourceFactoryでCacheDataSourceFactoryをラップ
            val dataSourceFactory = DefaultDataSourceFactory(
                context,
                cacheDataSourceFactory
            )

            exoPlayer = ExoPlayer.Builder(context)
                .setMediaSourceFactory(
                    DefaultMediaSourceFactory(dataSourceFactory)
                )
                .build()
        }
        return exoPlayer!!
    }

    /**
     * メディアアイテムを準備
     * Media3では、常に元のURLを使用し、SimpleCacheが自動的にキャッシュから読み込む
     * @param videoUrl 動画のURL（常に元のURLを使用）
     */
    fun prepareMediaItem(videoUrl: String) {
        val player = exoPlayer ?: initializePlayer()

        // Media3では、常に元のURLを使用
        // SimpleCacheが自動的にキャッシュから読み込む
        Timber.d("Preparing media item from URL: $videoUrl (Media3 will automatically use cache if available)")
        val mediaItem = MediaItem.fromUri(videoUrl)
        player.setMediaItem(mediaItem)
        player.prepare()
    }

    /**
     * ExoPlayerインスタンスを取得
     */
    fun getPlayer(): ExoPlayer? {
        return exoPlayer
    }

    /**
     * ExoPlayerインスタンスをリリース
     */
    fun releasePlayer() {
        exoPlayer?.let { player ->
            player.release()
            exoPlayer = null
            Timber.d("ExoPlayer released")
        }
    }
}

