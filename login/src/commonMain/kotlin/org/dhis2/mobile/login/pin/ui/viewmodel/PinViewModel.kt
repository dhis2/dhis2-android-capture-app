package org.dhis2.mobile.login.pin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.dhis2.mobile.commons.domain.invoke
import org.dhis2.mobile.login.pin.domain.model.PinError
import org.dhis2.mobile.login.pin.domain.model.PinState
import org.dhis2.mobile.login.pin.domain.model.ValidatePinInput
import org.dhis2.mobile.login.pin.domain.usecase.ForgotPinUseCase
import org.dhis2.mobile.login.pin.domain.usecase.SavePinUseCase
import org.dhis2.mobile.login.pin.domain.usecase.ValidatePinUseCase
import org.dhis2.mobile.login.pin.ui.components.PinMode
import org.dhis2.mobile.login.pin.ui.provider.PinResourceProvider

/**
 * ViewModel for managing PIN operations.
 * Coordinates between the UI and use cases for PIN creation, validation, and recovery.
 */
class PinViewModel(
    private val savePinUseCase: SavePinUseCase,
    private val validatePinUseCase: ValidatePinUseCase,
    private val forgotPinUseCase: ForgotPinUseCase,
    private val resourceProvider: PinResourceProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow<PinState>(PinState.Idle)
    val uiState: StateFlow<PinState> = _uiState.asStateFlow()

    private var pinAttempts = 0

    /**
     * Handles PIN completion based on the current mode.
     * @param pin The PIN entered by the user.
     * @param mode The current PIN mode (SET or ASK).
     */
    fun onPinComplete(
        pin: String,
        mode: PinMode,
    ) {
        when (mode) {
            PinMode.SET -> savePin(pin)
            PinMode.ASK -> validatePin(pin)
        }
    }

    /**
     * Saves a new PIN.
     * @param pin The PIN to save.
     */
    private fun savePin(pin: String) {
        viewModelScope.launch {
            _uiState.value = PinState.Loading
            savePinUseCase(pin)
                .onSuccess {
                    _uiState.value = PinState.Success
                }.onFailure { error ->
                    _uiState.value =
                        PinState.Error(
                            message = error.message ?: resourceProvider.getPinErrorSaveFailed(),
                        )
                }
        }
    }

    /**
     * Validates the entered PIN against the stored PIN.
     * @param pin The PIN to validate.
     */
    private fun validatePin(pin: String) {
        viewModelScope.launch {
            _uiState.value = PinState.Loading
            validatePinUseCase(ValidatePinInput(pin, pinAttempts)).fold(
                onSuccess = {
                    _uiState.value = PinState.Success
                    pinAttempts = 0
                },
                onFailure = { failure: Throwable ->
                    when (failure) {
                        is PinError.Failed -> {
                            pinAttempts++
                            _uiState.value =
                                PinState.Error(
                                    message = resourceProvider.getPinErrorIncorrect(),
                                    remainingAttempts = failure.attemptsLeft,
                                )
                        }

                        is PinError.TooManyAttempts -> {
                            _uiState.value = PinState.TooManyAttempts
                        }

                        is PinError.NoPinStored -> {
                            _uiState.value =
                                PinState.Error(
                                    message = resourceProvider.getPinErrorNoPinStored(),
                                )
                        }

                        else -> {
                            _uiState.value =
                                PinState.Error(
                                    message = failure.message ?: "Unknown error occurred",
                                )
                        }
                    }
                },
            )
        }
    }

    /**
     * Handles the forgot PIN action.
     * This will log out the user and clear the PIN.
     */
    fun onForgotPin() {
        viewModelScope.launch {
            _uiState.value = PinState.Loading
            forgotPinUseCase()
                .onSuccess {
                    _uiState.value = PinState.Dismissed
                }.onFailure { error ->
                    _uiState.value =
                        PinState.Error(
                            message = error.message ?: resourceProvider.getPinErrorResetFailed(),
                        )
                }
        }
    }

    /**
     * Resets the UI state to idle.
     * Useful for clearing error messages.
     */
    fun resetState() {
        _uiState.value = PinState.Idle
    }

    /**
     * Resets the attempt counter.
     * Should be called when the bottom sheet is dismissed.
     */
    fun resetAttempts() {
        pinAttempts = 0
    }
}
