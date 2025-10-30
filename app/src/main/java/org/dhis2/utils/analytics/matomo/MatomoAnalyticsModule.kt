package org.dhis2.utils.analytics.matomo

import android.content.Context
import dagger.Module
import dagger.Provides
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.mobile.commons.reporting.AnalyticActions
import org.dhis2.utils.analytics.AnalyticsHelper
import org.koin.dsl.module
import org.matomo.sdk.Matomo
import org.matomo.sdk.extra.DownloadTracker
import javax.inject.Singleton

@Module
class MatomoAnalyticsModule {
    @Provides
    @Singleton
    fun providesMatomoAnalyticsController(
        matomo: Matomo,
        apkChecksum: DownloadTracker.Extra.ApkChecksum,
    ): MatomoAnalyticsController = MatomoAnalyticsControllerImpl(matomo, apkChecksum)

    @Provides
    @Singleton
    fun provideMatomo(context: Context): Matomo = Matomo.getInstance(context)

    @Provides
    @Singleton
    fun apkCheckSum(context: Context): DownloadTracker.Extra.ApkChecksum = DownloadTracker.Extra.ApkChecksum(context)
}

val matomoModule =
    module {
        single { DownloadTracker.Extra.ApkChecksum(get()) }
        single { Matomo.getInstance(get()) }
        single<MatomoAnalyticsController> { MatomoAnalyticsControllerImpl(get(), get()) }
        single<AnalyticActions> { AnalyticsHelper(get()) }
    }
