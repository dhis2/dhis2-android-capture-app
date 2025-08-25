package org.dhis2.mobile.login.main.di

import org.dhis2.mobile.login.authentication.di.twoFAModule
import org.dhis2.mobile.login.main.domain.usecase.GetInitialScreen
import org.dhis2.mobile.login.main.domain.usecase.ValidateServer
import org.dhis2.mobile.login.main.ui.navigation.DefaultNavigator
import org.dhis2.mobile.login.main.ui.navigation.Navigator
import org.dhis2.mobile.login.main.ui.viewmodel.LoginViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val mainLoginModule = module {
    single<Navigator> { DefaultNavigator() }
    factoryOf(::GetInitialScreen)
    factoryOf(::ValidateServer)
    viewModelOf(::LoginViewModel)
}

internal expect val accountModule: Module

val loginModule = module {
    includes(mainLoginModule, twoFAModule, accountModule)
}
