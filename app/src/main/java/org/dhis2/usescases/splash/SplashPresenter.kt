package org.dhis2.usescases.splash


import io.reactivex.disposables.CompositeDisposable
import org.dhis2.data.prefs.Preference
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.server.UserManager
import org.dhis2.utils.Constants
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SplashPresenter internal constructor(
        private var view: SplashView,
        private val userManager: UserManager?,
        private val schedulerProvider: SchedulerProvider,
        private val preferenceProvider: PreferenceProvider) {

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
        userManager?.let {userManager->
            compositeDisposable.add(userManager.isUserLoggedIn
                    .delay(2000, TimeUnit.MILLISECONDS, schedulerProvider.io())
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(
                            {
                                view.goToNextScreen(
                                        it,
                                        preferenceProvider.getBoolean("SessionLocked", false),
                                        preferenceProvider.getBoolean(Preference.INITIAL_SYNC_DONE, false))
                            },
                            { Timber.d(it) }
                    ))
        } ?: view.goToNextScreen(false, sessionLocked = false, initialSyncDone = false)
    }
}