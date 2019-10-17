package org.dhis2.usescases.splash


import android.content.Context
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.dhis2.data.prefs.Preference
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.server.UserManager
import org.dhis2.usescases.login.LoginActivity
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.sync.SyncActivity
import org.dhis2.utils.Constants
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SplashPresenter internal constructor(private val userManager: UserManager?, private val schedulerProvider: SchedulerProvider) : SplashContracts.Presenter {
    private var view: SplashContracts.View? = null
    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun destroy() {
        compositeDisposable.clear()
    }

    override fun init(view: SplashContracts.View) {
        this.view = view
        isUserLoggedIn()
    }

    override fun isUserLoggedIn() {
        if (userManager == null)
            navigateTo(LoginActivity::class.java)
        else
            compositeDisposable.add(userManager.isUserLoggedIn
                    .delay(2000, TimeUnit.MILLISECONDS, schedulerProvider.io())
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(
                            {
                                val prefs = view!!.abstracContext.getSharedPreferences(
                                        Constants.SHARE_PREFS, Context.MODE_PRIVATE)
                                val sessionLocked = prefs.getBoolean("SessionLocked", false)
                                val initialSyncDone = prefs.getBoolean(Preference.INITIAL_SYNC_DONE.name, false)

                                if (it!! && initialSyncDone && !sessionLocked) {
                                    navigateTo(MainActivity::class.java)
                                } else if (it && !initialSyncDone) {
                                    navigateTo(SyncActivity::class.java)
                                } else {
                                    navigateTo(LoginActivity::class.java)
                                }
                            },
                            { Timber.d(it) }
                    ))
    }


    override fun navigateTo(data: Class<*>) {
        view!!.startActivity(data, null, true, true, null)
    }

}