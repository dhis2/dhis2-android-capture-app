package org.dhis2.mobile.login.pin.domain.model

data class ValidatePinInput(
    val pin: String,
    val currentAttempts: Int,
)
