package org.dhis2.mobile.login.authentication.di

import org.dhis2.mobile.login.authentication.data.repository.TwoFARepositoryImpl
import org.dhis2.mobile.login.authentication.domain.repository.TwoFARepository
import org.dhis2.mobile.login.authentication.domain.usecase.GetTwoFAStatus
import org.dhis2.mobile.login.authentication.ui.mapper.TwoFAUiStateMapper
import org.dhis2.mobile.login.authentication.ui.viewmodel.TwoFASettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal val twoFAModule = module {
    // Repository
    single<TwoFARepository> { TwoFARepositoryImpl() }

    // Use Cases
    single<GetTwoFAStatus> { GetTwoFAStatus(get()) }

    // Mappers
    single<TwoFAUiStateMapper> { TwoFAUiStateMapper() }

    // ViewModels
    viewModel {
        TwoFASettingsViewModel(
            getTwoFAStatus = get(),
            mapper = get(),
        )
    }
}
