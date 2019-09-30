package org.dhis2.utils.analytics

import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
@Singleton
class AnalyticsModule internal constructor() {

    @Provides
    fun providesAnalyticsHelper(context: Context): AnalyticsHelper {
        return AnalyticsHelper(context)
    }
}