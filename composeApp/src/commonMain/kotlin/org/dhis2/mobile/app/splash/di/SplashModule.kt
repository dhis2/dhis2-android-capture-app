package org.dhis2.mobile.app.splash.di

import org.dhis2.mobile.app.splash.domain.usecase.GetStartDestination
import org.dhis2.mobile.app.splash.ui.viewmodel.SplashViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val splashModule =
    module {
        factory { params ->
            GetStartDestination(
                isUserLoggedIn = get { parametersOf(params.get()) },
                verifyNeedOfflineCode = get { parametersOf(params.get()) },
                getIsSessionLocked = get(),
            )
        }
        viewModel { params ->
            SplashViewModel(get { parametersOf(params.get()) })
        }
    }
