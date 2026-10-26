package org.dhis2.mobile.app.splash.ui.state

import org.dhis2.mobile.app.splash.domain.model.StartDestination

sealed interface SplashUiState {
    data object Loading : SplashUiState

    data class Ready(
        val destination: StartDestination,
    ) : SplashUiState
}
