package org.dhis2.mobile.login.main.di

import org.dhis2.mobile.login.accounts.data.repository.AccountRepository
import org.dhis2.mobile.login.accounts.data.repository.AccountRepositoryImpl
import org.dhis2.mobile.login.accounts.ui.viewmodel.AccountsViewModel
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
