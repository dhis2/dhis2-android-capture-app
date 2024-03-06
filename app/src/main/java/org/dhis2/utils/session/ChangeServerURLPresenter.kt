package org.dhis2.utils.session

import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.dhis2.commons.Constants
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.prefs.SECURE_SERVER_URL
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.hisp.dhis.android.core.D2
import timber.log.Timber

enum class Mode {
    EDIT, WARNING
}

class ChangeServerURLPresenter(
    private val view: ChangeServerURLView,
    private val preferenceProvider: PreferenceProvider,
    val d2: D2
) {

    private var currentServerURL: String = ""
    private var newServerURL: String = ""
    private var mode = Mode.EDIT

    var disposable: CompositeDisposable = CompositeDisposable()

    fun init() {
        val serverURL = preferenceProvider.getString(SECURE_SERVER_URL) ?: ""

        this.currentServerURL = serverURL.replace("/api", "")

        view.renderServerUrl(currentServerURL)

        view.disableOk()
    }

    fun onServerChanged(serverUrl: CharSequence, start: Int, before: Int, count: Int) {
        this.newServerURL = serverUrl.toString()

        if (serverUrl.isNotEmpty() && serverUrl.toString() != currentServerURL) {
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
            saveInTheStore()
        }
    }

    private fun saveInTheStore() {
        view.showLoginProgress()

        try {
            updateUrlInPreference()
            updateCredentialsAndDataBaseConfigurations()

            d2.databaseAdapter().execSQL("DELETE FROM SystemInfo")

            CoroutineScope(Dispatchers.IO).launch {
                d2.systemInfoModule().systemInfo().download().blockingAwait()
            }

            view.renderSuccess("Change realized successfully to$newServerURL")

            view.closeDialog()
        } catch (e: Exception) {
            handleError(e)
        }
    }

    private fun updateUrlInPreference() {
        //preferenceProvider.setValue(SERVER, newServerURL)

        preferenceProvider.updateServerURL(newServerURL)

        val updatedServer =
            (preferenceProvider.getSet(Constants.PREFS_URLS, HashSet()) as HashSet)

        updatedServer.remove(currentServerURL)
        updatedServer.add(newServerURL)

        preferenceProvider.setValue(Constants.PREFS_URLS, updatedServer)

        view.closeDialog()
    }

    private fun updateCredentialsAndDataBaseConfigurations() {
        d2.userModule().accountManager().changeServerUrl(newServerURL)
    }

    private fun handleError(error: Throwable) {
        Timber.e(error)

        view.renderError(error)
        view.hideLoginProgress()
        view.showEditMode()
    }
}

interface ChangeServerURLView {
    fun showEditMode()
    fun requestConfirmation()
    fun renderServerUrl(url: String)
    fun enableOk()
    fun disableOk()
    fun getAbstractContext(): ActivityGlobalAbstract
    fun showLoginProgress()
    fun hideLoginProgress()
    fun renderError(throwable: Throwable)
    fun renderSuccess(message: String)
    fun closeDialog()
}
