package org.dhis2.mobile.login.pin.data

import org.dhis2.mobile.commons.providers.PreferenceProvider
import org.hisp.dhis.android.core.D2
import timber.log.Timber

/**
 * Android implementation of SessionRepository using DHIS2 SDK.
 * Handles PIN storage and session management using D2 dataStore and preferences.
 */
class SessionRepositoryImpl(
    private val d2: D2,
    private val preferenceProvider: PreferenceProvider,
) : SessionRepository {
    companion object {
        private const val PIN_KEY = "pin"
        private const val PREF_SESSION_LOCKED = "SessionLocked"
        private const val PREF_PIN_ENABLED = "PinEnabled"
    }

    override suspend fun savePin(pin: String) {
        try {
            d2
                .dataStoreModule()
                .localDataStore()
                .value(PIN_KEY)
                .blockingSet(pin)
        } catch (e: Exception) {
            Timber.e(e, "Failed to save PIN")
            throw e
        }
    }

    override suspend fun getStoredPin(): String? =
        try {
            d2
                .dataStoreModule()
                .localDataStore()
                .value(PIN_KEY)
                .blockingGet()
                ?.value()
        } catch (e: Exception) {
            Timber.e(e, "Failed to retrieve PIN")
            null
        }

    override suspend fun deletePin() {
        try {
            d2
                .dataStoreModule()
                .localDataStore()
                .value(PIN_KEY)
                .blockingDeleteIfExist()
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete PIN")
            throw e
        }
    }

    override suspend fun setSessionLocked(locked: Boolean) {
        try {
            preferenceProvider.setValue(PREF_SESSION_LOCKED, locked)
        } catch (e: Exception) {
            Timber.e(e, "Failed to set session locked state")
            throw e
        }
    }

    override suspend fun isSessionLocked(): Boolean =
        try {
            preferenceProvider.getBoolean(PREF_SESSION_LOCKED, false)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get session locked state")
            false
        }

    override suspend fun logout() {
        try {
            d2.userModule().blockingLogOut()
        } catch (e: Exception) {
            Timber.e(e, "Failed to logout")
            throw e
        }
    }

    override suspend fun setPinEnabled(enabled: Boolean) {
        try {
            preferenceProvider.setValue(PREF_PIN_ENABLED, enabled)
        } catch (e: Exception) {
            Timber.e(e, "Failed to set PIN enabled state")
            throw e
        }
    }
}
