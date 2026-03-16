package org.dhis2.mobile.login.pin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.dhis2.mobile.commons.domain.invoke
import org.dhis2.mobile.login.pin.domain.model.PinError
import org.dhis2.mobile.login.pin.domain.model.PinMode
import org.dhis2.mobile.login.pin.domain.model.ValidatePinInput
import org.dhis2.mobile.login.pin.domain.usecase.ForgotPinUseCase
import org.dhis2.mobile.login.pin.domain.usecase.SavePinUseCase
import org.dhis2.mobile.login.pin.domain.usecase.ValidatePinUseCase
import org.dhis2.mobile.login.pin.ui.provider.PinResourceProvider
import org.dhis2.mobile.login.pin.ui.state.PinUiState

/**
 * ViewModel for managing PIN operations.
 * Coordinates between the UI and use cases for PIN creation, validation, and recovery.
 *
 * @param mode The PIN mode (SET for creation, ASK for verification).
 */
class PinViewModel(
    private val mode: PinMode,
    private val savePinUseCase: SavePinUseCase,
    private val validatePinUseCase: ValidatePinUseCase,
    private val forgotPinUseCase: ForgotPinUseCase,
    private val resourceProvider: PinResourceProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PinUiState())
    val uiState: StateFlow<PinUiState> = _uiState.asStateFlow()

    private var pinAttempts = 0
    private var currentPin = ""

    init {
        viewModelScope.launch {
            _uiState.value =
                _uiState.value.copy(
                    title = resourceProvider.getPinTitle(mode),
                    subtitle = resourceProvider.getPinSubtitle(mode),
                    primaryButtonText = resourceProvider.getPrimaryButtonText(mode),
                    secondaryButtonText = resourceProvider.getSecondaryButtonText(mode),
                )
        }
    }

    /**
     * Updates the current PIN value and recomputes button enablement.
     * Clears any existing error message when the user starts typing.
     *
     * @param pin The current PIN value from the input field.
     */
    fun onPinChanged(pin: String) {
        currentPin = pin
        _uiState.value =
            _uiState.value.copy(
                errorMessage = null,
                primaryButtonIsEnabled = pin.replace("-", "").length == _uiState.value.pinLength,
            )
    }

    /**
     * Handles PIN completion based on the current mode.
     */
    fun onPinComplete() {
        val pin = currentPin.replace("-", "")
        when (mode) {
            PinMode.SET -> savePin(pin)
            PinMode.ASK -> validatePin(pin)
        }
    }

    private fun savePin(pin: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, primaryButtonIsEnabled = false)
            savePinUseCase(pin)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                }.onFailure { error ->
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            errorMessage = error.message ?: resourceProvider.getPinErrorSaveFailed(),
                            primaryButtonIsEnabled = currentPin.replace("-", "").length == _uiState.value.pinLength,
                        )
                }
        }
    }

    private fun validatePin(pin: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, primaryButtonIsEnabled = false)
            validatePinUseCase(ValidatePinInput(pin, pinAttempts)).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                    pinAttempts = 0
                },
                onFailure = { failure ->
                    when (failure) {
                        is PinError.Failed -> {
                            pinAttempts++
                            val message = resourceProvider.getPinErrorIncorrect()
                            val fullMessage =
                                resourceProvider.getPinErrorWithAttempts(message, failure.attemptsLeft)
                            _uiState.value =
                                _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = fullMessage,
                                    primaryButtonIsEnabled = currentPin.replace("-", "").length == _uiState.value.pinLength,
                                )
                        }

                        is PinError.TooManyAttempts -> {
                            _uiState.value =
                                _uiState.value.copy(isLoading = false, isTooManyAttempts = true)
                        }

                        is PinError.NoPinStored -> {
                            _uiState.value =
                                _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = resourceProvider.getPinErrorNoPinStored(),
                                    primaryButtonIsEnabled = currentPin.replace("-", "").length == _uiState.value.pinLength,
                                )
                        }

                        else -> {
                            _uiState.value =
                                _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = failure.message ?: "Unknown error occurred",
                                    primaryButtonIsEnabled = currentPin.replace("-", "").length == _uiState.value.pinLength,
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
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, primaryButtonIsEnabled = false)
            forgotPinUseCase()
                .onSuccess {
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            isDismissed = true,
                            isTooManyAttempts = false,
                        )
                }.onFailure { error ->
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            errorMessage = error.message ?: resourceProvider.getPinErrorResetFailed(),
                            primaryButtonIsEnabled = currentPin.replace("-", "").length == _uiState.value.pinLength,
                        )
                }
        }
    }

    /**
     * Resets transient UI state flags (error, success, dismissed, tooManyAttempts).
     * Useful when the dialog is dismissed or re-shown.
     */
    fun resetState() {
        _uiState.value =
            _uiState.value.copy(
                errorMessage = null,
                isSuccess = false,
                isDismissed = false,
                isTooManyAttempts = false,
            )
    }

    /**
     * Resets the attempt counter.
     * Should be called when the bottom sheet is dismissed.
     */
    fun resetAttempts() {
        pinAttempts = 0
    }
}
