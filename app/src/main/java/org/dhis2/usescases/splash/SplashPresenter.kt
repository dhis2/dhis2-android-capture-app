package org.dhis2.usescases.splash

import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit
import org.dhis2.commons.prefs.Preference
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.server.UserManager
import org.dhis2.utils.Constants
import org.dhis2.utils.Constants.SERVER
import org.dhis2.utils.Constants.USER
import org.dhis2.utils.reporting.CrashReportController
import timber.log.Timber

class SplashPresenter internal constructor(
    private var view: SplashView,
    private val userManager: UserManager?,
    private val schedulerProvider: SchedulerProvider,
    private val preferenceProvider: PreferenceProvider,
    private val crashReportController: CrashReportController
) {

    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    fun destroy() {
        compositeDisposable.clear()
    }

    fun init() {
        preferenceProvider.sharedPreferences().all.forEach {
            when (it.key) {
                Constants.PREFS_URLS, Constants.PREFS_USERS -> {
                }
                else -> preferenceProvider.setValue(it.key, it.value)
            }
        }
        isUserLoggedIn()
    }

    private fun isUserLoggedIn() {
        userManager?.let { userManager ->
            compositeDisposable.add(
                userManager.isUserLoggedIn
                    .delay(2000, TimeUnit.MILLISECONDS, schedulerProvider.io())
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(
                        { userLogged ->
                            if (userLogged) {
                                trackUserInfo()
                            }
                            view.goToNextScreen(
                                userLogged,
                                preferenceProvider.getBoolean(Preference.SESSION_LOCKED, false),
                                preferenceProvider.getBoolean(Preference.INITIAL_SYNC_DONE, false),
                                userManager.hasMultipleAccounts()
                            )
                        },
                        { Timber.d(it) }
                    )
            )
        } ?: view.goToNextScreen(
            false,
            sessionLocked = false,
            initialSyncDone = false,
            hasMultipleAccounts = false
        )
    }

    private fun trackUserInfo() {
        val username = preferenceProvider.getString(USER)
        val server = preferenceProvider.getString(SERVER)

        crashReportController.trackServer(server)
        crashReportController.trackUser(username, server)
    }
}
