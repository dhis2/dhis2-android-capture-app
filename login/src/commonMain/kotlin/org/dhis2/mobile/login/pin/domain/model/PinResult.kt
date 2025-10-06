package org.dhis2.mobile.login.pin.domain.model

/**
 * Represents the result of a PIN validation operation.
 */
sealed class PinResult {
    /**
     * PIN validation was successful.
     */
    data object Success : PinResult()

    /**
     * PIN validation failed.
     * @param attemptsLeft Number of attempts remaining before forced logout.
     */
    data class Failed(
        val attemptsLeft: Int,
    ) : PinResult()

    /**
     * Too many failed attempts - user needs to recover PIN.
     */
    data object TooManyAttempts : PinResult()

    /**
     * No PIN is stored in the system.
     */
    data object NoPinStored : PinResult()
}
