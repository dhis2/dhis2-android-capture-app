package org.dhis2.tracker.search.di

import org.dhis2.tracker.search.data.OptionSetRepository
import org.dhis2.tracker.search.data.OptionSetRepositoryImpl
import org.dhis2.tracker.search.data.SearchParametersRepository
import org.dhis2.tracker.search.data.SearchParametersRepositoryImpl
import org.dhis2.tracker.search.data.SearchTrackedEntityRepository
import org.dhis2.tracker.search.data.SearchTrackedEntityRepositoryImpl
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android-specific implementation of the tracker search repository module.
 */
internal actual val trackerSearchRepositoryModule: Module =
    module {
        // SearchTrackedEntityRepository - factory implementation
        factory<SearchTrackedEntityRepository> {
            SearchTrackedEntityRepositoryImpl(
                d2 = get(),
                filterPresenter = get(),
                profilePictureProvider = get(),
            )
        }

        // SearchParametersRepository - interface implementation
        single<SearchParametersRepository> {
            SearchParametersRepositoryImpl(
                d2 = get(),
                customIntentRepository = get(),
                domainErrorMapper = get(),
            )
        }

        // OptionSetRepository - interface implementation
        single<OptionSetRepository> {
            OptionSetRepositoryImpl(
                d2 = get(),
                domainErrorMapper = get(),
            )
        }
    }
