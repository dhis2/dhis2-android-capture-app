package org.dhis2.mobile.aggregates.di

import org.dhis2.mobile.aggregates.ui.viewModel.DataSetTableViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val featureModule = module {
    viewModelOf(::DataSetTableViewModel)
}

internal expect val platformModule: Module

val aggregatesModule = module {
    includes(featureModule, platformModule)
}
