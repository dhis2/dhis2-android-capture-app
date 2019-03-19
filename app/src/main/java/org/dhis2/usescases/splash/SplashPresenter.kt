package org.dhis2.usescases.splash


import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources

import org.dhis2.data.server.UserManager
import org.dhis2.usescases.login.LoginActivity
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.sync.SyncActivity
import org.dhis2.utils.Constants
import org.dhis2.utils.SyncUtils

import java.util.concurrent.TimeUnit
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

import android.text.TextUtils.isEmpty

class SplashPresenter internal constructor(private val userManager: UserManager?, private val splashRespository: SplashRepository) : SplashContracts.Presenter {
    private var view: SplashContracts.View? = null
    private val compositeDisposable: CompositeDisposable

    init {
        this.compositeDisposable = CompositeDisposable()
    }

    override fun destroy() {
        compositeDisposable.clear()
    }

    override fun init(view: SplashContracts.View) {
        this.view = view

        compositeDisposable.add(splashRespository.iconForFlag
                .delay(2, TimeUnit.SECONDS, Schedulers.io())
                .map<Int> { flagName ->
                    if (!isEmpty(flagName)) {
                        val resources = view.abstracContext.resources
                        return@splashRespository.getIconForFlag()
                                .delay(2, TimeUnit.SECONDS, Schedulers.io())
                                .map resources . getIdentifier flagName, "drawable", view.getAbstracContext().getPackageName())
                    } else
                        return@splashRespository.getIconForFlag()
                                .delay(2, TimeUnit.SECONDS, Schedulers.io())
                                .map - 1
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view.renderFlag(),
                        Consumer<Throwable> { Timber.d(it) }
                )
        )
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
                    .subscribe({ isUserLoggedIn ->
                        val prefs = view!!.abstracContext.getSharedPreferences(
                                Constants.SHARE_PREFS, Context.MODE_PRIVATE)
                        if (isUserLoggedIn!! && !prefs.getBoolean("SessionLocked", false)) {
                            navigateToHomeView()
                        } else {
                            navigateToLoginView()
                        }
                    }, Consumer<Throwable> { Timber.e(it) }))
        }
    }

    override fun navigateToLoginView() {
        view!!.startActivity(LoginActivity::class.java, null, true, true, null)
    }

    override fun navigateToHomeView() {
        view!!.startActivity(MainActivity::class.java, null, true, true, null)
    }

}