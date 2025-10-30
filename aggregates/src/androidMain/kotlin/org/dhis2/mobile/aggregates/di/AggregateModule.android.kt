package org.dhis2.mobile.aggregates.di

import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository
import org.dhis2.mobile.aggregates.data.DataSetInstanceRepositoryImpl
import org.dhis2.mobile.aggregates.data.OptionRepository
import org.dhis2.mobile.aggregates.data.OptionRepositoryImpl
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module
    get() =
        module {
            single<DataSetInstanceRepository> { DataSetInstanceRepositoryImpl(get(), get(), get()) }
            single<OptionRepository> { OptionRepositoryImpl(get()) }
        }
