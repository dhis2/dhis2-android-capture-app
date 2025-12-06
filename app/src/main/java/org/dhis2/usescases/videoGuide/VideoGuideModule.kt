@file:OptIn(androidx.media3.common.util.UnstableApi::class)

package org.dhis2.usescases.videoGuide

import android.content.Context
import androidx.room.Room
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import org.dhis2.BuildConfig
import org.dhis2.commons.di.dagger.PerFragment
import org.dhis2.usescases.videoGuide.data.api.VideoApiService
import org.dhis2.usescases.videoGuide.data.datasource.DummyVideoDataSource
import org.dhis2.usescases.videoGuide.data.datasource.DrupalVideoApiDataSource
import org.dhis2.usescases.videoGuide.data.datasource.RoomVideoLocalDataSource
import org.dhis2.usescases.videoGuide.data.datasource.VideoLocalDataSource
import org.dhis2.usescases.videoGuide.data.datasource.VideoRemoteDataSource
import org.dhis2.usescases.videoGuide.data.local.DownloadedVideoDao
import org.dhis2.usescases.videoGuide.data.local.VideoDatabase
import org.dhis2.usescases.videoGuide.data.mapper.VideoMapper
import org.dhis2.usescases.videoGuide.video.DownloadTracker
import org.dhis2.usescases.videoGuide.video.VideoDownloadManager
import androidx.media3.datasource.cache.SimpleCache
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Module
class VideoGuideModule {

    companion object {
        // Moshi AdapterをSingleton化（パフォーマンス向上）
        private val moshi: Moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @PerFragment
    fun provideVideoApiService(
        okHttpClient: OkHttpClient,
    ): VideoApiService {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.DRUPAL_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(VideoApiService::class.java)
    }

    @Provides
    @PerFragment
    fun provideDrupalBaseUrl(): String {
        return BuildConfig.DRUPAL_BASE_URL
    }

    @Provides
    @PerFragment
    fun provideMapper(): VideoMapper {
        return VideoMapper()
    }

    @Provides
    @PerFragment
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .build()
    }

    @Provides
    @PerFragment
    fun provideDataSource(
        api: VideoApiService,
        mapper: VideoMapper,
        baseUrl: String,
    ): VideoRemoteDataSource {
        // 開発時は以下に切り替え可能
        // return DummyVideoDataSource()
        return DrupalVideoApiDataSource(api, mapper, baseUrl)
    }

    @Provides
    @PerFragment
    fun provideVideoDatabase(context: Context): VideoDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            VideoDatabase::class.java,
            "video_database"
        ).build()
    }

    @Provides
    @PerFragment
    fun provideDownloadedVideoDao(database: VideoDatabase): DownloadedVideoDao {
        return database.downloadedVideoDao()
    }

    @Provides
    @PerFragment
    fun provideVideoLocalDataSource(dao: DownloadedVideoDao): VideoLocalDataSource {
        return RoomVideoLocalDataSource(dao)
    }

    @Provides
    @PerFragment
    fun provideRepository(
        dataSource: VideoRemoteDataSource,
        localDataSource: VideoLocalDataSource,
    ): VideoGuideRepository {
        return VideoGuideRepository(dataSource, localDataSource)
    }

    @Provides
    @PerFragment
    fun providesViewModelFactory(
        repository: VideoGuideRepository,
        downloadManager: VideoDownloadManager,
        context: Context,
    ): VideoGuideViewModelFactory {
        return VideoGuideViewModelFactory(repository, downloadManager, context)
    }

    @Provides
    @PerFragment
    fun provideSimpleCache(context: Context): SimpleCache {
        return org.dhis2.usescases.videoGuide.video.VideoCacheManager.getOrCreateSimpleCache(context)
    }

    @Provides
    @PerFragment
    fun provideDownloadManager(
        context: Context,
        cache: SimpleCache,
    ): androidx.media3.exoplayer.offline.DownloadManager {
        val databaseProvider = androidx.media3.database.StandaloneDatabaseProvider(context)
        val dataSourceFactory: androidx.media3.datasource.HttpDataSource.Factory =
            androidx.media3.datasource.DefaultHttpDataSource.Factory()
                .setUserAgent(androidx.media3.common.util.Util.getUserAgent(context, "DHIS2-Android-Capture"))
                .setAllowCrossProtocolRedirects(true)

        val downloadManager = androidx.media3.exoplayer.offline.DownloadManager(
            context,
            databaseProvider,
            cache,
            dataSourceFactory,
            java.util.concurrent.Executors.newSingleThreadExecutor()
        )

        // VideoDownloadServiceにDownloadManagerを設定
        org.dhis2.usescases.videoGuide.video.VideoDownloadService.setDownloadManager(downloadManager)

        return downloadManager
    }

    @Provides
    @PerFragment
    fun provideDownloadTracker(
        downloadManager: androidx.media3.exoplayer.offline.DownloadManager,
    ): DownloadTracker {
        return DownloadTracker(downloadManager)
    }

    @Provides
    @PerFragment
    fun provideVideoDownloadManager(
        context: Context,
        downloadManager: androidx.media3.exoplayer.offline.DownloadManager,
        downloadTracker: DownloadTracker,
        localDataSource: VideoLocalDataSource,
    ): VideoDownloadManager {
        return VideoDownloadManager(context, downloadManager, downloadTracker, localDataSource)
    }
}

