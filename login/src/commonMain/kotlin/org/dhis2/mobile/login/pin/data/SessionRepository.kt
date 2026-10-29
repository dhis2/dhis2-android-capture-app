package org.dhis2.mobile.login.pin.data

import org.dhis2.mobile.login.pin.domain.model.PinVerification

/**
 * Repository interface for managing PIN and session operations.
 * Platform-specific implementations should use the DHIS2 SDK for data operations.
 */
interface SessionRepository {
    /**
     * Saves a PIN to secure storage. Only a salted hash of it is stored, never the PIN itself.
     * @param pin The PIN to save.
     */
    suspend fun savePin(pin: String)

    /**
     * Checks a PIN against the stored one, so the stored value never leaves the data layer.
     * A PIN stored in plaintext by a previous version is replaced by its hash when it matches.
     * @param pin The PIN to check.
     * @return Whether the PIN matches, or [PinVerification.NoPinStored] if there is none to check.
     */
    suspend fun verifyPin(pin: String): PinVerification

    /**
     * Deletes the stored PIN from secure storage.
     */
    suspend fun deletePin()

    /**
     * Marks the session as locked.
     * @param locked True to lock the session, false to unlock it.
     */
    suspend fun setSessionLocked(locked: Boolean)

    /**
     * Checks if the session is currently locked.
     * @return True if the session is locked, false otherwise.
     */
    suspend fun isSessionLocked(): Boolean

    /**
     * Logs out the current user and clears session data.
     */
    suspend fun logout()
}
