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
import org.dhis2.mobile.login.main.domain.model.LoginScreenState
import org.dhis2.mobile.login.main.domain.model.LoginScreenState.LoginCredentials
import org.dhis2.mobile.login.main.domain.model.ServerValidationResult
import org.dhis2.mobile.login.main.domain.usecase.GetInitialScreen
import org.dhis2.mobile.login.main.domain.usecase.ImportDatabase
import org.dhis2.mobile.login.main.domain.usecase.ValidateServer
import org.dhis2.mobile.login.main.ui.navigation.Navigator
import org.dhis2.mobile.login.main.ui.state.DatabaseImportState
import org.dhis2.mobile.login.main.ui.state.ServerValidationUiState

class LoginViewModel(
    val navigator: Navigator,
    private val getInitialScreen: GetInitialScreen,
    private val importDatabase: ImportDatabase,
    private val validateServer: ValidateServer,
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

                    is ServerValidationResult.Success -> {
                        navigator.navigate(
                            destination =
                                LoginCredentials(
                                    serverName = result.serverName,
                                    allowRecovery = result.allowRecovery,
                                    selectedServer = serverUrl,
                                    selectedServerFlag = result.countryFlag,
                                    selectedUsername = null,
                                    oAuthEnabled = result.oAuthEnabled,
                                ),
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
