package org.dhis2.mobile.login.pin.domain.model

/**
 * Outcome of checking a PIN against the stored one.
 */
sealed interface PinVerification {
    data object Match : PinVerification

    data object Mismatch : PinVerification

    data object NoPinStored : PinVerification
}
