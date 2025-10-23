package org.dhis2.mobile.login.pin.data

import org.dhis2.mobile.commons.error.DomainErrorMapper
import org.dhis2.mobile.commons.providers.PreferenceProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.maintenance.D2Error

/**
 * Android implementation of SessionRepository using DHIS2 SDK.
 * Handles PIN storage and session management using D2 dataStore and preferences.
 */
class SessionRepositoryImpl(
    private val d2: D2,
    private val preferenceProvider: PreferenceProvider,
    private val domainErrorMapper: DomainErrorMapper,
) : SessionRepository {
    companion object {
        private const val PIN_KEY = "pin"
        private const val PREF_SESSION_LOCKED = "SessionLocked"
    }

    override suspend fun savePin(pin: String) {
        try {
            d2
                .dataStoreModule()
                .localDataStore()
                .value(PIN_KEY)
                .blockingSet(pin)
        } catch (d2Error: D2Error) {
            throw domainErrorMapper.mapToDomainError(d2Error)
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
        } catch (d2Error: D2Error) {
            throw domainErrorMapper.mapToDomainError(d2Error)
        }

    override suspend fun deletePin() {
        try {
            d2
                .dataStoreModule()
                .localDataStore()
                .value(PIN_KEY)
                .blockingDeleteIfExist()
        } catch (d2Error: D2Error) {
            throw domainErrorMapper.mapToDomainError(d2Error)
        }
    }

    override suspend fun setSessionLocked(locked: Boolean) {
        try {
            preferenceProvider.setValue(PREF_SESSION_LOCKED, locked)
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun isSessionLocked(): Boolean =
        try {
            preferenceProvider.getBoolean(PREF_SESSION_LOCKED, false)
        } catch (_: Exception) {
            false
        }

    override suspend fun logout() {
        try {
            d2.userModule().blockingLogOut()
        } catch (d2Error: D2Error) {
            throw domainErrorMapper.mapToDomainError(d2Error)
        }
    }
}
