package org.dhis2.mobile.login.main.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import org.dhis2.mobile.login.main.domain.model.LoginScreenState
import org.dhis2.mobile.login.main.domain.usecase.GetInitialScreen
import org.dhis2.mobile.login.main.ui.navigation.Navigator

class LoginViewModel(
    val navigator: Navigator,
    val getInitialScreen: GetInitialScreen,
) : ViewModel() {

    private val _currentScreen = MutableStateFlow<LoginScreenState>(LoginScreenState.Loading)
    val currentScreen = _currentScreen.onStart {
        goToInitialScreen()
    }.shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
    )

    private fun goToInitialScreen() {
        viewModelScope.launch {
            _currentScreen.emit(getInitialScreen())
        }
    }

    fun onValidateServer() {
        viewModelScope.launch {
            navigator.navigate(LoginScreenState.LegacyLogin("", ""))
        }
    }
}
