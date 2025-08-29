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
import org.dhis2.mobile.login.authentication.domain.usecase.DisableTwoFA
import org.dhis2.mobile.login.authentication.domain.usecase.EnableTwoFA
import org.dhis2.mobile.login.authentication.domain.usecase.GetTwoFAStatus
import org.dhis2.mobile.login.authentication.ui.mapper.TwoFAUiStateMapper
import org.dhis2.mobile.login.authentication.ui.state.TwoFAUiState

open class TwoFASettingsViewModel(
    private val getTwoFAStatus: GetTwoFAStatus,
    private val enableTwoFA: EnableTwoFA,
    private val disableTwoFA: DisableTwoFA,
    private val mapper: TwoFAUiStateMapper,
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

    private fun checkTwoFAStatus(check: Boolean = true) {
        viewModelScope.launch {
            if (check) {
                _uiState.update {
                    TwoFAUiState.Checking
                }
                delay(1000)
            }
            val twoFAStatus = getTwoFAStatus()
            _uiState.update {
                mapper.mapToUiState(twoFAStatus)
            }
        }
    }

    fun retry() {
        checkTwoFAStatus()
    }

    fun enableTwoFA(code: String) {
        viewModelScope.launch {
            _uiState.update {
                (it as? TwoFAUiState.Enable)?.copy(
                    isEnabling = true,
                ) ?: it
            }
            enableTwoFA.invoke(code).fold(
                onSuccess = {
                    checkTwoFAStatus(false)
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

    fun disableTwoFA(code: String) {
        viewModelScope.launch {
            _uiState.update {
                (it as? TwoFAUiState.Disable)?.copy(
                    isDisabling = true,
                ) ?: it
            }
            disableTwoFA.invoke(code).fold(
                onSuccess = {
                    checkTwoFAStatus(false)
                },
                onFailure = { throwable ->
                    _uiState.update {
                        (it as TwoFAUiState.Disable).copy(
                            isDisabling = false,
                            disableErrorMessage = throwable.message,
                        )
                    }
                },
            )
        }
    }
}
