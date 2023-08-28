package org.dhis2.utils.session

import org.dhis2.commons.prefs.Preference
import org.dhis2.commons.prefs.PreferenceProvider
import org.hisp.dhis.android.core.D2
import timber.log.Timber

class PinPresenter(
    val view: PinView,
    val preferenceProvider: PreferenceProvider,
    val d2: D2,
) {

    fun unlockSession(
        pin: String,
        attempts: Int,
        onPinCorrect: () -> Unit,
        onError: () -> Unit,
        onTwoManyAttempts: () -> Unit,
    ) {
        val pinStored = d2.dataStoreModule()
            .localDataStore()
            .value(Preference.PIN)
            .blockingGet()?.value()
        when {
            pinStored == pin -> {
                preferenceProvider.setValue(Preference.SESSION_LOCKED, true)
                onPinCorrect()
            }
            attempts < 2 -> onError()
            else -> onTwoManyAttempts()
        }
    }

    fun savePin(pin: String) {
        d2.dataStoreModule().localDataStore().value(Preference.PIN).blockingSet(pin)
        preferenceProvider.setValue(Preference.SESSION_LOCKED, true)
    }

    fun logOut() {
        try {
            d2.dataStoreModule().localDataStore().value(Preference.PIN).blockingDelete()
            d2.userModule().blockingLogOut()
            preferenceProvider.setValue(Preference.SESSION_LOCKED, false)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}
