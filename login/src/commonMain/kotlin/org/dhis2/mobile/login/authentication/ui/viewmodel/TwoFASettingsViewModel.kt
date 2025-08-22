package org.dhis2.mobile.login.authentication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dhis2.mobile.login.authentication.domain.model.TwoFAStatus
import org.dhis2.mobile.login.authentication.domain.usecase.DisableTwoFA
import org.dhis2.mobile.login.authentication.domain.usecase.EnableTwoFA
import org.dhis2.mobile.login.authentication.domain.usecase.GetTwoFASecretCode
import org.dhis2.mobile.login.authentication.domain.usecase.GetTwoFAStatus
import org.dhis2.mobile.login.authentication.ui.mapper.TwoFAUiStateMapper
import org.dhis2.mobile.login.authentication.ui.state.TwoFAUiState
import org.dhis2.mobile.login.authentication.ui.state.TwoFaEnableUiState

open class TwoFASettingsViewModel(
    private val getTwoFAStatus: GetTwoFAStatus,
    private val getTwoFASecretCode: GetTwoFASecretCode,
    private val enableTwoFA: EnableTwoFA,
    private val disableTwoFA: DisableTwoFA,
    private val mapper: TwoFAUiStateMapper,
) : ViewModel() {
    private val _uiState = MutableStateFlow<TwoFAUiState>(TwoFAUiState.Checking)
    val uiState: StateFlow<TwoFAUiState> = _uiState.asStateFlow()
        .onStart {
            checkTwoFAStatus()
        }.stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            TwoFAUiState.Checking,
        )

    private val _uiEnableState = MutableStateFlow<TwoFaEnableUiState>(TwoFaEnableUiState.Starting)
    val uiEnableState: StateFlow<TwoFaEnableUiState> = _uiEnableState.asStateFlow()
        .onStart {
            getSecretCode()
        }.stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            TwoFaEnableUiState.Starting,
        )

    private val _secretCode = MutableStateFlow("")
    val secretCode: StateFlow<String> = _secretCode.asStateFlow()
        .onStart {
            getSecretCode()
        }.stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            "",
        )

    private fun checkTwoFAStatus() {
        viewModelScope.launch {
            _uiState.value = TwoFAUiState.Checking

            val twoFAStatus = getTwoFAStatus()
            _uiState.update { mapper.mapToUiState(twoFAStatus) }
        }
    }

    fun getSecretCode() {
        viewModelScope.launch {
            getTwoFASecretCode().collect { code ->
                _secretCode.value = code
            }
        }
    }

    fun getSecretCode() {
        viewModelScope.launch {
            getTwoFASecretCode().collect { code ->
                _secretCode.value = code
            }
        }
    }

    fun retry() {
        checkTwoFAStatus()
    }

    fun enableTwoFA(code: String) {
        viewModelScope.launch {
            _uiEnableState.value = TwoFaEnableUiState.Enabling
            enableTwoFA.invoke(code).collect { status ->
                if (status) {
                    _uiState.value = mapper.mapToUiState(TwoFAStatus.Enabled())
                } else {
                    _uiEnableState.value = TwoFaEnableUiState.Failure
                }
            }
        }
    }

    fun disableTwoFA(code: String) {
        viewModelScope.launch {
            disableTwoFA.invoke(code).collect { status ->
                _uiState.value = mapper.mapToUiState(status)
            }
        }
    }
}
