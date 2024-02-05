package org.dhis2.utils.session

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import org.dhis2.App
import org.dhis2.commons.Constants
import org.dhis2.commons.Constants.SERVER
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.hisp.dhis.android.core.D2
import retrofit2.Response
import timber.log.Timber

enum class Mode {
    EDIT, WARNING
}

class ChangeServerURLPresenter(
    val view: ChangeServerURLView,
    val preferenceProvider: PreferenceProvider,
    private val schedulers: SchedulerProvider,
    val d2: D2?
) {

    private var currentServerURL: String = ""
    private var newServerURL: String = ""
    private var mode = Mode.EDIT

    var disposable: CompositeDisposable = CompositeDisposable()

    fun init() {
        val serverURL = preferenceProvider.getString(SERVER) ?: ""

        this.currentServerURL = serverURL.replace("/api", "")

        view.renderServerUrl(currentServerURL)

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
            checkLogin()
        }
    }

    private fun checkLogin() {
        view.showLoginProgress()

        disposable.add(Observable.just(
            (view.getAbstractContext().applicationContext as App).createServerComponent()
                .userManager()
        ).flatMap { userManager ->
            userManager.logIn("android", "Android123", newServerURL)
                .map<Response<Any>> { _ ->
                    run {
                        preferenceProvider.setValue(SERVER, "$newServerURL/api")

                        Response.success(null)
                    }
                }
        }.subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
            .subscribe(
                {
                    this.handleResponse(it)
                },
                {
                    this.handleError(it)
                }
            ))
    }

    private fun handleResponse(userResponse: Response<*>) {
        view.hideLoginProgress()

        if (userResponse.isSuccessful) {
            val updatedServer =
                (preferenceProvider.getSet(Constants.PREFS_URLS, HashSet()) as HashSet)

            updatedServer.remove(currentServerURL)
            updatedServer.add(newServerURL)

            preferenceProvider.setValue(Constants.PREFS_URLS, updatedServer)

            view.closeDialog()
        }
    }

    private fun handleError(
        throwable: Throwable,
    ) {
        Timber.e(throwable)

        view.renderError(throwable)
        view.hideLoginProgress()
    }
}

interface ChangeServerURLView {
    fun requestConfirmation()
    fun renderServerUrl(url: String)
    fun enableOk()
    fun disableOk()
    fun getAbstractContext(): ActivityGlobalAbstract
    fun showLoginProgress()
    fun hideLoginProgress()
    fun renderError(throwable: Throwable)
    fun closeDialog()
}
