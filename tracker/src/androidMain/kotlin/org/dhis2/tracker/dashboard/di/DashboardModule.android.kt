package org.dhis2.tracker.dashboard.di

import org.dhis2.tracker.dashboard.data.DashboardRepository
import org.dhis2.tracker.dashboard.data.DashboardRepositoryImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val dashboardRepositoryModule: Module
    get() =
        module {
            factoryOf(::DashboardRepositoryImpl) bind DashboardRepository::class
        }
