package org.dhis2.mobile.login.pin.data

/**
 * In-memory [SessionRepository] for targets without the DHIS2 Android SDK.
 */
internal class FakeSessionRepository : SessionRepository {
    private var pin: String? = null
    private var sessionLocked = false

    override suspend fun savePin(pin: String) {
        this.pin = pin
    }

    override suspend fun getStoredPin(): String? = pin

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
