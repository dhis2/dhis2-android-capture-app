package org.dhis2.mobile.aggregates.di

import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository
import org.dhis2.mobile.aggregates.data.DataSetInstanceRepositoryImpl
import org.dhis2.mobile.aggregates.ui.UIActionHandler
import org.dhis2.mobile.aggregates.ui.UIActionHandlerImpl
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module
    get() = module {
        single<DataSetInstanceRepository> { DataSetInstanceRepositoryImpl(get()) }
        single<UIActionHandler> { params -> UIActionHandlerImpl(params.get()) }
    }
