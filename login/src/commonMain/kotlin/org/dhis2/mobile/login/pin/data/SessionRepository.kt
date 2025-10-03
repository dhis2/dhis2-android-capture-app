package org.dhis2.mobile.login.pin.data

/**
 * Repository interface for managing PIN and session operations.
 * Platform-specific implementations should use the DHIS2 SDK for data operations.
 */
interface SessionRepository {
    /**
     * Saves a PIN to secure storage.
     * @param pin The PIN to save.
     */
    suspend fun savePin(pin: String)

    /**
     * Retrieves the stored PIN.
     * @return The stored PIN, or null if no PIN is stored.
     */
    suspend fun getStoredPin(): String?

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
