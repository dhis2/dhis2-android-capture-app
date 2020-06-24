package org.dhis2.utils.analytics.matomo

import android.content.Context
import dagger.Module
import dagger.Provides
import org.dhis2.App
import javax.inject.Singleton

@Module
class MatomoAnalyticsModule {

    @Provides
    @Singleton
    fun providesMatomoAnalyticsController(
        context: Context
    ): MatomoAnalyticsController {
        val tracker = (context.applicationContext as App).tracker
        return MatomoAnalyticsControllerImpl(tracker)
    }
}