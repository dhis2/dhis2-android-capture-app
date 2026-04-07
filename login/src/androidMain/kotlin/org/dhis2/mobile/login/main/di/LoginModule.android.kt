package org.dhis2.mobile.login.main.di

import android.content.Context
import android.content.pm.ApplicationInfo
import org.dhis2.mobile.login.accounts.data.repository.AccountRepository
import org.dhis2.mobile.login.accounts.data.repository.AccountRepositoryImpl
import org.dhis2.mobile.login.accounts.ui.viewmodel.AccountsViewModel
import org.dhis2.mobile.login.authentication.OpenIdController
import org.dhis2.mobile.login.authentication.OpenIdControllerImpl
import org.dhis2.mobile.login.main.data.LoginRepository
import org.dhis2.mobile.login.main.data.LoginRepositoryImpl
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

internal actual val accountModule =
    module {
        factory<AccountRepository> {
            val context = get<Context>()
            val isDebug = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
            AccountRepositoryImpl(
                get(),
                get(),
                isDebug = isDebug,
                isTrainingFlavor = getProperty("isTrainingFlavor", false),
            )
        }

        single { OpenIdControllerImpl() }
        single<OpenIdController> { get<OpenIdControllerImpl>() }

        factory<LoginRepository> { _ ->
            LoginRepositoryImpl(
                d2 = get(),
                authenticator = get(),
                cryptographyManager = get(),
                preferences = get(),
                d2ErrorMessageProvider = get(),
                crashReportController = get(),
                analyticActions = get(),
                openIdController = get(),
                dispatcher = get(),
                domainErrorMapper = get(),
            )
        }

        viewModel { params ->
            AccountsViewModel(
                navigator = get(),
                repository = get { parametersOf(params.get()) },
            )
        }
    }
