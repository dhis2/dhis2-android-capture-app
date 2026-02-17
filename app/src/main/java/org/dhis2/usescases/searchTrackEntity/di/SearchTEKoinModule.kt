package org.dhis2.usescases.searchTrackEntity.di

import org.dhis2.usescases.searchTrackEntity.SearchRepository
import org.dhis2.usescases.searchTrackEntity.SearchTEIViewModel
import org.dhis2.usescases.searchTrackEntity.SearchTeiViewModelFactory
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
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
            val searchRepository: SearchRepository = get()

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
                searchTrackedEntities = get { parametersOf(teType) },
                fetchSearchParameters = get(),
                fetchOptionSetOptions = get(),
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
            val searchRepository: SearchRepository = get()

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
                searchTrackedEntities = get { parametersOf(teType) },
                fetchSearchParameters = get(),
                fetchOptionSetOptions = get(),
            )
        }
    }
