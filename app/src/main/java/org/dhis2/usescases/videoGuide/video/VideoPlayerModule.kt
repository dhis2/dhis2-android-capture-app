@file:OptIn(androidx.media3.common.util.UnstableApi::class)

package org.dhis2.usescases.videoGuide.video

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import org.dhis2.BuildConfig
import org.dhis2.commons.di.dagger.PerActivity
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.videoGuide.VideoGuideRepository
import org.dhis2.usescases.videoGuide.data.api.VideoApiService
import org.dhis2.usescases.videoGuide.data.datasource.DrupalVideoApiDataSource
import org.dhis2.usescases.videoGuide.data.datasource.RoomVideoLocalDataSource
import org.dhis2.usescases.videoGuide.data.datasource.VideoLocalDataSource
import org.dhis2.usescases.videoGuide.data.datasource.VideoRemoteDataSource
import org.dhis2.usescases.videoGuide.data.local.DownloadedVideoDao
import org.dhis2.usescases.videoGuide.data.local.VideoDatabase
import org.dhis2.usescases.videoGuide.data.mapper.VideoMapper
import androidx.room.Room
import androidx.media3.datasource.cache.SimpleCache
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Module
class VideoPlayerModule(
    private val activity: ActivityGlobalAbstract,
    private val viewModelStoreOwner: ViewModelStoreOwner,
) {
    companion object {
        // Moshi AdapterをSingleton化（パフォーマンス向上）
        private val moshi: Moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    fun provideActivity(): ActivityGlobalAbstract = activity

    @Provides
    @PerActivity
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
    @PerActivity
    fun provideDrupalBaseUrl(): String {
        return BuildConfig.DRUPAL_BASE_URL
    }

    @Provides
    @PerActivity
    fun provideMapper(): VideoMapper {
        return VideoMapper()
    }

    @Provides
    @PerActivity
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .build()
    }

    @Provides
    @PerActivity
    fun provideDataSource(
        api: VideoApiService,
        mapper: VideoMapper,
        baseUrl: String,
    ): VideoRemoteDataSource {
        return DrupalVideoApiDataSource(api, mapper, baseUrl)
    }

    @Provides
    @PerActivity
    fun provideVideoDatabase(): VideoDatabase {
        return Room.databaseBuilder(
            activity.applicationContext,
            VideoDatabase::class.java,
            "video_database"
        ).build()
    }

    @Provides
    @PerActivity
    fun provideDownloadedVideoDao(database: VideoDatabase): DownloadedVideoDao {
        return database.downloadedVideoDao()
    }

    @Provides
    @PerActivity
    fun provideVideoLocalDataSource(dao: DownloadedVideoDao): VideoLocalDataSource {
        return RoomVideoLocalDataSource(dao)
    }

    @Provides
    @PerActivity
    fun provideRepository(
        dataSource: VideoRemoteDataSource,
        localDataSource: VideoLocalDataSource,
    ): VideoGuideRepository {
        return VideoGuideRepository(dataSource, localDataSource)
    }

    @Provides
    @PerActivity
    fun provideDownloadTracker(
        downloadManager: androidx.media3.exoplayer.offline.DownloadManager,
    ): DownloadTracker {
        return DownloadTracker(downloadManager)
    }

    @Provides
    @PerActivity
    fun provideSimpleCache(): SimpleCache {
        return VideoCacheManager.getOrCreateSimpleCache(activity)
    }

    @Provides
    @PerActivity
    fun provideDownloadManager(
        cache: SimpleCache,
    ): androidx.media3.exoplayer.offline.DownloadManager {
        val databaseProvider = androidx.media3.database.StandaloneDatabaseProvider(activity)
        val dataSourceFactory: androidx.media3.datasource.HttpDataSource.Factory =
            androidx.media3.datasource.DefaultHttpDataSource.Factory()
                .setUserAgent(androidx.media3.common.util.Util.getUserAgent(activity, "DHIS2-Android-Capture"))
                .setAllowCrossProtocolRedirects(true)

        val downloadManager = androidx.media3.exoplayer.offline.DownloadManager(
            activity,
            databaseProvider,
            cache,
            dataSourceFactory,
            java.util.concurrent.Executors.newSingleThreadExecutor()
        )

        // VideoDownloadServiceにDownloadManagerを設定（既に設定済みの可能性があるが、念のため）
        org.dhis2.usescases.videoGuide.video.VideoDownloadService.setDownloadManager(downloadManager)

        return downloadManager
    }

    @Provides
    @PerActivity
    fun provideVideoDownloadManager(
        downloadManager: androidx.media3.exoplayer.offline.DownloadManager,
        downloadTracker: DownloadTracker,
        localDataSource: VideoLocalDataSource,
    ): VideoDownloadManager {
        return VideoDownloadManager(activity, downloadManager, downloadTracker, localDataSource)
    }

    @Provides
    @PerActivity
    fun provideExoPlayerManager(
        cache: SimpleCache,
    ): ExoPlayerManager {
        return ExoPlayerManager(activity, cache)
    }

    @Provides
    @PerActivity
    fun provideViewModelFactory(
        repository: VideoGuideRepository,
        downloadManager: VideoDownloadManager,
    ): VideoPlayerViewModelFactory {
        return VideoPlayerViewModelFactory(repository, downloadManager, activity)
    }

    @Provides
    @PerActivity
    fun provideViewModel(
        factory: VideoPlayerViewModelFactory,
    ): VideoPlayerViewModel {
        return ViewModelProvider(viewModelStoreOwner, factory)[VideoPlayerViewModel::class.java]
    }
}

