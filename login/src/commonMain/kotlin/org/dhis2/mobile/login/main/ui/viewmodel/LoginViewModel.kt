package org.dhis2.mobile.login.main.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dhis2.mobile.commons.extensions.withMinimumDuration
import org.dhis2.mobile.commons.network.NetworkStatusProvider
import org.dhis2.mobile.login.main.domain.model.LoginScreenState
import org.dhis2.mobile.login.main.domain.model.ServerValidationResult
import org.dhis2.mobile.login.main.domain.usecase.GetInitialScreen
import org.dhis2.mobile.login.main.domain.usecase.ValidateServer
import org.dhis2.mobile.login.main.ui.navigation.Navigator

class LoginViewModel(
    val navigator: Navigator,
    val getInitialScreen: GetInitialScreen,
    val validateServer: ValidateServer,
    val networkStatusProvider: NetworkStatusProvider,
) : ViewModel() {

    private val isNetworkOnline = networkStatusProvider.connectionStatus
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            false,
        )
    private val _currentScreen = MutableStateFlow<LoginScreenState>(LoginScreenState.Loading)
    val currentScreen = _currentScreen.asSharedFlow()

    private var serverValidationJob: Job? = null

    private fun goToInitialScreen() {
        viewModelScope.launch {
            navigator.navigate(destination = getInitialScreen())
        }
    }

    init {
        goToInitialScreen()
    }

    fun setCurrentScreen(screen: LoginScreenState) {
        viewModelScope.launch {
            _currentScreen.update { screen }
        }
    }

    fun onValidateServer(serverUrl: String) {
        _currentScreen.update {
            (it as? LoginScreenState.ServerValidation)?.copy(
                validationRunning = true,
            ) ?: it
        }
        serverValidationJob = viewModelScope.launch {
            val result = withMinimumDuration { validateServer(serverUrl, isNetworkOnline.value) }
            when (result) {
                is ServerValidationResult.Error ->
                    _currentScreen.update {
                        (it as? LoginScreenState.ServerValidation)?.copy(
                            currentServer = serverUrl,
                            error = result.message,
                            validationRunning = false,
                        ) ?: it
                    }

                ServerValidationResult.Legacy -> {
                    updateIsValidationRunning()
                    navigator.navigate(LoginScreenState.LegacyLogin(serverUrl, ""))
                }

                ServerValidationResult.Oauth -> {
                    updateIsValidationRunning()
                    navigator.navigate(LoginScreenState.OauthLogin(serverUrl))
                }
            }
        }
    }

    fun cancelServerValidation() {
        serverValidationJob?.cancel()
        updateIsValidationRunning()
    }

    private fun updateIsValidationRunning() {
        _currentScreen.update {
            (it as? LoginScreenState.ServerValidation)?.copy(
                validationRunning = serverValidationJob?.isActive == true,
            ) ?: it
        }
    }
}
