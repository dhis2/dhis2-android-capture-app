package org.dhis2.mobile.login.main.di

import org.dhis2.mobile.commons.auth.OpenIdController
import org.dhis2.mobile.commons.auth.OpenIdControllerImpl
import org.dhis2.mobile.login.accounts.data.repository.AccountRepository
import org.dhis2.mobile.login.accounts.data.repository.AccountRepositoryImpl
import org.dhis2.mobile.login.accounts.ui.viewmodel.AccountsViewModel
import org.dhis2.mobile.login.main.data.LoginRepository
import org.dhis2.mobile.login.main.data.LoginRepositoryImpl
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

internal actual val accountModule =
    module {
        factory<AccountRepository> {
            AccountRepositoryImpl(
                get(),
                get(),
            )
        }

        single<OpenIdController> { params ->
            OpenIdControllerImpl(params.get())
        }

        factory<LoginRepository> { params ->
            LoginRepositoryImpl(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get { parametersOf(params.get()) },
                get(),
            )
        }

        viewModel { params ->
            AccountsViewModel(
                navigator = get(),
                repository = get { parametersOf(params.get()) },
            )
        }
    }
