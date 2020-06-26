package org.dhis2.utils.analytics

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.utils.analytics.matomo.MatomoAnalyticsController

@Module
@Singleton
class AnalyticsModule internal constructor() {

    @Provides
    @Singleton
    fun providesAnalyticsHelper(
        context: Context,
        preferencesProvider: PreferenceProvider,
        matomoAnalyticsController: MatomoAnalyticsController
    ): AnalyticsHelper {
        return AnalyticsHelper(
            FirebaseAnalytics.getInstance(context),
            preferencesProvider,
            matomoAnalyticsController
        )
    }

    @Provides
    fun providesAnalyticsInterceptor(analyticHelper: AnalyticsHelper): AnalyticsInterceptor {
        return AnalyticsInterceptor(analyticHelper)
    }
}
