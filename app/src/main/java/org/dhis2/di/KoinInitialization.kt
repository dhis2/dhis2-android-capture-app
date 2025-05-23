package org.dhis2.di

import android.app.Application
import org.dhis2.commons.di.resourceManagerModule
import org.dhis2.commons.filters.periods.di.filterPeriodsModule
import org.dhis2.mobile.aggregates.di.aggregatesModule
import org.dhis2.mobile.commons.di.commonsModule
import org.dhis2.usescases.datasets.di.dataSetModules
import org.hisp.dhis.android.core.D2Configuration
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

object KoinInitialization {
    operator fun Application.invoke(d2Configuration: D2Configuration) {
        startKoin {
            androidLogger()
            androidContext(this@invoke)
            modules(
                serverModule(d2Configuration),
                commonsModule,
                aggregatesModule,
                filterPeriodsModule,
                resourceManagerModule,
                dataSetModules,
            )
        }
    }
}
