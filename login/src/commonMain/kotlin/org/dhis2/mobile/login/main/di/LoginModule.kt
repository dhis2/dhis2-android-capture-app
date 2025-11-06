package org.dhis2.mobile.login.main.di

import coil3.PlatformContext
import org.dhis2.mobile.login.authentication.di.twoFAModule
import org.dhis2.mobile.login.main.domain.usecase.BiometricLogin
import org.dhis2.mobile.login.main.domain.usecase.GetAvailableUsernames
import org.dhis2.mobile.login.main.domain.usecase.GetBiometricInfo
import org.dhis2.mobile.login.main.domain.usecase.GetHasOtherAccounts
import org.dhis2.mobile.login.main.domain.usecase.GetInitialScreen
import org.dhis2.mobile.login.main.domain.usecase.ImportDatabase
import org.dhis2.mobile.login.main.domain.usecase.LogOutUser
import org.dhis2.mobile.login.main.domain.usecase.LoginUser
import org.dhis2.mobile.login.main.domain.usecase.OpenIdLogin
import org.dhis2.mobile.login.main.domain.usecase.UpdateBiometricPermission
import org.dhis2.mobile.login.main.domain.usecase.UpdateTrackingPermission
import org.dhis2.mobile.login.main.domain.usecase.ValidateServer
import org.dhis2.mobile.login.main.ui.navigation.AppLinkNavigation
import org.dhis2.mobile.login.main.ui.navigation.DefaultNavigator
import org.dhis2.mobile.login.main.ui.navigation.Navigator
import org.dhis2.mobile.login.main.ui.state.OidcInfo
import org.dhis2.mobile.login.main.ui.viewmodel.CredentialsViewModel
import org.dhis2.mobile.login.main.ui.viewmodel.LoginViewModel
import org.dhis2.mobile.login.pin.di.completePinModule
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

internal val mainLoginModule =
    module {
        single<Navigator> { DefaultNavigator() }
        factoryOf(::GetInitialScreen)
        factory { params ->
            ValidateServer(get { parametersOf(params.get()) })
        }
        factory { params ->
            ImportDatabase(get { parametersOf(params.get()) })
        }
        single<AppLinkNavigation> { AppLinkNavigation() }
        factory { params ->
            GetAvailableUsernames(get { parametersOf(params.get()) })
        }
        factory { params ->
            GetHasOtherAccounts(get { parametersOf(params.get()) })
        }
        factory { params ->
            GetBiometricInfo(get { parametersOf(params.get()) })
        }
        factory { params ->
            LoginUser(get { parametersOf(params.get()) })
        }
        factory { params ->
            LogOutUser(get { parametersOf(params.get()) })
        }
        factory { params ->
            BiometricLogin(get { parametersOf(params.get()) })
        }
        factory { params ->
            UpdateTrackingPermission(get { parametersOf(params.get()) })
        }

        factory { params ->
            UpdateBiometricPermission(
                get { parametersOf(params.get()) },
                get { parametersOf(params.get()) },
                get { parametersOf(params.get()) },
                get { parametersOf(params.get()) },
            )
        }
        factory { params ->
            OpenIdLogin(get { parametersOf(params.get()) })
        }
        viewModel { parameters ->
            val context = parameters.get<PlatformContext>()
            LoginViewModel(
                get(),
                get(),
                get { parametersOf(context) },
                get { parametersOf(context) },
                get(),
                get(),
            )
        }
        viewModel { parameters ->
            val serverName = parameters[0] as String?
            val serverUrl = parameters[1] as String
            val userName = parameters[2] as String?
            val allowRecovery = parameters[3] as Boolean
            val oidcInfo = parameters[4] as OidcInfo?
            val context = parameters[5] as PlatformContext
            val fromHome = parameters[6] as Boolean
            CredentialsViewModel(
                navigator = get(),
                getAvailableUsernames = get { parametersOf(context) },
                getBiometricInfo = get { parametersOf(context) },
                getHasOtherAccounts = get { parametersOf(context) },
                loginUser = get { parametersOf(context) },
                logOutUser = get { parametersOf(context) },
                openIdLogin = get { parametersOf(context) },
                biometricLogin = get { parametersOf(context) },
                updateTrackingPermission = get { parametersOf(context) },
                updateBiometricPermission = get { parametersOf(context) },
                networkStatusProvider = get(),
                serverName = serverName,
                serverUrl = serverUrl,
                username = userName,
                allowRecovery = allowRecovery,
                getIsSessionLockedUseCase = get(),
                oidcInfo = oidcInfo,
                forgotPinUseCase = get(),
                fromHome = fromHome,
            )
        }
    }

internal expect val accountModule: Module

val loginModule =
    module {
        includes(mainLoginModule, twoFAModule, accountModule, completePinModule)
    }
