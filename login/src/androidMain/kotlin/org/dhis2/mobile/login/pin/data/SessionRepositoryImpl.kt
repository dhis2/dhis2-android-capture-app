package org.dhis2.mobile.login.pin.data

import kotlinx.coroutines.withContext
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.commons.error.DomainErrorMapper
import org.dhis2.mobile.commons.error.withDomainErrors
import org.dhis2.mobile.commons.providers.PreferenceProvider
import org.hisp.dhis.android.core.D2

/**
 * Android implementation of SessionRepository using DHIS2 SDK.
 * Handles PIN storage and session management using D2 dataStore and preferences.
 */
class SessionRepositoryImpl(
    private val d2: D2,
    private val preferenceProvider: PreferenceProvider,
    private val domainErrorMapper: DomainErrorMapper,
    private val dispatcher: Dispatcher,
) : SessionRepository {
    companion object {
        private const val PIN_KEY = "pin"
        private const val PREF_SESSION_LOCKED = "SessionLocked"
    }

    override suspend fun savePin(pin: String) {
        withContext(dispatcher.io) {
            domainErrorMapper.withDomainErrors {
                d2
                    .dataStoreModule()
                    .localDataStore()
                    .value(PIN_KEY)
                    .blockingSet(pin)
            }
        }
    }

    override suspend fun getStoredPin(): String? =
        withContext(dispatcher.io) {
            domainErrorMapper.withDomainErrors {
                d2
                    .dataStoreModule()
                    .localDataStore()
                    .value(PIN_KEY)
                    .blockingGet()
                    ?.value()
            }
        }

    override suspend fun deletePin() {
        withContext(dispatcher.io) {
            domainErrorMapper.withDomainErrors {
                d2
                    .dataStoreModule()
                    .localDataStore()
                    .value(PIN_KEY)
                    .blockingDeleteIfExist()
            }
        }
    }

    override suspend fun setSessionLocked(locked: Boolean) {
        withContext(dispatcher.io) {
            preferenceProvider.setValue(PREF_SESSION_LOCKED, locked)
        }
    }

    override suspend fun isSessionLocked(): Boolean =
        withContext(dispatcher.io) {
            try {
                d2
                    .dataStoreModule()
                    .localDataStore()
                    .value(PIN_KEY)
                    .blockingExists()
            } catch (_: Exception) {
                false
            }
        }

    override suspend fun logout() {
        withContext(dispatcher.io) {
            domainErrorMapper.withDomainErrors {
                d2.userModule().blockingLogOut()
            }
        }
    }
}
