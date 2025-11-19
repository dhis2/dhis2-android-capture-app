package org.dhis2.mobile.login.pin.domain.model

sealed class PinError : Throwable() {
    data object NoPinStored : PinError()

    data object TooManyAttempts : PinError()

    data class Failed(
        val attemptsLeft: Int,
    ) : PinError()
}
