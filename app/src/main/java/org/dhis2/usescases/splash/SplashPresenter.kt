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
            navigateTo(LoginActivity::class.java)
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
                                    navigateTo(MainActivity::class.java)
                                } else {
                                    navigateTo(LoginActivity::class.java)
                                }
                            },
                            { Timber.d(it) }
                    ))

        }
    }


    override fun navigateTo(data: Class<*>) {
        view!!.startActivity(data, null, true, true, null)
    }

}