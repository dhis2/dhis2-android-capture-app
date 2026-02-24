package org.dhis2.tracker.search.di

import org.dhis2.tracker.search.domain.FetchOptionSetOptions
import org.dhis2.tracker.search.domain.SearchTrackedEntities
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Platform-specific repository module.
 * Implemented in androidMain and other platform-specific source sets.
 */
internal expect val trackerSearchRepositoryModule: Module

/**
 * Common tracker search module containing use cases and common dependencies.
 */
val trackerSearchModule =
    module {
        // Include platform-specific repository implementations
        includes(trackerSearchRepositoryModule)

        // SearchTrackedEntities - factory with teType parameter
        factory { params ->
            SearchTrackedEntities(
                repository = get(),
                customIntentRepository = get(),
                teType = params.get(),
            )
        }
        // FetchOptionSetOptions use case
        factoryOf(::FetchOptionSetOptions)
    }
