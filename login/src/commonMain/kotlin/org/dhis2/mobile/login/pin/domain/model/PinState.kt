package org.dhis2.mobile.login.pin.domain.model

/**
 * Represents the different states of PIN operations in the UI.
 */
sealed class PinState {
    /**
     * Initial/Idle state - waiting for user interaction.
     */
    data object Idle : PinState()

    /**
     * PIN operation in progress.
     */
    data object Loading : PinState()

    /**
     * PIN validation or save was successful.
     */
    data object Success : PinState()

    /**
     * The PIN entry UI was dismissed.
     */
    data object Dismissed : PinState()

    /**
     * PIN validation failed with an error message.
     * @param message The error message to display.
     * @param remainingAttempts Number of remaining attempts before forced logout (null if not applicable).
     */
    data class Error(
        val message: String,
        val remainingAttempts: Int? = null,
    ) : PinState()

    /**
     * Too many failed attempts - will trigger forgot PIN flow.
     */
    data object TooManyAttempts : PinState()
}
