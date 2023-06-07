package org.dhis2.android.rtsm.di

import android.content.Context
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import org.dhis2.android.rtsm.coroutines.StockDispatcherProvider
import org.dhis2.android.rtsm.services.SpeechRecognitionManager
import org.dhis2.android.rtsm.services.SpeechRecognitionManagerImpl
import org.dhis2.android.rtsm.utils.Sdk
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.hisp.dhis.android.core.D2

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun providesD2(@ApplicationContext appContext: Context): D2 {
        return Sdk.d2(appContext)
    }

    @Provides
    @Singleton
    fun providesWorkManager(@ApplicationContext appContext: Context): WorkManager {
        return WorkManager.getInstance(appContext)
    }

    @Provides
    @Singleton
    fun providesSpeechRecognitionManager(
        @ApplicationContext appContext: Context
    ): SpeechRecognitionManager {
        return SpeechRecognitionManagerImpl(appContext)
    }

    @Provides
    @Singleton
    fun provideResourcesProvider(@ApplicationContext appContext: Context): ResourceManager {
        return ResourceManager(appContext)
    }

    @Provides
    fun provideDispatcherProvider(): DispatcherProvider {
        return StockDispatcherProvider()
    }
}
