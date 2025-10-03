package org.dhis2.mobile.login.main.di

import org.dhis2.mobile.login.accounts.data.repository.AccountRepository
import org.dhis2.mobile.login.accounts.data.repository.AccountRepositoryImpl
import org.dhis2.mobile.login.accounts.ui.viewmodel.AccountsViewModel
import org.dhis2.mobile.login.main.data.LoginRepository
import org.dhis2.mobile.login.main.data.LoginRepositoryImpl
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

internal actual val accountModule =
    module {
        factory<AccountRepository> {
            AccountRepositoryImpl(
                get(),
            )
        }

        factory<LoginRepository> {
            val hasConnectionCallback = { true }
            LoginRepositoryImpl(
                get(),
                get(),
                get(),
                get(),
                get { parametersOf(hasConnectionCallback) },
                get(),
                get(),
            )
        }

        viewModelOf(::AccountsViewModel)
    }
