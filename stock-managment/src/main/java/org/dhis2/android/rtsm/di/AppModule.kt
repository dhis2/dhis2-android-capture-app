package org.dhis2.android.rtsm.di

import android.content.Context
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.apache.commons.jexl2.JexlEngine
import org.dhis2.android.rtsm.data.AppConfig
import org.dhis2.android.rtsm.services.preferences.PreferenceProvider
import org.dhis2.android.rtsm.services.preferences.PreferenceProviderImpl
import org.dhis2.android.rtsm.services.rules.ExpressionEvaluatorImpl
import org.dhis2.android.rtsm.services.WorkManagerController
import org.dhis2.android.rtsm.services.WorkManagerControllerImpl
import org.dhis2.android.rtsm.services.SyncManager
import org.dhis2.android.rtsm.services.SyncManagerImpl
import org.dhis2.android.rtsm.services.SpeechRecognitionManager
import org.dhis2.android.rtsm.services.SpeechRecognitionManagerImpl
import org.dhis2.android.rtsm.services.OpenIdProvider
import org.dhis2.android.rtsm.services.OpenIdProviderImpl
import org.dhis2.android.rtsm.utils.ConfigUtils
import org.dhis2.android.rtsm.utils.Sdk
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.rules.RuleExpressionEvaluator
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    @Singleton
    fun providesAppConfig(@ApplicationContext appContext: Context): AppConfig {
        return ConfigUtils.getAppConfig(appContext.resources)
    }

    @Provides
    @Singleton
    fun providesD2(@ApplicationContext appContext: Context): D2 {
        return Sdk.d2(appContext)
    }

    @Provides
    @Singleton
    fun providesPreferenceProvider(@ApplicationContext appContext: Context): PreferenceProvider {
        return PreferenceProviderImpl(appContext)
    }

    @Provides
    @Singleton
    fun providesWorkManager(@ApplicationContext appContext: Context): WorkManager {
        return WorkManager.getInstance(appContext)
    }

    @Provides
    @Singleton
    fun providesWorkManagerController(workManager: WorkManager): WorkManagerController {
        return WorkManagerControllerImpl(workManager)
    }

    @Provides
    @Singleton
    fun providesSyncManager(
        d2: D2,
        preferenceProvider: PreferenceProvider,
        workManagerController: WorkManagerController
    ): SyncManager {
        return SyncManagerImpl(d2, preferenceProvider, workManagerController)
    }

    @Provides
    @Singleton
    fun providesJexlEngine(): JexlEngine {
        return JexlEngine()
    }

    @Provides
    @Singleton
    fun providesRuleExpressionEvaluator(jexlEngine: JexlEngine): RuleExpressionEvaluator {
        return ExpressionEvaluatorImpl(jexlEngine)
    }

    @Provides
    @Singleton
    fun providesSpeechRecognitionManager(@ApplicationContext appContext: Context):
            SpeechRecognitionManager {
        return SpeechRecognitionManagerImpl(appContext)
    }

    @Provides
    @Singleton
    fun provideOpenIdProvider(@ApplicationContext appContext: Context): OpenIdProvider {
        return OpenIdProviderImpl(appContext.applicationContext)
    }
}