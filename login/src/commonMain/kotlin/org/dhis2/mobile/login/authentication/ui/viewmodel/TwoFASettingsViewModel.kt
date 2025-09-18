package org.dhis2.mobile.login.authentication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.commons.network.NetworkStatusProvider
import org.dhis2.mobile.login.authentication.domain.usecase.DisableTwoFA
import org.dhis2.mobile.login.authentication.domain.usecase.EnableTwoFA
import org.dhis2.mobile.login.authentication.domain.usecase.GetTwoFAStatus
import org.dhis2.mobile.login.authentication.ui.mapper.TwoFAUiStateMapper
import org.dhis2.mobile.login.authentication.ui.state.TwoFAUiState
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState

open class TwoFASettingsViewModel(
    private val getTwoFAStatus: GetTwoFAStatus,
    private val enableTwoFA: EnableTwoFA,
    private val disableTwoFA: DisableTwoFA,
    private val mapper: TwoFAUiStateMapper,
    private val networkStatusProvider: NetworkStatusProvider,
    private val dispatchers: Dispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow<TwoFAUiState>(TwoFAUiState.Checking)
    val uiState: StateFlow<TwoFAUiState> =
        _uiState
            .asStateFlow()
            .onStart {
                checkTwoFAStatus()
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000L),
                TwoFAUiState.Checking,
            )

    private val isNetworkOnline =
        networkStatusProvider.connectionStatus
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                false,
            )

    private suspend fun checkTwoFAStatus() {
        val twoFAStatus = getTwoFAStatus()
        _uiState.update {
            mapper.mapToUiState(twoFAStatus)
        }
    }

    fun retry() {
        viewModelScope.launch(dispatchers.io) {
            _uiState.update {
                TwoFAUiState.Checking
            }
            delay(1000)
            checkTwoFAStatus()
        }
    }

    fun enableTwoFA(code: String) {
        viewModelScope.launch(dispatchers.io) {
            _uiState.update {
                (it as? TwoFAUiState.Enable)?.copy(
                    isEnabling = true,
                ) ?: it
            }
            yield()
            enableTwoFA.invoke(code, isNetworkOnline.value).fold(
                onSuccess = {
                    checkTwoFAStatus()
                },
                onFailure = { throwable ->
                    _uiState.update {
                        (it as TwoFAUiState.Enable).copy(
                            isEnabling = false,
                            enableErrorMessage = throwable.message,
                        )
                    }
                },
            )
        }
    }

    fun updateAuthCode(authCode: String) {
        viewModelScope.launch {
            _uiState.update {
                (it as? TwoFAUiState.Disable)?.copy(
                    state =
                        if (it.disableErrorMessage != null && authCode.isEmpty()) {
                            InputShellState.ERROR
                        } else if (it.isDisabling) {
                            InputShellState.DISABLED
                        } else {
                            InputShellState.UNFOCUSED
                        },
                ) ?: it
            }
        }
    }

    fun disableTwoFA(code: String) {
        viewModelScope.launch(dispatchers.io) {
            _uiState.update {
                (it as? TwoFAUiState.Disable)?.copy(
                    isDisabling = true,
                ) ?: it
            }
            yield()
            disableTwoFA.invoke(code, isNetworkOnline.value).fold(
                onSuccess = {
                    checkTwoFAStatus()
                },
                onFailure = { throwable ->
                    _uiState.update {
                        (it as TwoFAUiState.Disable).copy(
                            state = InputShellState.ERROR,
                            isDisabling = false,
                            disableErrorMessage = throwable.message,
                        )
                    }
                },
            )
        }
    }
}
