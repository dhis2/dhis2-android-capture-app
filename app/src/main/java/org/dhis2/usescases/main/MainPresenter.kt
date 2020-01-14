package org.dhis2.usescases.main

import android.text.TextUtils.isEmpty
import android.view.Gravity
import androidx.work.WorkManager
import io.reactivex.disposables.CompositeDisposable
import org.dhis2.data.prefs.Preference.Companion.DEFAULT_CAT_COMBO
import org.dhis2.data.prefs.Preference.Companion.PIN
import org.dhis2.data.prefs.Preference.Companion.PREF_DEFAULT_CAT_OPTION_COMBO
import org.dhis2.data.prefs.Preference.Companion.SESSION_LOCKED
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.usescases.login.LoginActivity
import org.dhis2.utils.filters.FilterManager
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.user.User
import timber.log.Timber

const val DEFAULT = "default"

class MainPresenter(
    private val view: MainView,
    private val d2: D2,
    private val schedulerProvider: SchedulerProvider,
    private val preferences: PreferenceProvider,
    private val workManger: WorkManager,
    private val filterManager: FilterManager
) {

    var disposable: CompositeDisposable = CompositeDisposable()

    fun init() {
        disposable.add(
            d2.userModule().user().get()
                .map { username(it) }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.renderUsername(it) },
                    { Timber.e(it) }
                )
        )

        disposable.add(
            d2.categoryModule().categoryCombos().byIsDefault().eq(true).one().get()
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { categoryCombo ->
                        preferences.setValue(DEFAULT_CAT_COMBO, categoryCombo.uid())
                    },
                    { Timber.e(it) }
                )
        )

        disposable.add(
            d2
                .categoryModule()
                .categoryOptionCombos().byCode().eq(DEFAULT).one().get()
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { categoryOptionCombo ->
                        preferences.setValue(
                            PREF_DEFAULT_CAT_OPTION_COMBO,
                            categoryOptionCombo.uid()
                        )
                    },
                    { Timber.e(it) }
                )
        )
    }

    fun initFilters() {
        disposable.add(
            filterManager.asFlowable()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { filterManager -> view.updateFilters(filterManager.totalFilters) },
                    { Timber.e(it) }
                )
        )

        disposable.add(
            filterManager.periodRequest
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { periodRequest -> view.showPeriodRequest(periodRequest) },
                    { Timber.e(it) }
                )
        )
    }

    fun logOut() {
        disposable.add(
            d2.userModule().logOut()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    {
                        workManger.cancelAllWork()
                        view.startActivity(LoginActivity::class.java, null, true, true, null)
                    },
                    { Timber.e(it) }
                )
        )
    }

    fun blockSession() {
        workManger.cancelAllWork()
        view.back()
    }

    fun showFilter() {
        view.showHideFilter()
    }

    fun onDetach() {
        disposable.clear()
    }

    fun onMenuClick() {
        view.openDrawer(Gravity.START)
    }

    private fun username(user: User): String {
        return String.format(
            "%s %s",
            if (isEmpty(user.firstName())) "" else user.firstName(),
            if (isEmpty(user.surname())) "" else user.surname()
        )
    }
}
