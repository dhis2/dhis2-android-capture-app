package org.dhis2.di

import android.app.Application
import dhis2.org.analytics.charts.di.chartsModule
import org.dhis2.BuildConfig
import org.dhis2.android.rtsm.di.stockModule
import org.dhis2.appModule
import org.dhis2.commons.di.filterModule
import org.dhis2.commons.di.resourceManagerModule
import org.dhis2.commons.filters.periods.di.filterPeriodsModule
import org.dhis2.data.biometric.biometricModule
import org.dhis2.mobile.aggregates.di.aggregatesModule
import org.dhis2.mobile.commons.di.commonsModule
import org.dhis2.mobile.login.main.di.loginModule
import org.dhis2.mobile.sync.di.syncModule
import org.dhis2.tracker.search.di.trackerSearchModule
import org.dhis2.usescases.datasets.di.dataSetModules
import org.dhis2.usescases.searchTrackEntity.di.searchTEKoinModule
import org.dhis2.usescases.settingsprogram.di.settingsProgramModule
import org.dhis2.utils.analytics.matomo.matomoModule
import org.hisp.dhis.android.core.D2Configuration
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin

object KoinInitialization {
    operator fun Application.invoke(d2Configuration: D2Configuration) {
        startKoin {
            androidLogger()
            androidContext(this@invoke)
            properties(
                mapOf(
                    "sentryDsn" to BuildConfig.SENTRY_DSN,
                    "isTrainingFlavor" to (BuildConfig.FLAVOR == "dhis2Training"),
                    "openIdType" to BuildConfig.OPEN_ID_TYPE,
                    "openIdServer" to BuildConfig.OPEN_ID_SERVER,
                    "openIdClient" to BuildConfig.OPEN_ID_CLIENT,
                    "openIdRedirectUri" to BuildConfig.OPEN_ID_REDIRECT_URI,
                    "openIdDiscoveryUri" to BuildConfig.OPEN_ID_DISCOVERY_URI,
                    "openIdAuthorizationUrl" to BuildConfig.OPEN_ID_AUTHORIZATION_URL,
                    "openIdTokenUrl" to BuildConfig.OPEN_ID_TOKEN_URL,
                    "openIdButtonText" to BuildConfig.OPEN_ID_BUTTON_TEXT,
                ),
            )
            workManagerFactory()
            modules(
                appModule,
                serverModule(d2Configuration),
                commonsModule,
                aggregatesModule,
                filterModule,
                filterPeriodsModule,
                resourceManagerModule,
                dataSetModules,
                stockModule,
                loginModule,
                trackerSearchModule,
                searchTEKoinModule,
                settingsProgramModule,
                biometricModule,
                matomoModule,
                syncModule,
                chartsModule,
            )
        }
    }
}
