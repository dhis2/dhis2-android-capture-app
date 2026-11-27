package org.dhis2.mobile.login.pin.data

import kotlinx.coroutines.withContext
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.commons.error.DomainErrorMapper
import org.dhis2.mobile.commons.error.withDomainErrors
import org.dhis2.mobile.commons.providers.PreferenceProvider
import org.dhis2.mobile.login.pin.domain.model.PinVerification
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.maintenance.D2Error

/**
 * Android implementation of SessionRepository using DHIS2 SDK.
 * Handles PIN storage and session management using D2 dataStore and preferences.
 * The PIN is stored as a hash produced by [PinHasher], never in plaintext.
 */
class SessionRepositoryImpl(
    private val d2: D2,
    private val preferenceProvider: PreferenceProvider,
    private val domainErrorMapper: DomainErrorMapper,
    private val dispatcher: Dispatcher,
    private val pinHasher: PinHasher,
) : SessionRepository {
    companion object {
        private const val PIN_KEY = "pin"
        private const val PREF_SESSION_LOCKED = "SessionLocked"
    }

    override suspend fun savePin(pin: String) =
        withContext(dispatcher.io) {
            domainErrorMapper.withDomainErrors {
                storePinHash(pin)
            }
        }

    override suspend fun verifyPin(pin: String): PinVerification =
        withContext(dispatcher.io) {
            domainErrorMapper.withDomainErrors {
                val storedValue =
                    d2
                        .dataStoreModule()
                        .localDataStore()
                        .value(PIN_KEY)
                        .blockingGet()
                        ?.value()
                        ?: return@withDomainErrors PinVerification.NoPinStored

                when (val verification = pinHasher.verify(pin, storedValue)) {
                    is PinHasher.Verification.Match -> {
                        if (verification.needsUpgrade) {
                            storePinHash(pin)
                        }
                        PinVerification.Match
                    }

                    PinHasher.Verification.Mismatch -> PinVerification.Mismatch
                    PinHasher.Verification.KeyMissing -> PinVerification.NoPinStored
                }
            }
        }

    private fun storePinHash(pin: String) {
        d2
            .dataStoreModule()
            .localDataStore()
            .value(PIN_KEY)
            .blockingSet(pinHasher.hash(pin))
    }

    override suspend fun deletePin() =
        withContext(dispatcher.io) {
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

    override suspend fun setSessionLocked(locked: Boolean) =
        withContext(dispatcher.io) {
            try {
                preferenceProvider.setValue(PREF_SESSION_LOCKED, locked)
            } catch (e: Exception) {
                throw e
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

    override suspend fun logout() =
        withContext(dispatcher.io) {
            try {
                d2.userModule().blockingLogOut()
            } catch (d2Error: D2Error) {
                throw domainErrorMapper.mapToDomainError(d2Error)
            }
        }
}
