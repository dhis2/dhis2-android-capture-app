package org.dhis2.utils.session

import org.dhis2.data.prefs.Preference
import org.dhis2.data.prefs.PreferenceProvider

class PinPresenter(
    val view: PinView,
    val preferenceProvider: PreferenceProvider
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
}