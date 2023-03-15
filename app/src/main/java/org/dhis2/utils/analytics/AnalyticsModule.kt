package org.dhis2.utils.analytics

import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import org.dhis2.commons.matomo.MatomoAnalyticsController

@Module
class AnalyticsModule internal constructor() {

    @Provides
    @Singleton
    fun providesAnalyticsHelper(
        matomoAnalyticsController: MatomoAnalyticsController
    ): AnalyticsHelper {
        return AnalyticsHelper(matomoAnalyticsController)
    }

    @Provides
    fun providesAnalyticsInterceptor(analyticHelper: AnalyticsHelper): AnalyticsInterceptor {
        return AnalyticsInterceptor(analyticHelper)
    }
}
