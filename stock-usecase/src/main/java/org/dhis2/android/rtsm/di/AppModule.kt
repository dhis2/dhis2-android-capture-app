package org.dhis2.android.rtsm.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.dhis2.android.rtsm.coroutines.StockDispatcherProvider
import org.dhis2.android.rtsm.services.SpeechRecognitionManager
import org.dhis2.android.rtsm.services.SpeechRecognitionManagerImpl
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.D2Manager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun providesD2(): D2 {
        return D2Manager.getD2()
    }

    @Provides
    @Singleton
    fun providesSpeechRecognitionManager(
        @ApplicationContext appContext: Context,
    ): SpeechRecognitionManager {
        return SpeechRecognitionManagerImpl(appContext)
    }

    @Provides
    @Singleton
    fun provideResourcesProvider(
        @ApplicationContext appContext: Context,
        colorUtils: ColorUtils,
    ): ResourceManager {
        return ResourceManager(appContext, colorUtils)
    }

    @Provides
    fun provideDispatcherProvider(): DispatcherProvider {
        return StockDispatcherProvider()
    }

    @Provides
    @Singleton
    fun provideColorUtilsProvider(): ColorUtils {
        return ColorUtils()
    }
}
