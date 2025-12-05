package org.dhis2.usescases.videoGuide

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
import org.dhis2.usescases.videoGuide.data.datasource.VideoRemoteDataSource
import org.dhis2.usescases.videoGuide.data.mapper.VideoMapper
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
    fun provideRepository(
        dataSource: VideoRemoteDataSource,
    ): VideoGuideRepository {
        return VideoGuideRepository(dataSource)
    }

    @Provides
    @PerFragment
    fun providesViewModelFactory(
        repository: VideoGuideRepository,
    ): VideoGuideViewModelFactory {
        return VideoGuideViewModelFactory(repository)
    }
}

