package org.dhis2.utils.session

import org.dhis2.commons.Constants.SERVER
import org.dhis2.commons.prefs.Preference
import org.dhis2.commons.prefs.PreferenceProvider
import org.hisp.dhis.android.core.D2
import timber.log.Timber

class ChangeServerURLPresenter(
    val view: ChangeServerURLView,
    val preferenceProvider: PreferenceProvider,
    val d2: D2?
) {

    fun init() {
        val serverURL = preferenceProvider.getString(SERVER) ?: ""

        view.renderServerUrl(serverURL)
    }

    fun logOut() {
        try {
            d2?.userModule()?.blockingLogOut()
            preferenceProvider.setValue(Preference.PIN, null)
            preferenceProvider.setValue(Preference.SESSION_LOCKED, false)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}

interface ChangeServerURLView {
    fun closeDialog()
    fun renderServerUrl(url: String)
}
