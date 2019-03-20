package org.dhis2.usescases.splash


import android.content.Context
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.dhis2.data.server.UserManager
import org.dhis2.usescases.login.LoginActivity
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.sync.SyncActivity
import org.dhis2.utils.Constants
import org.dhis2.utils.SyncUtils
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SplashPresenter internal constructor(private val userManager: UserManager?) : SplashContracts.Presenter {
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
        if (userManager == null) {
            navigateToLoginView()
            return
        }

        if (SyncUtils.isSyncRunning()) {

            view!!.startActivity(SyncActivity::class.java, null, true, true, null)

        } else {

            compositeDisposable.add(userManager.isUserLoggedIn
                    .delay(2000, TimeUnit.MILLISECONDS, Schedulers.io())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            {
                                val prefs = view!!.abstracContext.getSharedPreferences(
                                        Constants.SHARE_PREFS, Context.MODE_PRIVATE)
                                if (it!! && !prefs.getBoolean("SessionLocked", false)) {
                                    navigateToHomeView()
                                } else {
                                    navigateToLoginView()
                                }
                            },
                            { Timber.d(it) }
                    ))

        }
    }

    override fun navigateToLoginView() {
        view!!.startActivity(LoginActivity::class.java, null, true, true, null)
    }

    override fun navigateToHomeView() {
        view!!.startActivity(MainActivity::class.java, null, true, true, null)
    }

}