package org.dhis2.mobile.login.pin.data

import org.dhis2.mobile.login.pin.domain.model.PinVerification

/**
 * In-memory [SessionRepository] for targets without the DHIS2 Android SDK.
 */
internal class FakeSessionRepository : SessionRepository {
    private var pin: String? = null
    private var sessionLocked = false

    override suspend fun savePin(pin: String) {
        this.pin = pin
    }

    override suspend fun verifyPin(pin: String): PinVerification =
        when (this.pin) {
            null -> PinVerification.NoPinStored
            pin -> PinVerification.Match
            else -> PinVerification.Mismatch
        }

    override suspend fun deletePin() {
        pin = null
    }

    override suspend fun setSessionLocked(locked: Boolean) {
        sessionLocked = locked
    }

    override suspend fun isSessionLocked(): Boolean = sessionLocked

    override suspend fun logout() {
        pin = null
        sessionLocked = false
    }
}
