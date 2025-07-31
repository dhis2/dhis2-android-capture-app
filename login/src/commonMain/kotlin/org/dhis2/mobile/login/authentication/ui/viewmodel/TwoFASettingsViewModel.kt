package org.dhis2.mobile.login.authentication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.dhis2.mobile.login.authentication.domain.usecase.GetTwoFAStatus
import org.dhis2.mobile.login.authentication.ui.mapper.TwoFAUiStateMapper
import org.dhis2.mobile.login.authentication.ui.state.TwoFAUiState

open class TwoFASettingsViewModel(
    private val getTwoFAStatus: GetTwoFAStatus,
    private val mapper: TwoFAUiStateMapper,
) : ViewModel() {

    private val _uiState = MutableStateFlow<TwoFAUiState>(TwoFAUiState.Checking)
    val uiState: StateFlow<TwoFAUiState> = _uiState.asStateFlow()

    init {
        checkTwoFAStatus()
    }

    fun checkTwoFAStatus() {
        viewModelScope.launch {
            _uiState.value = TwoFAUiState.Checking

            getTwoFAStatus()
                .collect { status ->
                    _uiState.value = mapper.mapToUiState(status)
                }
        }
    }

    fun retry() {
        checkTwoFAStatus()
    }
}
