package org.dhis2.mobile.aggregates.di

import org.dhis2.mobile.aggregates.domain.GetDataSetInstanceDetails
import org.dhis2.mobile.aggregates.ui.viewModel.DataSetTableViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

internal val featureModule = module {
    factory { params ->
        GetDataSetInstanceDetails(
            params.get(),
            params.get(),
            params.get(),
            params.get(),
            get(),
        )
    }

    viewModel { params ->
        DataSetTableViewModel(
            get { parametersOf(params.get(), params.get(), params.get(), params.get()) },
        )
    }
}

internal expect val platformModule: Module

val aggregatesModule = module {
    includes(featureModule, platformModule)
}
