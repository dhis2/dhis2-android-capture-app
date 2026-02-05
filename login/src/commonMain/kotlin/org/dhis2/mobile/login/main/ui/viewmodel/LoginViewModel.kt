package org.dhis2.mobile.login.main.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.dhis2.mobile.commons.extensions.launchUseCase
import org.dhis2.mobile.commons.extensions.withMinimumDuration
import org.dhis2.mobile.commons.network.NetworkStatusProvider
import org.dhis2.mobile.login.main.domain.model.DeviceEnrollmentInfo
import org.dhis2.mobile.login.main.domain.model.LoginResult
import org.dhis2.mobile.login.main.domain.model.LoginScreenState
import org.dhis2.mobile.login.main.domain.model.LoginScreenState.LegacyLogin
import org.dhis2.mobile.login.main.domain.model.LoginScreenState.OauthLogin
import org.dhis2.mobile.login.main.domain.model.ServerValidationResult
import org.dhis2.mobile.login.main.domain.usecase.GetDeviceEnrollmentUrl
import org.dhis2.mobile.login.main.domain.usecase.GetInitialScreen
import org.dhis2.mobile.login.main.domain.usecase.ImportDatabase
import org.dhis2.mobile.login.main.domain.usecase.LoginUserWithOAuth
import org.dhis2.mobile.login.main.domain.usecase.ProcessDeviceEnrollment
import org.dhis2.mobile.login.main.domain.usecase.ValidateServer
import org.dhis2.mobile.login.main.ui.navigation.AppLinkNavigation
import org.dhis2.mobile.login.main.ui.navigation.Navigator
import org.dhis2.mobile.login.main.ui.state.DatabaseImportState
import org.dhis2.mobile.login.main.ui.state.ServerValidationUiState

class LoginViewModel(
    val navigator: Navigator,
    private val getInitialScreen: GetInitialScreen,
    private val importDatabase: ImportDatabase,
    private val validateServer: ValidateServer,
    private val appLinkNavigation: AppLinkNavigation,
    private val getDeviceEnrollmentUrl: GetDeviceEnrollmentUrl,
    private val processDeviceEnrollment: ProcessDeviceEnrollment,
    private val loginUserWithOAuth: LoginUserWithOAuth,
    networkStatusProvider: NetworkStatusProvider,
) : ViewModel() {
    private val isNetworkOnline =
        networkStatusProvider.connectionStatus
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                false,
            )

    private val _serverValidationState = MutableStateFlow(ServerValidationUiState())
    val serverValidationState = _serverValidationState.asStateFlow()

    private val _importDatabaseState = MutableStateFlow<DatabaseImportState?>(null)
    val importDatabaseState = _importDatabaseState.asStateFlow()

    private var serverValidationJob: Job? = null

    init {
        launchUseCase {
            appLinkNavigation.appLink.collect { urlString ->
                handleOAuthCallbacks(urlString)
            }
        }
        goToInitialScreen()
    }

    private fun goToInitialScreen() {
        launchUseCase {
            val destination = getInitialScreen()
            navigator.navigate(
                destination = destination,
                navOptions = {
                    popUpTo(LoginScreenState.Loading) {
                        inclusive = true
                    }
                },
            )
        }
    }

    fun onValidateServer(serverUrl: String) {
        _serverValidationState.update {
            it.copy(
                currentServer = serverUrl,
                error = null,
                validationRunning = true,
            )
        }
        serverValidationJob =
            launchUseCase {
                val result =
                    withMinimumDuration { validateServer(serverUrl, isNetworkOnline.value) }
                when (result) {
                    is ServerValidationResult.Error -> {
                        _serverValidationState.update {
                            it.copy(
                                currentServer = serverUrl,
                                error = result.message,
                                validationRunning = false,
                            )
                        }
                    }

                    is ServerValidationResult.Legacy -> {
                        navigator.navigate(
                            destination =
                                LegacyLogin(
                                    serverName = result.serverName,
                                    allowRecovery = result.allowRecovery,
                                    selectedServer = serverUrl,
                                    selectedServerFlag = result.countryFlag,
                                    selectedUsername = null,
                                ),
                        )
                        stopValidation()
                    }

                    is ServerValidationResult.Oauth -> {
                        getDeviceEnrollmentUrl(serverUrl).fold(
                            onSuccess = { enrollmentURL ->
                                navigator.navigate(OauthLogin(enrollmentURL))
                            },
                            onFailure = { error ->
                                _serverValidationState.update {
                                    it.copy(
                                        currentServer = serverUrl,
                                        error = error.message,
                                        validationRunning = false,
                                    )
                                }
                            },
                        )
                        stopValidation()
                    }
                }
            }
    }

    fun cancelServerValidation() {
        serverValidationJob?.cancel()
        stopValidation()
    }

    private fun stopValidation() {
        _serverValidationState.update { it.copy(validationRunning = false) }
    }

    private fun handleOAuthCallbacks(urlString: String) {
        // First check if there is any error
        val error = urlString.substringAfter("error=", "").substringBefore('&')
        if (error.isNotEmpty()) {
            _serverValidationState.update {
                it.copy(
                    error = error,
                    validationRunning = false,
                )
            }
            return
        }

        // Check if there is a device enrollment callback
        val iat = urlString.substringAfter("iat=", "").substringBefore('&')
        if (iat.isNotEmpty()) {
            registerDevice(
                serverURL = serverValidationState.value.currentServer,
                iat = iat,
            )
            return
        }

        // Check if there is a login callback with the authorization code
        val code = urlString.substringAfter("code=", "").substringBefore('&')
        if (code.isNotEmpty()) {
            val state = urlString.substringAfter("state=").substringBefore('&')
            loginOAuth(
                serverUrl = serverValidationState.value.currentServer,
                code = code,
            )
            return
        }

        _serverValidationState.update {
            it.copy(
                error = "Unknown error",
                validationRunning = false,
            )
        }
    }

    private fun registerDevice(
        serverURL: String,
        iat: String,
    ) {
        launchUseCase {
            processDeviceEnrollment(
                DeviceEnrollmentInfo(
                    iat = iat,
                    serverURL = serverURL,
                ),
            ).fold(
                onSuccess = {
                    // Open Consent permissions
                    navigator.navigate(OauthLogin(it))
                },
                onFailure = { error ->
                    _serverValidationState.update {
                        it.copy(
                            error = error.message,
                            validationRunning = false,
                        )
                    }
                },
            )
        }
    }

    fun loginOAuth(
        serverUrl: String,
        code: String,
    ) {
        launchUseCase {
            val result =
                withMinimumDuration {
                    loginUserWithOAuth(
                        serverUrl = serverUrl,
                        code = code,
                    )
                }
            when (result) {
                is LoginResult.Success -> {
                    // TODO build post login actions
                    navigator.navigateToSync()
                }
                is LoginResult.Error ->
                    _serverValidationState.update {
                        it.copy(
                            error = "Unknown error",
                            validationRunning = false,
                        )
                    }
            }
        }
    }

    fun onOauthLoginCancelled() {
        navigateUp()
    }

    fun onRecoveryCancelled() {
        navigateUp()
    }

    fun onBackToManageAccounts() {
        navigateUp()
    }

    private fun navigateUp() {
        launchUseCase {
            navigator.navigateUp()
        }
    }

    fun importDb(path: String) {
        launchUseCase {
            importDatabase(path)
                .fold(
                    onSuccess = {
                        _importDatabaseState.update {
                            DatabaseImportState.OnSuccess
                        }
                        goToInitialScreen()
                    },
                    onFailure = { error ->
                        _importDatabaseState.update {
                            error.message?.let {
                                DatabaseImportState.OnFailure(it)
                            }!!
                        }
                    },
                )
        }
    }
}
