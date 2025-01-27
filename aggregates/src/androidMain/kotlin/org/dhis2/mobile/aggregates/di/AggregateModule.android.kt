package org.dhis2.mobile.aggregates.di

import org.dhis2.mobile.aggregates.data.DataFetcher
import org.dhis2.mobile.aggregates.data.DataSetDataFetcher
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module
    get() = module {
        single<DataFetcher> { DataSetDataFetcher(get()) }
    }
