package org.dhis2.mobile.login.authentication.di

import kotlinx.coroutines.Dispatchers
import org.dhis2.mobile.login.authentication.domain.usecase.DisableTwoFA
import org.dhis2.mobile.login.authentication.domain.usecase.EnableTwoFA
import org.dhis2.mobile.login.authentication.domain.usecase.GetTwoFAStatus
import org.dhis2.mobile.login.authentication.ui.mapper.TwoFAUiStateMapper
import org.dhis2.mobile.login.authentication.ui.viewmodel.TwoFASettingsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal expect val twoFARepositoryModule: Module

internal val twoFAModule =
    module {
        // Repository
        includes(twoFARepositoryModule)

        // Use Cases
        single<GetTwoFAStatus> { GetTwoFAStatus(get()) }
        single<EnableTwoFA> { EnableTwoFA(get()) }
        single<DisableTwoFA> { DisableTwoFA(get()) }

        // Mappers
        single<TwoFAUiStateMapper> { TwoFAUiStateMapper() }

    // ViewModels
    viewModel {
        TwoFASettingsViewModel(
            getTwoFAStatus = get(),
            enableTwoFA = get(),
            disableTwoFA = get(),
            mapper = get(),
            dispatchers = get(),
        )
    }
}
