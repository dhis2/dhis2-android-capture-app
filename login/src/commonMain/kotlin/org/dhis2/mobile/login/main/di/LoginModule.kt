package org.dhis2.mobile.login.main.di

import org.dhis2.mobile.login.authentication.di.twoFAModule
import org.dhis2.mobile.login.main.domain.usecase.BiometricLogin
import org.dhis2.mobile.login.main.domain.usecase.GetAvailableUsernames
import org.dhis2.mobile.login.main.domain.usecase.GetBiometricInfo
import org.dhis2.mobile.login.main.domain.usecase.GetHasOtherAccounts
import org.dhis2.mobile.login.main.domain.usecase.GetInitialScreen
import org.dhis2.mobile.login.main.domain.usecase.ImportDatabase
import org.dhis2.mobile.login.main.domain.usecase.LoginUser
import org.dhis2.mobile.login.main.domain.usecase.UpdateBiometricPermission
import org.dhis2.mobile.login.main.domain.usecase.UpdateTrackingPermission
import org.dhis2.mobile.login.main.domain.usecase.ValidateServer
import org.dhis2.mobile.login.main.ui.navigation.AppLinkNavigation
import org.dhis2.mobile.login.main.ui.navigation.DefaultNavigator
import org.dhis2.mobile.login.main.ui.navigation.Navigator
import org.dhis2.mobile.login.main.ui.viewmodel.CredentialsViewModel
import org.dhis2.mobile.login.main.ui.viewmodel.LoginViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val mainLoginModule =
    module {
        single<Navigator> { DefaultNavigator() }
        factoryOf(::GetInitialScreen)
        factoryOf(::ValidateServer)
        factoryOf(::ImportDatabase)
        viewModelOf(::LoginViewModel)
        single<AppLinkNavigation> { AppLinkNavigation() }
        factoryOf(::GetAvailableUsernames)
        factoryOf(::GetHasOtherAccounts)
        factoryOf(::GetBiometricInfo)
        factoryOf(::LoginUser)
        factoryOf(::BiometricLogin)
        factoryOf(::UpdateTrackingPermission)
        factoryOf(::UpdateBiometricPermission)
        viewModel { parameters ->
            val serverName = parameters.get<String?>(0)
            val serverUrl = parameters.get<String>(1)
            val userName = parameters.get<String?>(2)
            val allowRecovery = parameters.get<Boolean>(3)
            CredentialsViewModel(
                navigator = get(),
                getAvailableUsernames = get(),
                getBiometricInfo = get(),
                getHasOtherAccounts = get(),
                loginUser = get(),
                biometricLogin = get(),
                updateTrackingPermission = get(),
                updateBiometricPermission = get(),
                networkStatusProvider = get(),
                serverName = serverName,
                serverUrl = serverUrl,
                username = userName,
                allowRecovery = allowRecovery,
            )
        }
    }

internal expect val accountModule: Module

val loginModule =
    module {
        includes(mainLoginModule, twoFAModule, accountModule)
    }
