package org.dhis2.usescases.searchTrackEntity.di

import org.dhis2.tracker.search.domain.SearchTrackedEntities
import org.dhis2.usescases.searchTrackEntity.SearchTEIViewModel
import org.dhis2.usescases.searchTrackEntity.SearchTeiViewModelFactory
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for Search Tracked Entity feature.
 * This module provides dependencies for SearchTEIViewModel and related components.
 *
 * Note: This module coexists with the legacy Dagger SearchTEModule during the migration.
 */
val searchTEKoinModule =
    module {
        // SearchTeiViewModelFactory
        // Factory for creating SearchTEIViewModel instances with SearchTrackedEntities
        factory { params ->
            val initialProgramUid: String? = params.getOrNull()
            val initialQuery: MutableMap<String, List<String>?>? = params.getOrNull()
            val teType: String = params.get() // teType is required

            // Get SearchRepository from Dagger to access teType
            val searchRepository: org.dhis2.usescases.searchTrackEntity.SearchRepository = get()

            // Create SearchTrackedEntities with teType
            val searchTrackedEntities =
                SearchTrackedEntities(
                    repository = get(),
                    customIntentRepository = get(),
                    teType = teType,
                )

            SearchTeiViewModelFactory(
                searchRepository = searchRepository,
                searchRepositoryKt = get(),
                searchNavPageConfigurator = get(),
                initialProgramUid = initialProgramUid,
                initialQuery = initialQuery,
                mapDataRepository = get(),
                networkUtils = get(),
                dispatchers = get(),
                mapStyleConfig = get(),
                resourceManager = get(),
                displayNameProvider = get(),
                filterManager = get(),
                searchTrackedEntities = searchTrackedEntities,
            )
        }

        // SearchTEIViewModel
        // Note: This ViewModel requires parameters (initialProgramUid, initialQuery, teType)
        // Use: viewModel { parametersOf(programUid, queryMap, teType) }
        viewModel { params ->
            val initialProgramUid: String? = params.getOrNull()
            val initialQuery: MutableMap<String, List<String>?>? = params.getOrNull()
            val teType: String = params.get() // teType is required

            // Get SearchRepository from Dagger
            val searchRepository: org.dhis2.usescases.searchTrackEntity.SearchRepository = get()

            // Create SearchTrackedEntities with teType
            val searchTrackedEntities =
                SearchTrackedEntities(
                    repository = get(),
                    customIntentRepository = get(),
                    teType = teType,
                )

            SearchTEIViewModel(
                initialProgramUid = initialProgramUid,
                initialQuery = initialQuery,
                searchRepository = searchRepository,
                searchRepositoryKt = get(),
                searchNavPageConfigurator = get(),
                mapDataRepository = get(),
                networkUtils = get(),
                dispatchers = get(),
                mapStyleConfig = get(),
                resourceManager = get(),
                displayNameProvider = get(),
                filterManager = get(),
                searchTrackedEntities = searchTrackedEntities,
            )
        }
    }
