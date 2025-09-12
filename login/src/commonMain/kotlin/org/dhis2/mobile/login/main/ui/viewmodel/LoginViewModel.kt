package org.dhis2.mobile.login.main.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dhis2.mobile.commons.extensions.withMinimumDuration
import org.dhis2.mobile.login.main.domain.model.LoginScreenState
import org.dhis2.mobile.login.main.domain.model.ServerValidationResult
import org.dhis2.mobile.login.main.domain.usecase.GetInitialScreen
import org.dhis2.mobile.login.main.domain.usecase.ValidateServer
import org.dhis2.mobile.login.main.ui.navigation.AppLinkNavigation
import org.dhis2.mobile.login.main.ui.navigation.Navigator

class LoginViewModel(
    val navigator: Navigator,
    val getInitialScreen: GetInitialScreen,
    val validateServer: ValidateServer,
    private val appLinkNavigation: AppLinkNavigation,
) : ViewModel() {
    private val _currentScreen = MutableStateFlow<LoginScreenState>(LoginScreenState.Loading)
    val currentScreen =
        _currentScreen
            .onStart {
                goToInitialScreen()
            }.shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
            )

    private var serverValidationJob: Job? = null
    private val redirectUri = "https://vgarciabnz.github.io"

    init {
        viewModelScope.launch {
            appLinkNavigation.appLink.collect { urlString ->
                handleAppLink(urlString)
            }
        }
    }

    private fun goToInitialScreen() {
        viewModelScope.launch {
            _currentScreen.emit(getInitialScreen())
        }
    }

    fun onValidateServer(serverUrl: String) {
        _currentScreen.update {
            (it as? LoginScreenState.ServerValidation)?.copy(
                validationRunning = true,
            ) ?: it
        }
        serverValidationJob =
            viewModelScope.launch {
                val result = withMinimumDuration { validateServer(serverUrl) }
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

    private fun handleAppLink(urlString: String) {
        if (urlString.startsWith(redirectUri)) {
            val code = urlString.substringAfter("code=").substringBefore('&')
            if (code.isNotEmpty()) {
                // TODO: Use the authorization code to get a token and log in, then show statistics screen
            } else {
                val error = urlString.substringAfter("error=").substringBefore('&')
                _currentScreen.update {
                    (it as? LoginScreenState.ServerValidation)?.copy(
                        error = error,
                        validationRunning = false,
                    ) ?: it
                }
            }
        }
    }

    fun onOauthLoginCancelled() {
        viewModelScope.launch {
            navigator.navigateUp()
        }
    }
}
