package org.dhis2.utils.session

import android.util.Log
import org.dhis2.commons.Constants.SERVER
import org.dhis2.commons.prefs.PreferenceProvider
import org.hisp.dhis.android.core.D2

enum class Mode {
    EDIT, WARNING
}

class ChangeServerURLPresenter(
    val view: ChangeServerURLView,
    val preferenceProvider: PreferenceProvider,
    val d2: D2?
) {

    private var currentServerURL: String = ""
    private var newServerURL: String = ""
    private var mode = Mode.EDIT

    fun init() {
        val serverURL = preferenceProvider.getString(SERVER) ?: ""

        this.currentServerURL = serverURL

        view.renderServerUrl(serverURL)

        view.disableOk()
    }

    fun onServerChanged(serverUrl: CharSequence, start: Int, before: Int, count: Int) {
        if (serverUrl.isNotEmpty()) {
            this.newServerURL = serverUrl.toString()
            view.enableOk()
        } else {
            view.disableOk()
        }
    }

    fun save() {
        if (mode == Mode.EDIT) {
            if (currentServerURL != newServerURL) {
                mode = Mode.WARNING
                view.requestConfirmation()
            }
        } else {
            Log.d("ChangeURLPresenter", "confirmed")
        }
    }
}

interface ChangeServerURLView {
    fun requestConfirmation()
    fun renderServerUrl(url: String)
    fun enableOk()
    fun disableOk()
}
