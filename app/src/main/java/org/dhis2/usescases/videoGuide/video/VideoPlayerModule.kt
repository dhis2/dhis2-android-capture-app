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
import org.dhis2.usescases.videoGuide.data.datasource.VideoRemoteDataSource
import org.dhis2.usescases.videoGuide.data.mapper.VideoMapper
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
    fun provideContext(): Context = activity

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
    fun provideRepository(
        dataSource: VideoRemoteDataSource,
    ): VideoGuideRepository {
        return VideoGuideRepository(dataSource)
    }

    @Provides
    @PerActivity
    fun provideViewModelFactory(
        repository: VideoGuideRepository,
    ): VideoPlayerViewModelFactory {
        return VideoPlayerViewModelFactory(repository)
    }

    @Provides
    @PerActivity
    fun provideViewModel(
        factory: VideoPlayerViewModelFactory,
    ): VideoPlayerViewModel {
        return ViewModelProvider(viewModelStoreOwner, factory)[VideoPlayerViewModel::class.java]
    }
}

