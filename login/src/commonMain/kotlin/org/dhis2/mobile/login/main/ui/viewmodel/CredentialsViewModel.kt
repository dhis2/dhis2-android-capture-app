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
import kotlinx.coroutines.launch
import org.dhis2.mobile.commons.network.NetworkStatusProvider
import org.dhis2.mobile.login.main.domain.model.LoginResult
import org.dhis2.mobile.login.main.domain.model.LoginScreenState
import org.dhis2.mobile.login.main.domain.usecase.BiometricLogin
import org.dhis2.mobile.login.main.domain.usecase.GetAvailableUsernames
import org.dhis2.mobile.login.main.domain.usecase.GetBiometricInfo
import org.dhis2.mobile.login.main.domain.usecase.LoginUser
import org.dhis2.mobile.login.main.domain.usecase.UpdateBiometricPermission
import org.dhis2.mobile.login.main.domain.usecase.UpdateTrackingPermission
import org.dhis2.mobile.login.main.ui.navigation.Navigator
import org.dhis2.mobile.login.main.ui.states.AfterLoginAction
import org.dhis2.mobile.login.main.ui.states.CredentialsInfo
import org.dhis2.mobile.login.main.ui.states.CredentialsUiState
import org.dhis2.mobile.login.main.ui.states.LoginState
import org.dhis2.mobile.login.main.ui.states.OidcInfo
import org.dhis2.mobile.login.main.ui.states.ServerInfo

class CredentialsViewModel(
    private val navigator: Navigator,
    private val getAvailableUsernames: GetAvailableUsernames,
    private val getBiometricInfo: GetBiometricInfo,
    private val loginUser: LoginUser,
    private val biometricLogin: BiometricLogin,
    private val updateTrackingPermission: UpdateTrackingPermission,
    private val updateBiometricPermission: UpdateBiometricPermission,
    networkStatusProvider: NetworkStatusProvider,
    private val serverName: String?,
    private val serverUrl: String,
    private val username: String?,
    private val allowRecovery: Boolean,
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
        viewModelScope.launch {
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
                    oidcInfo =
                        OidcInfo(
                            // TODO: This should either be received from validate server or with a new usecase
                            oidcIcon = "icon",
                            oidcLoginText = "Open id connect",
                            oidcUrl = "https://openid.login.test",
                        ),
                    afterLoginActions = emptyList(),
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
        _credentialsScreenState.update {
            it.copy(
                loginState = LoginState.Running,
            )
        }
        loginJob =
            viewModelScope.launch {
                val result =
                    loginUser(
                        serverUrl = _credentialsScreenState.value.serverInfo.serverUrl,
                        username = _credentialsScreenState.value.credentialsInfo.username,
                        password = _credentialsScreenState.value.credentialsInfo.password,
                        isNetworkAvailable = isNetworkOnline.value,
                    )
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
            }
        loginJob?.invokeOnCompletion {
            _credentialsScreenState.update {
                it.copy(
                    loginState = LoginState.Enabled,
                )
            }
        }
    }

    fun onOpenIdLogin(url: String) {
        // TODO
    }

    fun cancelLogin() {
        loginJob?.cancel()
    }

    context(platformContext: PlatformContext)
    fun onBiometricsClicked() {
        viewModelScope.launch {
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
                        )
                    }
                }
            }
        }
    }

    fun onManageAccountsClicked() {
        viewModelScope.launch {
            navigator.navigate(destination = LoginScreenState.Accounts)
        }
    }

    fun onRecoverAccountClicked() {
        viewModelScope.launch {
            navigator.navigate(
                destination =
                    LoginScreenState.RecoverAccount(
                        selectedServer = serverUrl,
                    ),
            )
        }
    }

    fun onTrackingPermission(granted: Boolean) {
        viewModelScope.launch {
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
        viewModelScope.launch {
            navigator.navigateToPrivacyPolicy()
        }
    }

    context(platformContext: PlatformContext)
    fun onEnableBiometrics(granted: Boolean) {
        viewModelScope.launch {
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
        viewModelScope.launch {
            if (initialSyncDone) {
                navigator.navigateToHome()
            } else {
                navigator.navigateToSync()
            }
        }
    }
}
