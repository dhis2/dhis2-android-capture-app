package org.dhis2.mobile.app.splash.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.dhis2.mobile.app.splash.domain.model.StartDestination
import org.dhis2.mobile.app.splash.domain.usecase.GetStartDestination
import org.dhis2.mobile.app.splash.ui.state.SplashUiState
import org.dhis2.mobile.commons.domain.invoke
import org.dhis2.mobile.commons.extensions.launchUseCase
import org.dhis2.mobile.commons.extensions.withMinimumDuration

private const val SPLASH_MINIMUM_DURATION_MILLIS = 2000L

class SplashViewModel(
    private val getStartDestination: GetStartDestination,
) : ViewModel() {
    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        launchUseCase {
            val destination =
                withMinimumDuration(SPLASH_MINIMUM_DURATION_MILLIS) {
                    getStartDestination()
                }.getOrDefault(StartDestination.Login)
            _uiState.value = SplashUiState.Ready(destination)
        }
    }
}
