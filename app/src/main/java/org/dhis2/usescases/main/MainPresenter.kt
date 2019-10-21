package org.dhis2.usescases.main

import android.content.Context
import android.text.TextUtils.isEmpty
import android.view.Gravity
import androidx.work.WorkManager
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import org.dhis2.data.prefs.Preference
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.usescases.login.LoginActivity
import org.dhis2.utils.Constants
import org.dhis2.utils.filters.FilterManager
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.user.User
import timber.log.Timber

const val DEFAULT = "default"

class MainPresenter(
        private val view: MainView,
        private val d2: D2,
        private val schedulerProvider: SchedulerProvider) {

    private var compositeDisposable: CompositeDisposable? = CompositeDisposable()

    fun init() {

        compositeDisposable!!.add(d2.userModule().user.get()
                .map { this.username(it) }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        view.renderUsername(),
                        Consumer { Timber.e(it) }
                )
        )

        compositeDisposable!!.add(
                d2.categoryModule().categoryCombos.byIsDefault().eq(true).one().get().toObservable()
                        .subscribeOn(schedulerProvider.io())
                        .subscribe(
                                { categoryCombo ->
                                    val prefs = view.abstracContext.getSharedPreferences(
                                            Constants.SHARE_PREFS, Context.MODE_PRIVATE)
                                    prefs.edit().putString(Constants.DEFAULT_CAT_COMBO, categoryCombo.uid()).apply()
                                },
                                { Timber.e(it) }
                        )
        )


        compositeDisposable!!.add(
                d2.categoryModule().categoryOptionCombos.byCode().eq(DEFAULT).one().get().toObservable()
                        .subscribeOn(schedulerProvider.io())
                        .subscribe(
                                { categoryOptionCombo ->
                                    val prefs = view.abstracContext.getSharedPreferences(
                                            Constants.SHARE_PREFS, Context.MODE_PRIVATE)
                                    prefs.edit().putString(Constants.PREF_DEFAULT_CAT_OPTION_COMBO, categoryOptionCombo.uid()).apply()
                                },
                                { Timber.e(it) }
                        )
        )

        compositeDisposable!!.add(
                FilterManager.getInstance().asFlowable()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                { filterManager -> view.updateFilters(filterManager.totalFilters) },
                                { Timber.e(it) }
                        )
        )

        compositeDisposable!!.add(
                FilterManager.getInstance().periodRequest
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                { periodRequest -> view.showPeriodRequest(periodRequest) },
                                { Timber.e(it) }
                        ))
    }

    fun logOut() {
        compositeDisposable!!.add(d2.userModule().logOut()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        {
                            WorkManager.getInstance(view.context.applicationContext).cancelAllWork()
                            view.startActivity(LoginActivity::class.java, null, true, true, null)
                        },
                        { Timber.e(it) })
        )
    }

    fun blockSession(pin: String) {
        val prefs = view.abstracContext.getSharedPreferences(
                Constants.SHARE_PREFS, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(Preference.SESSION_LOCKED, true).apply()
        prefs.edit().putString(Preference.PIN, pin).apply()
        WorkManager.getInstance(view.context.applicationContext).cancelAllWork()
        view.back()
    }

    fun showFilter() {
        view.showHideFilter()
    }

    fun onDetach() {
        compositeDisposable!!.clear()
    }

    fun onMenuClick() {
        view.openDrawer(Gravity.START)
    }

    private fun username(user: User): String {
        return String.format("%s %s",
                if (isEmpty(user.firstName())) "" else user.firstName(),
                if (isEmpty(user.surname())) "" else user.surname())
    }
}