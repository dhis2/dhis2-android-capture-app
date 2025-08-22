package org.dhis2.mobile.login.main.di

import org.dhis2.mobile.login.accounts.data.repository.AccountRepository
import org.dhis2.mobile.login.accounts.data.repository.AccountRepositoryImpl
import org.dhis2.mobile.login.accounts.ui.viewmodel.AccountsViewModel
import org.dhis2.mobile.login.authentication.data.repository.TwoFARepositoryImpl
import org.dhis2.mobile.login.authentication.domain.usecase.GetTwoFAStatus
import org.dhis2.mobile.login.authentication.ui.mapper.TwoFAUiStateMapper
import org.dhis2.mobile.login.authentication.ui.viewmodel.TwoFASettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal actual val accountModule = module {
    factory<AccountRepository> {
        AccountRepositoryImpl(
            get(),
        )
    }
    viewModelOf(::AccountsViewModel)
}
