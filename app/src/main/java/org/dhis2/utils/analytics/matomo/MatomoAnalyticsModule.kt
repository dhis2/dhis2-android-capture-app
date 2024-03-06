package org.dhis2.utils.analytics.matomo

import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.matomo.sdk.Matomo
import org.matomo.sdk.extra.DownloadTracker

@Module
class MatomoAnalyticsModule {

    @Provides
    @Singleton
    fun providesMatomoAnalyticsController(
        matomo: Matomo,
        apkChecksum: DownloadTracker.Extra.ApkChecksum
    ): MatomoAnalyticsController {
        return MatomoAnalyticsControllerImpl(matomo, apkChecksum)
    }

    @Provides
    @Singleton
    fun provideMatomo(context: Context): Matomo {
        return Matomo.getInstance(context)
    }

    @Provides
    @Singleton
    fun apkCheckSum(context: Context): DownloadTracker.Extra.ApkChecksum {
        return DownloadTracker.Extra.ApkChecksum(context)
    }
}
