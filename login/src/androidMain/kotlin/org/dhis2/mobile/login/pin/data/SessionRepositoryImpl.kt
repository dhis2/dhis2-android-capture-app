package org.dhis2.mobile.login.pin.data

import kotlinx.coroutines.flow.firstOrNull
import org.dhis2.mobile.commons.network.NetworkStatusProvider
import org.dhis2.mobile.commons.providers.PreferenceProvider
import org.dhis2.mobile.commons.resources.D2ErrorMessageProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.maintenance.D2Error
import timber.log.Timber

/**
 * Android implementation of SessionRepository using DHIS2 SDK.
 * Handles PIN storage and session management using D2 dataStore and preferences.
 */
class SessionRepositoryImpl(
    private val d2: D2,
    private val preferenceProvider: PreferenceProvider,
    private val d2ErrorMessageProvider: D2ErrorMessageProvider,
    private val networkStatusProvider: NetworkStatusProvider,
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
            Timber.e(d2Error, "Failed to save PIN")
            val isNetworkAvailable = networkStatusProvider.connectionStatus.firstOrNull() ?: false
            val errorMessage = d2ErrorMessageProvider.getErrorMessage(d2Error, isNetworkAvailable)
            throw Exception(errorMessage, d2Error)
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
        } catch (d2Error: D2Error) {
            Timber.e(d2Error, "Failed to retrieve PIN")
            null
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
        } catch (d2Error: D2Error) {
            Timber.e(d2Error, "Failed to delete PIN")
            val isNetworkAvailable = networkStatusProvider.connectionStatus.firstOrNull() ?: false
            val errorMessage = d2ErrorMessageProvider.getErrorMessage(d2Error, isNetworkAvailable)
            throw Exception(errorMessage, d2Error)
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
            getStoredPin() != null
        } catch (e: Exception) {
            Timber.e(e, "Failed to get session locked state")
            false
        }

    override suspend fun logout() {
        try {
            d2.userModule().blockingLogOut()
        } catch (d2Error: D2Error) {
            Timber.e(d2Error, "Failed to logout")
            val isNetworkAvailable = networkStatusProvider.connectionStatus.firstOrNull() ?: false
            val errorMessage = d2ErrorMessageProvider.getErrorMessage(d2Error, isNetworkAvailable)
            throw Exception(errorMessage, d2Error)
        } catch (e: Exception) {
            Timber.e(e, "Failed to logout")
            throw e
        }
    }
}
