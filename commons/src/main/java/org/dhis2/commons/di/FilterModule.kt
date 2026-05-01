package org.dhis2.commons.di

import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.FilterResources
import org.dhis2.commons.filters.data.FilterPresenter
import org.dhis2.commons.filters.data.FilterRepository
import org.dhis2.commons.filters.data.GetFiltersApplyingWebAppConfig
import org.dhis2.commons.filters.workingLists.EventFilterToWorkingListItemMapper
import org.dhis2.commons.filters.workingLists.ProgramStageToWorkingListItemMapper
import org.dhis2.commons.filters.workingLists.TeiFilterToWorkingListItemMapper
import org.dhis2.commons.resources.EventResourcesProvider
import org.dhis2.commons.resources.ResourceManager
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val mainFilterModule =
    module {
        // Singleton for GetFiltersApplyingWebAppConfig
        single {
            GetFiltersApplyingWebAppConfig()
        }

        // FilterManager - singleton per server
        single {
            FilterManager.initWith(get<ResourceManager>())
        }

        // FilterResources
        factory {
            FilterResources(
                resourceManager = get(),
                eventResourcesProvider = get(),
            )
        }

        // EventResourcesProvider
        factory {
            EventResourcesProvider(
                d2 = get(),
                resourceManager = get(),
            )
        }

        // Working list mappers
        factory {
            EventFilterToWorkingListItemMapper(
                defaultWorkingListLabel = get<FilterResources>().defaultWorkingListLabel(),
            )
        }

        factory {
            TeiFilterToWorkingListItemMapper(
                defaultWorkingListLabel = get<FilterResources>().defaultWorkingListLabel(),
            )
        }

        factory {
            ProgramStageToWorkingListItemMapper(
                defaultWorkingListLabel = get<FilterResources>().defaultWorkingListLabel(),
            )
        }

        // FilterRepository
        factory {
            FilterRepository(
                d2 = get(),
                resources = get(),
                getFiltersApplyingWebAppConfig = get(),
                eventFilterToWorkingListItemMapper = get(),
                teiFilterToWorkingListItemMapper = get(),
                programStageToWorkingListItemMapper = get(),
            )
        }

        // FilterPresenter
        factoryOf(::FilterPresenter)
    }

val filterModule =
    module {
        includes(mainFilterModule, resourceManagerModule)
    }
