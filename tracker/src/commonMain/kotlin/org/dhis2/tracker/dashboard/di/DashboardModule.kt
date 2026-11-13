package org.dhis2.tracker.dashboard.di

import org.dhis2.tracker.dashboard.domain.GetEnrollmentAOC
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

internal expect val dashboardRepositoryModule: Module

val dashboardModule =
    module {
        includes(dashboardRepositoryModule)
        factoryOf(::GetEnrollmentAOC)
    }
