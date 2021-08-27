package org.dhis2.usescases.main

import android.view.Gravity
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import org.dhis2.commons.prefs.Preference
import org.dhis2.commons.prefs.Preference.Companion.DEFAULT_CAT_COMBO
import org.dhis2.commons.prefs.Preference.Companion.PREF_DEFAULT_CAT_OPTION_COMBO
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.filter.FilterRepository
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.usescases.login.LoginActivity
import org.dhis2.utils.analytics.matomo.Actions.Companion.SETTINGS
import org.dhis2.utils.analytics.matomo.Categories.Companion.HOME
import org.dhis2.utils.analytics.matomo.Labels.Companion.CLICK
import org.dhis2.utils.analytics.matomo.MatomoAnalyticsController
import org.dhis2.utils.filters.FilterManager
import org.hisp.dhis.android.core.user.User
import timber.log.Timber

const val DEFAULT = "default"

class MainPresenter(
    private val view: MainView,
    private val repository: HomeRepository,
    private val schedulerProvider: SchedulerProvider,
    private val preferences: PreferenceProvider,
    private val workManagerController: WorkManagerController,
    private val filterManager: FilterManager,
    private val filterRepository: FilterRepository,
    private val matomoAnalyticsController: MatomoAnalyticsController
) {

    var disposable: CompositeDisposable = CompositeDisposable()

    fun init() {
        preferences.removeValue(Preference.CURRENT_ORG_UNIT)
        disposable.add(
            repository.user()
                .map { username(it) }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.renderUsername(it) },
                    { Timber.e(it) }
                )
        )

        disposable.add(
            repository.defaultCatCombo()
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { categoryCombo ->
                        preferences.setValue(DEFAULT_CAT_COMBO, categoryCombo.uid())
                    },
                    { Timber.e(it) }
                )
        )

        disposable.add(
            repository.defaultCatOptCombo()
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
            Flowable.just(filterRepository.homeFilters())
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { filters ->
                        if (filters.isEmpty()) {
                            view.hideFilters()
                        } else {
                            view.setFilters(filters)
                        }
                    },
                    { Timber.e(it) }
                )
        )

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
                    { periodRequest -> view.showPeriodRequest(periodRequest.first) },
                    { Timber.e(it) }
                )
        )
    }

    fun logOut() {
        disposable.add(
            repository.logOut()
                .subscribeOn(schedulerProvider.ui())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    {
                        workManagerController.cancelAllWork()
                        preferences.setValue(Preference.SESSION_LOCKED, false)
                        preferences.setValue(Preference.PIN, null)
                        view.startActivity(LoginActivity::class.java, null, true, true, null)
                    },
                    { Timber.e(it) }
                )
        )
    }

    fun blockSession() {
        workManagerController.cancelAllWork()
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
            if (user.firstName().isNullOrEmpty()) "" else user.firstName(),
            if (user.surname().isNullOrEmpty()) "" else user.surname()
        )
    }

    fun hasProgramWithAssignment(): Boolean {
        return repository.hasProgramWithAssignment()
    }

    fun onNavigateBackToHome() {
        view.goToHome()
        initFilters()
    }

    fun onClickSyncManager() {
        matomoAnalyticsController.trackEvent(HOME, SETTINGS, CLICK)
    }

    fun setOpeningFilterToNone() {
        filterRepository.collapseAllFilters()
    }
}
