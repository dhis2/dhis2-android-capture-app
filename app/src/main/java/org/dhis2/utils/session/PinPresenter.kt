package org.dhis2.utils.session

import org.dhis2.data.prefs.Preference
import org.dhis2.data.prefs.PreferenceProvider
import org.hisp.dhis.android.core.D2
import timber.log.Timber

class PinPresenter(
    val view: PinView,
    val preferenceProvider: PreferenceProvider,
    val d2: D2
) {

    fun unlockSession(pin: String): Boolean {
        return if (preferenceProvider.getString(Preference.PIN, "") == pin) {
            preferenceProvider.setValue(Preference.SESSION_LOCKED, true)
            true
        } else {
            false
        }
    }

    fun savePin(pin: String) {
        preferenceProvider.setValue(Preference.PIN, pin)
        preferenceProvider.setValue(Preference.SESSION_LOCKED, true)
    }

    fun logOut() {
        try {
            d2.userModule().blockingLogOut()
            preferenceProvider.setValue(Preference.PIN, null)
            preferenceProvider.setValue(Preference.SESSION_LOCKED, false)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}
