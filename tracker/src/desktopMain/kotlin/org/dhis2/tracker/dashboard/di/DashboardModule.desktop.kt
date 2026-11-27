package org.dhis2.tracker.dashboard.di

import org.dhis2.tracker.dashboard.data.DashboardRepository
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val dashboardRepositoryModule: Module
    get() =
        module {
            factory {
                object : DashboardRepository {
                    override suspend fun enrollmentCategoryComboLabel(programUid: String): String? = null

                    override suspend fun enrollmentCategoryOptionComboLabel(enrollmentAttributeOptionCombo: String): String = ""
                }
            }
        }
