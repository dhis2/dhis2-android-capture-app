package org.dhis2.mobile.login.main.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.PlatformContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.dhis2.mobile.commons.domain.invoke
import org.dhis2.mobile.commons.extensions.launchUseCase
import org.dhis2.mobile.commons.extensions.withMinimumDuration
import org.dhis2.mobile.commons.network.NetworkStatusProvider
import org.dhis2.mobile.login.main.domain.model.LoginResult
import org.dhis2.mobile.login.main.domain.model.LoginScreenState
import org.dhis2.mobile.login.main.domain.usecase.BiometricLogin
import org.dhis2.mobile.login.main.domain.usecase.GetAvailableUsernames
import org.dhis2.mobile.login.main.domain.usecase.GetBiometricInfo
import org.dhis2.mobile.login.main.domain.usecase.GetHasOtherAccounts
import org.dhis2.mobile.login.main.domain.usecase.LogOutUser
import org.dhis2.mobile.login.main.domain.usecase.LoginUser
import org.dhis2.mobile.login.main.domain.usecase.OpenIdLogin
import org.dhis2.mobile.login.main.domain.usecase.UpdateBiometricPermission
import org.dhis2.mobile.login.main.domain.usecase.UpdateTrackingPermission
import org.dhis2.mobile.login.main.ui.navigation.Navigator
import org.dhis2.mobile.login.main.ui.state.AfterLoginAction
import org.dhis2.mobile.login.main.ui.state.CredentialsInfo
import org.dhis2.mobile.login.main.ui.state.CredentialsUiState
import org.dhis2.mobile.login.main.ui.state.LoginState
import org.dhis2.mobile.login.main.ui.state.OidcInfo
import org.dhis2.mobile.login.main.ui.state.ServerInfo
import org.dhis2.mobile.login.pin.domain.usecase.ForgotPinUseCase
import org.dhis2.mobile.login.pin.domain.usecase.GetIsSessionLockedUseCase

class CredentialsViewModel(
    private val navigator: Navigator,
    private val getAvailableUsernames: GetAvailableUsernames,
    private val getBiometricInfo: GetBiometricInfo,
    private val getHasOtherAccounts: GetHasOtherAccounts,
    private val loginUser: LoginUser,
    private val logOutUser: LogOutUser,
    private val biometricLogin: BiometricLogin,
    private val openIdLogin: OpenIdLogin,
    private val updateTrackingPermission: UpdateTrackingPermission,
    private val updateBiometricPermission: UpdateBiometricPermission,
    networkStatusProvider: NetworkStatusProvider,
    private val serverName: String?,
    private val serverUrl: String,
    private val username: String?,
    private val allowRecovery: Boolean,
    private val getIsSessionLockedUseCase: GetIsSessionLockedUseCase,
    private val forgotPinUseCase: ForgotPinUseCase,
    private val oidcInfo: OidcInfo?,
    private val fromHome: Boolean,
) : ViewModel() {
    private val isNetworkOnline =
        networkStatusProvider.connectionStatus
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                false,
            )

    private val initialState =
        CredentialsUiState(
            serverInfo =
                ServerInfo(
                    serverName = serverName,
                    serverUrl = serverUrl,
                    username = username,
                ),
            credentialsInfo =
                CredentialsInfo(
                    username = username ?: "",
                    password = "",
                    availableUsernames = emptyList(),
                    usernameCanBeEdited = username == null,
                ),
            loginState = LoginState.Disabled,
            errorMessage = null,
            allowRecovery = false,
            canUseBiometrics = false,
            oidcInfo = null,
            afterLoginActions = emptyList(),
            hasOtherAccounts = false,
            isSessionLocked = false,
            displayBiometricsDialog = false,
        )

    private var loginJob: Job? = null

    private val _credentialsScreenState = MutableStateFlow(initialState)
    val credentialsScreenState =
        _credentialsScreenState
            .onStart {
                loadData()
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = initialState,
            )

    private fun loadData() {
        launchUseCase {
            val biometricInfo = getBiometricInfo(serverUrl)

            _credentialsScreenState.emit(
                CredentialsUiState(
                    serverInfo =
                        ServerInfo(
                            serverName = serverName,
                            serverUrl = serverUrl,
                            username = username,
                        ),
                    credentialsInfo =
                        CredentialsInfo(
                            username = username ?: "",
                            password = "",
                            availableUsernames = getAvailableUsernames(),
                            usernameCanBeEdited = username == null,
                        ),
                    loginState = LoginState.Disabled,
                    errorMessage = null,
                    allowRecovery = allowRecovery,
                    canUseBiometrics = getBiometricInfo(serverUrl).canUseBiometrics,
                    oidcInfo = oidcInfo,
                    afterLoginActions = emptyList(),
                    hasOtherAccounts = getHasOtherAccounts(),
                    isSessionLocked = getIsSessionLockedUseCase(),
                    displayBiometricsDialog = biometricInfo.canUseBiometrics && !fromHome,
                ),
            )
        }
    }

    fun updateUsername(username: String) {
        _credentialsScreenState.update {
            it.copy(
                credentialsInfo =
                    it.credentialsInfo.copy(
                        username = username,
                    ),
                loginState =
                    if (username.isNotBlank() &&
                        it.credentialsInfo.password.isNotBlank()
                    ) {
                        LoginState.Enabled
                    } else {
                        LoginState.Disabled
                    },
                errorMessage = null,
            )
        }
    }

    fun updatePassword(password: String) {
        _credentialsScreenState.update {
            it.copy(
                credentialsInfo =
                    it.credentialsInfo.copy(
                        password = password,
                    ),
                loginState =
                    if (password.isNotBlank() &&
                        it.credentialsInfo.username.isNotBlank()
                    ) {
                        LoginState.Enabled
                    } else {
                        LoginState.Disabled
                    },
                errorMessage = null,
            )
        }
    }

    fun onLoginClicked() {
        startLoginJob {
            loginUser(
                serverUrl = _credentialsScreenState.value.serverInfo.serverUrl,
                username = _credentialsScreenState.value.credentialsInfo.username,
                password = _credentialsScreenState.value.credentialsInfo.password,
                isNetworkAvailable = isNetworkOnline.value,
            )
        }
    }

    fun onOpenIdLogin() {
        startLoginJob {
            openIdLogin(
                serverUrl = _credentialsScreenState.value.serverInfo.serverUrl,
                isNetworkAvailable = isNetworkOnline.value,
                clientId = _credentialsScreenState.value.oidcInfo?.oidcClientId ?: "",
                redirectUri = _credentialsScreenState.value.oidcInfo?.oidcRedirectUri ?: "",
                discoveryUri = _credentialsScreenState.value.oidcInfo?.discoveryUri(),
                authorizationUri = _credentialsScreenState.value.oidcInfo?.authorizationUri(),
                tokenUrl = _credentialsScreenState.value.oidcInfo?.tokenUrl(),
            )
        }
    }

    private fun startLoginJob(loginCall: suspend () -> LoginResult) {
        _credentialsScreenState.update {
            it.copy(
                loginState = LoginState.Running,
            )
        }
        loginJob =
            launchUseCase {
                val result =
                    withMinimumDuration {
                        loginCall()
                    }
                handleLoginResult(result)
            }
        loginJob?.invokeOnCompletion {
            _credentialsScreenState.update {
                it.copy(
                    loginState = LoginState.Enabled,
                )
            }
        }
    }

    private suspend fun handleLoginResult(result: LoginResult) =
        when (result) {
            is LoginResult.Success -> {
                _credentialsScreenState.update {
                    it.copy(
                        afterLoginActions =
                            buildList {
                                if (result.displayTrackingMessage) {
                                    add(AfterLoginAction.DisplayTrackingMessage)
                                }
                                if (getBiometricInfo(serverUrl).displayBiometricsMessageAfterLogin) {
                                    add(AfterLoginAction.DisplayBiometricsMessage)
                                }
                                add(AfterLoginAction.NavigateToNextScreen(result.initialSyncDone))
                            },
                    )
                }
            }

            is LoginResult.Error -> {
                _credentialsScreenState.update {
                    it.copy(
                        errorMessage = result.message,
                    )
                }
            }
        }

    fun cancelLogin() {
        loginJob?.cancel()
        launchUseCase {
            logOutUser.invoke()
        }
    }

    context(platformContext: PlatformContext)
    fun onBiometricsClicked() {
        // Cancel any previous biometric login attempt
        loginJob?.cancel()

        loginJob =
            launchUseCase {
                val result = biometricLogin()

                when {
                    result.isSuccess -> {
                        updatePassword(password = result.getOrNull() ?: "")
                        onLoginClicked()
                    }

                    else -> {
                        _credentialsScreenState.update {
                            it.copy(
                                errorMessage = result.exceptionOrNull()?.message,
                                displayBiometricsDialog = false,
                            )
                        }
                    }
                }
            }
    }

    fun onManageAccountsClicked() {
        launchUseCase {
            navigator.navigate(destination = LoginScreenState.Accounts)
        }
    }

    fun onRecoverAccountClicked() {
        launchUseCase {
            navigator.navigate(
                destination =
                    LoginScreenState.RecoverAccount(
                        selectedServer = serverUrl,
                    ),
            )
        }
    }

    fun onTrackingPermission(granted: Boolean) {
        launchUseCase {
            updateTrackingPermission(granted)
            _credentialsScreenState.update {
                it.copy(
                    afterLoginActions =
                        it.afterLoginActions.toMutableList().apply {
                            remove(AfterLoginAction.DisplayTrackingMessage)
                        },
                )
            }
        }
    }

    fun checkPrivacyPolicy() {
        launchUseCase {
            navigator.navigateToPrivacyPolicy()
        }
    }

    context(platformContext: PlatformContext)
    fun onEnableBiometrics(granted: Boolean) {
        launchUseCase {
            updateBiometricPermission(
                serverUrl,
                credentialsScreenState.value.credentialsInfo.username,
                credentialsScreenState.value.credentialsInfo.password,
                granted,
            )
            _credentialsScreenState.update {
                it.copy(
                    afterLoginActions =
                        it.afterLoginActions.toMutableList().apply {
                            remove(AfterLoginAction.DisplayBiometricsMessage)
                        },
                )
            }
        }
    }

    fun goToNextScreen(initialSyncDone: Boolean) {
        launchUseCase {
            if (initialSyncDone) {
                navigator.navigateToHome()
            } else {
                navigator.navigateToSync()
            }
        }
    }

    fun onPinUnlocked() {
        // Session unlocked successfully, update the state
        launchUseCase {
            _credentialsScreenState.update {
                it.copy(
                    isSessionLocked = false,
                )
            }
            navigator.navigateToHome()
        }
    }

    fun onPinDismissed() {
        // User dismissed the PIN dialog (forgot PIN)
        // Logout the user from the app and ask for the password
        launchUseCase {
            forgotPinUseCase()
            _credentialsScreenState.update {
                it.copy(
                    isSessionLocked = false,
                )
            }
        }
    }
}
