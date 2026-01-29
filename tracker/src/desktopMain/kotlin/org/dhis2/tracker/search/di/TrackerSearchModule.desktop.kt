package org.dhis2.tracker.search.di

import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Desktop-specific implementation of the tracker search repository module.
 * Currently empty as desktop support is not yet implemented for search.
 */
internal actual val trackerSearchRepositoryModule: Module =
    module {
        // Desktop implementation placeholder
    }
