package org.dhis2.mobile.login.main.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.PlatformContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dhis2.mobile.commons.domain.invoke
import org.dhis2.mobile.commons.extensions.launchUseCase
import org.dhis2.mobile.commons.extensions.withMinimumDuration
import org.dhis2.mobile.commons.network.NetworkStatusProvider
import org.dhis2.mobile.login.main.domain.model.CredentialsEntryMode
import org.dhis2.mobile.login.main.domain.model.DeviceEnrollmentInfo
import org.dhis2.mobile.login.main.domain.model.LoginResult
import org.dhis2.mobile.login.main.domain.model.LoginScreenState
import org.dhis2.mobile.login.main.domain.model.OpenIdLoginConfiguration
import org.dhis2.mobile.login.main.domain.model.SessionRenewalRequest
import org.dhis2.mobile.login.main.domain.usecase.BiometricLogin
import org.dhis2.mobile.login.main.domain.usecase.GetAvailableUsernames
import org.dhis2.mobile.login.main.domain.usecase.GetBiometricInfo
import org.dhis2.mobile.login.main.domain.usecase.GetDeviceEnrollmentUrl
import org.dhis2.mobile.login.main.domain.usecase.GetHasOtherAccounts
import org.dhis2.mobile.login.main.domain.usecase.GetOAuthLogoutUrl
import org.dhis2.mobile.login.main.domain.usecase.GetSessionRenewalUrl
import org.dhis2.mobile.login.main.domain.usecase.LogOutUser
import org.dhis2.mobile.login.main.domain.usecase.LoginUser
import org.dhis2.mobile.login.main.domain.usecase.LoginUserOffline
import org.dhis2.mobile.login.main.domain.usecase.LoginUserWithOAuth
import org.dhis2.mobile.login.main.domain.usecase.OpenIdLogin
import org.dhis2.mobile.login.main.domain.usecase.ProcessDeviceEnrollment
import org.dhis2.mobile.login.main.domain.usecase.SetOAuthPin
import org.dhis2.mobile.login.main.domain.usecase.UpdateBiometricPermission
import org.dhis2.mobile.login.main.domain.usecase.UpdateTrackingPermission
import org.dhis2.mobile.login.main.ui.navigation.AppLinkNavigation
import org.dhis2.mobile.login.main.ui.navigation.Navigator
import org.dhis2.mobile.login.main.ui.provider.CredentialsResourceProvider
import org.dhis2.mobile.login.main.ui.state.AfterLoginAction
import org.dhis2.mobile.login.main.ui.state.CredentialsInfo
import org.dhis2.mobile.login.main.ui.state.CredentialsUiState
import org.dhis2.mobile.login.main.ui.state.LoginState
import org.dhis2.mobile.login.main.ui.state.OidcInfo
import org.dhis2.mobile.login.main.ui.state.ServerInfo
import org.dhis2.mobile.login.pin.domain.usecase.ForgotPinUseCase
import org.dhis2.mobile.login.pin.domain.usecase.GetIsSessionLockedUseCase
import kotlin.time.Duration.Companion.seconds

class CredentialsViewModel(
    private val navigator: Navigator,
    private val getAvailableUsernames: GetAvailableUsernames,
    private val getBiometricInfo: GetBiometricInfo,
    private val getHasOtherAccounts: GetHasOtherAccounts,
    private val loginUser: LoginUser,
    private val logOutUser: LogOutUser,
    private val biometricLogin: BiometricLogin,
    private val openIdLogin: OpenIdLogin,
    private val loginUserWithOAuth: LoginUserWithOAuth,
    private val getDeviceEnrollmentUrl: GetDeviceEnrollmentUrl,
    private val getOAuthLogoutUrl: GetOAuthLogoutUrl,
    private val processDeviceEnrollment: ProcessDeviceEnrollment,
    private val updateTrackingPermission: UpdateTrackingPermission,
    private val updateBiometricPermission: UpdateBiometricPermission,
    private val appLinkNavigation: AppLinkNavigation,
    networkStatusProvider: NetworkStatusProvider,
    private val serverName: String?,
    private val serverUrl: String,
    private val username: String?,
    private val allowRecovery: Boolean,
    private val getIsSessionLockedUseCase: GetIsSessionLockedUseCase,
    private val forgotPinUseCase: ForgotPinUseCase,
    private val oidcInfo: OidcInfo?,
    private val entryMode: CredentialsEntryMode,
    private val autoPromptLogin: Boolean,
    private val setOAuthPin: SetOAuthPin,
    private val loginUserOfflineWithCode: LoginUserOffline,
    private val credentialsResourceProvider: CredentialsResourceProvider,
    private val getSessionRenewalUrl: GetSessionRenewalUrl,
) : ViewModel() {
    companion object {
        private val COUNTDOWN_TICK_INTERVAL = 1.seconds
    }

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
            credentialsInfo = null,
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

    private var lockoutJob: Job? = null

    private val isLockoutActive: Boolean
        get() = lockoutJob?.isActive == true

    private var pendingOAuthLoginResult: LoginResult.Success? = null

    private var appLinkJob: Job? = null

    private var offlinePin: String = ""

    private val _credentialsScreenState = MutableStateFlow(initialState)
    val credentialsScreenState =
        _credentialsScreenState
            .onStart {
                loadData()
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = initialState,
            )

    private fun loadData() {
        when (entryMode) {
            CredentialsEntryMode.NEW_ACCOUNT_BASIC -> handleNewBasicAccount()
            CredentialsEntryMode.NEW_ACCOUNT_OAUTH -> fetchOAuthEnrollmentUrl()
            CredentialsEntryMode.EXISTING_OAUTH -> handleExistingOAuthAccount()
            CredentialsEntryMode.EXISTING_BASIC,
            CredentialsEntryMode.EXISTING_OPEN_ID,
            -> handleExistingPasswordAccount()
        }
    }

    private fun handleNewBasicAccount() {
        launchUseCase {
            val biometricInfo = getBiometricInfo(serverUrl)
            _credentialsScreenState.update { current ->
                current.copy(
                    credentialsInfo =
                        CredentialsInfo(
                            username = username ?: "",
                            password = "",
                            availableUsernames = getAvailableUsernames(),
                            usernameCanBeEdited = username == null,
                        ),
                    errorMessage = null,
                    allowRecovery = allowRecovery,
                    canUseBiometrics = biometricInfo.canUseBiometrics,
                    oidcInfo = oidcInfo,
                    afterLoginActions = emptyList(),
                    hasOtherAccounts = getHasOtherAccounts(),
                    displayBiometricsDialog = biometricInfo.canUseBiometrics && autoPromptLogin,
                )
            }
        }
    }

    private fun handleExistingOAuthAccount() {
        launchUseCase {
            val biometricInfo = getBiometricInfo(serverUrl)
            _credentialsScreenState.update { current ->
                current.copy(
                    loginState = LoginState.Enabled,
                    errorMessage = null,
                    allowRecovery = allowRecovery,
                    canUseBiometrics = biometricInfo.canUseBiometrics,
                    oidcInfo = oidcInfo,
                    afterLoginActions = emptyList(),
                    hasOtherAccounts = getHasOtherAccounts(),
                    isSessionLocked =
                        getIsSessionLockedUseCase(requireOfflineCredentials = true) &&
                            autoPromptLogin,
                    displayBiometricsDialog = biometricInfo.canUseBiometrics && autoPromptLogin,
                )
            }
        }
    }

    private fun handleExistingPasswordAccount() {
        launchUseCase {
            val biometricInfo = getBiometricInfo(serverUrl)
            _credentialsScreenState.update { current ->
                current.copy(
                    credentialsInfo =
                        CredentialsInfo(
                            username = username ?: "",
                            password = "",
                            availableUsernames = getAvailableUsernames(),
                            usernameCanBeEdited = false,
                        ),
                    loginState = LoginState.Disabled,
                    errorMessage = null,
                    allowRecovery = allowRecovery,
                    canUseBiometrics = biometricInfo.canUseBiometrics,
                    oidcInfo = oidcInfo,
                    afterLoginActions = emptyList(),
                    hasOtherAccounts = getHasOtherAccounts(),
                    isSessionLocked = getIsSessionLockedUseCase(requireOfflineCredentials = false),
                    displayBiometricsDialog = biometricInfo.canUseBiometrics && autoPromptLogin,
                )
            }
        }
    }

    private fun fetchOAuthEnrollmentUrl() {
        _credentialsScreenState.update {
            it.copy(
                loginState = LoginState.Running,
            )
        }
        launchUseCase {
            getDeviceEnrollmentUrl(serverUrl).fold(
                onSuccess = { enrollmentURL ->
                    // First OAuth call (enrollment) - clear any previous OAuth sessions
                    startListeningForOAuthCallbacks()
                    navigator.navigate(
                        LoginScreenState.OauthAuthentication(
                            selectedServer = enrollmentURL,
                        ),
                    )
                    _credentialsScreenState.update {
                        it.copy(
                            loginState = LoginState.Enabled,
                        )
                    }
                },
                onFailure = { error ->
                    _credentialsScreenState.update {
                        it.copy(
                            loginState = LoginState.Enabled,
                            errorMessage = error.message,
                        )
                    }
                },
            )
        }
    }

    /**
     * Renews the session of an account that already exists on this device, which is what the user
     * needs once the tokens expired or were never stored here. Enrollment is only repeated when
     * this device holds no client registration; the account database is left untouched either way,
     * so the redirect is handled by the same callbacks as a first login.
     */
    fun onRenewSession() {
        _credentialsScreenState.update {
            it.copy(
                loginState = LoginState.Running,
                errorMessage = null,
            )
        }
        launchUseCase {
            getSessionRenewalUrl(
                SessionRenewalRequest(
                    serverUrl = serverUrl,
                    isNetworkAvailable = isNetworkOnline.value,
                ),
            ).fold(
                onSuccess = { loginUrl ->
                    startListeningForOAuthCallbacks()
                    navigator.navigate(
                        LoginScreenState.OauthAuthentication(
                            selectedServer = loginUrl,
                        ),
                    )
                    _credentialsScreenState.update {
                        it.copy(loginState = LoginState.Enabled)
                    }
                },
                onFailure = { error ->
                    _credentialsScreenState.update {
                        it.copy(
                            loginState = LoginState.Enabled,
                            errorMessage = error.message,
                        )
                    }
                },
            )
        }
    }

    // AppLinkNavigation is a single-delivery channel shared by every CredentialsViewModel alive
    // on the back stack, so only the instance that launched the OAuth browser round-trip may
    // collect it. Collection starts when the flow begins and stops when it terminates.
    private fun startListeningForOAuthCallbacks() {
        if (appLinkJob?.isActive == true) return
        appLinkJob =
            viewModelScope.launch {
                appLinkNavigation.appLink.collect { urlString ->
                    handleOAuthCallbacks(urlString)
                }
            }
    }

    private fun stopListeningForOAuthCallbacks() {
        appLinkJob?.cancel()
        appLinkJob = null
    }

    private fun handleOAuthCallbacks(urlString: String) {
        // First check if there is any error
        val error = urlString.substringAfter("error=", "").substringBefore('&')
        if (error.isNotEmpty()) {
            stopListeningForOAuthCallbacks()
            _credentialsScreenState.update {
                it.copy(
                    errorMessage = error,
                    loginState = LoginState.Enabled,
                )
            }
            return
        }

        // Check if there is a device enrollment callback
        val iat = urlString.substringAfter("iat=", "").substringBefore('&')
        val state = urlString.substringAfter("state=", "").substringBefore('&')
        if (iat.isNotEmpty()) {
            registerDevice(iat, state)
            return
        }

        // Check if there is a login callback with the authorization code
        val code = urlString.substringAfter("code=", "").substringBefore('&')
        if (code.isNotEmpty()) {
            loginWithOAuthCode(code, state)
            return
        }

        // Logout callback after a successful OAuth login: resume the deferred actions
        if (pendingOAuthLoginResult != null) {
            completeOAuthLogin()
            return
        }

        stopListeningForOAuthCallbacks()
        _credentialsScreenState.update {
            it.copy(
                loginState = LoginState.Enabled,
            )
        }
    }

    private fun loginWithOAuthCode(
        code: String,
        state: String,
    ) {
        _credentialsScreenState.update {
            it.copy(
                loginState = LoginState.Running,
                errorMessage = null,
            )
        }
        loginJob =
            launchUseCase {
                val result =
                    withMinimumDuration {
                        loginUserWithOAuth(
                            serverUrl = serverUrl,
                            code = code,
                            state = state,
                        )
                    }
                when (result) {
                    is LoginResult.Success -> {
                        // Defer entering the app until the server session cookie is cleared
                        pendingOAuthLoginResult = result
                        getOAuthLogoutUrl(serverUrl).fold(
                            onSuccess = { logoutUrl ->
                                navigator.navigate(
                                    LoginScreenState.OauthAuthentication(
                                        selectedServer = logoutUrl,
                                    ),
                                )
                            },
                            onFailure = {
                                // Best-effort: proceed into the app if the logout URL can't be built
                                completeOAuthLogin()
                            },
                        )
                    }

                    is LoginResult.Error, is LoginResult.LockOut -> {
                        stopListeningForOAuthCallbacks()
                        handleLoginResult(result)
                        _credentialsScreenState.update {
                            it.copy(loginState = LoginState.Enabled)
                        }
                    }
                }
            }
    }

    private fun completeOAuthLogin() {
        val pending = pendingOAuthLoginResult ?: return
        pendingOAuthLoginResult = null
        stopListeningForOAuthCallbacks()
        launchUseCase {
            handleLoginResult(pending, sessionOpenedInBrowser = true)
            _credentialsScreenState.update {
                it.copy(loginState = LoginState.Enabled)
            }
        }
    }

    private fun registerDevice(
        enrollmentIat: String,
        state: String,
    ) {
        launchUseCase {
            _credentialsScreenState.update {
                it.copy(loginState = LoginState.Running)
            }

            processDeviceEnrollment(
                DeviceEnrollmentInfo(
                    iat = enrollmentIat,
                    serverURL = serverUrl,
                    state = state,
                ),
            ).fold(
                onSuccess = { consentUrl ->
                    // Second OAuth call (consent) - keep session from enrollment
                    navigator.navigate(
                        LoginScreenState.OauthAuthentication(
                            selectedServer = consentUrl,
                        ),
                    )
                },
                onFailure = { error ->
                    stopListeningForOAuthCallbacks()
                    _credentialsScreenState.update {
                        it.copy(
                            errorMessage = error.message,
                            loginState = LoginState.Enabled,
                        )
                    }
                },
            )
        }
    }

    fun updateUsername(username: String) {
        _credentialsScreenState.update {
            it.copy(
                credentialsInfo =
                    it.credentialsInfo?.copy(
                        username = username,
                    ),
                loginState =
                    if (username.isNotBlank() &&
                        it.credentialsInfo?.password?.isNotBlank() == true
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
                    it.credentialsInfo?.copy(
                        password = password,
                    ),
                loginState =
                    if (password.isNotBlank() &&
                        it.credentialsInfo?.username?.isNotBlank() == true
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
        when (entryMode) {
            CredentialsEntryMode.NEW_ACCOUNT_OAUTH -> fetchOAuthEnrollmentUrl()
            CredentialsEntryMode.EXISTING_OAUTH ->
                _credentialsScreenState.update { it.copy(isSessionLocked = true) }

            else ->
                startLoginJob {
                    loginUser(
                        serverUrl = _credentialsScreenState.value.serverInfo.serverUrl,
                        username = _credentialsScreenState.value.username().trim(),
                        password = _credentialsScreenState.value.credentialsInfo?.password ?: "",
                    )
                }
        }
    }

    fun onOpenIdLogin() {
        startLoginJob {
            openIdLogin(
                OpenIdLoginConfiguration(
                    serverUrl = _credentialsScreenState.value.serverInfo.serverUrl,
                    isNetworkAvailable = isNetworkOnline.value,
                    clientId = _credentialsScreenState.value.oidcInfo?.oidcClientId ?: "",
                    redirectUri = _credentialsScreenState.value.oidcInfo?.oidcRedirectUri ?: "",
                    discoveryUri = _credentialsScreenState.value.oidcInfo?.discoveryUri(),
                    authorizationUri = _credentialsScreenState.value.oidcInfo?.authorizationUri(),
                    tokenUrl = _credentialsScreenState.value.oidcInfo?.tokenUrl(),
                    prompt = _credentialsScreenState.value.oidcInfo?.userPrompt,
                ),
            )
        }
    }

    private fun startLoginJob(loginCall: suspend () -> LoginResult) {
        _credentialsScreenState.update {
            it.copy(
                loginState = LoginState.Running,
                errorMessage = null,
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
            if (!isLockoutActive) {
                _credentialsScreenState.update {
                    it.copy(
                        loginState = LoginState.Enabled,
                    )
                }
            }
        }
    }

    private suspend fun handleLoginResult(
        result: LoginResult,
        sessionOpenedInBrowser: Boolean = false,
    ) = when (result) {
        is LoginResult.Success -> {
            _credentialsScreenState.update {
                it.copy(
                    afterLoginActions =
                        buildList {
                            // A session opened through the browser always sets the offline
                            // credential: on a first login there is none yet, and on a renewal
                            // this is also how a forgotten one is replaced
                            if (sessionOpenedInBrowser) {
                                add(AfterLoginAction.CreateOfflineCredential)
                            }
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
            val errorMessage =
                result.attemptsLeft?.let { attemptsLeft ->
                    credentialsResourceProvider.getLoginErrorWithAttempts(result.message, attemptsLeft)
                } ?: result.message
            _credentialsScreenState.update {
                it.copy(
                    errorMessage = errorMessage,
                )
            }
        }
        is LoginResult.LockOut -> {
            startLockoutCountdown(result.lockoutSeconds)
        }
    }

    private fun startLockoutCountdown(lockoutSeconds: Int) {
        lockoutJob?.cancel()
        lockoutJob =
            viewModelScope.launch {
                for (remainingSeconds in lockoutSeconds downTo 1) {
                    _credentialsScreenState.update {
                        it.copy(
                            loginState = LoginState.Disabled,
                            errorMessage = credentialsResourceProvider.getLockoutCountdownMessage(remainingSeconds),
                        )
                    }
                    delay(COUNTDOWN_TICK_INTERVAL)
                }
                _credentialsScreenState.update {
                    it.copy(
                        loginState = LoginState.Enabled,
                        errorMessage = null,
                    )
                }
            }
    }

    fun cancelLogin() {
        loginJob?.cancel()
        launchUseCase {
            logOutUser.invoke()
            _credentialsScreenState.update {
                it.copy(
                    loginState = LoginState.Enabled,
                    errorMessage = null,
                )
            }
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
                        if (entryMode == CredentialsEntryMode.EXISTING_OAUTH) {
                            result.getOrNull()?.let {
                                onOfflineCredentialEntered(it)
                            } ?: { onLoginClicked() }
                        } else {
                            updatePassword(password = result.getOrNull() ?: "")
                            onLoginClicked()
                        }
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
        val credential =
            if (entryMode == CredentialsEntryMode.NEW_ACCOUNT_OAUTH) {
                offlinePin
            } else {
                credentialsScreenState.value.credentialsInfo?.password ?: ""
            }
        launchUseCase {
            updateBiometricPermission(
                serverUrl,
                credentialsScreenState.value.credentialsInfo?.username ?: "",
                credential,
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

    fun onOfflineCredentialEntered(credential: String) {
        launchUseCase {
            _credentialsScreenState.update { it.copy(isSessionLocked = false) }
            startLoginJob {
                loginUserOfflineWithCode(
                    serverUrl = serverUrl,
                    username = username ?: "",
                    code = credential,
                )
            }
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

    fun onOfflineCredentialCreated(credential: String) {
        launchUseCase {
            setOAuthPin(credential).fold(
                onSuccess = {
                    _credentialsScreenState
                        .update {
                            it.copy(
                                afterLoginActions =
                                    it.afterLoginActions.toMutableList().apply {
                                        remove(AfterLoginAction.CreateOfflineCredential)
                                    },
                            )
                        }.also { offlinePin = credential }
                },
                onFailure = { error ->
                    logOutUser.invoke()
                    _credentialsScreenState.update {
                        it.copy(
                            afterLoginActions = emptyList(),
                            loginState = LoginState.Enabled,
                            errorMessage = error.message,
                        )
                    }
                },
            )
        }
    }
}
