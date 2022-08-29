package org.dhis2.usescases.main

import android.view.Gravity
import androidx.lifecycle.LiveData
import androidx.work.WorkInfo
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import org.dhis2.commons.Constants
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.data.FilterRepository
import org.dhis2.commons.matomo.Actions.Companion.BLOCK_SESSION_PIN
import org.dhis2.commons.matomo.Actions.Companion.OPEN_ANALYTICS
import org.dhis2.commons.matomo.Actions.Companion.QR_SCANNER
import org.dhis2.commons.matomo.Actions.Companion.SETTINGS
import org.dhis2.commons.matomo.Categories.Companion.HOME
import org.dhis2.commons.matomo.Labels.Companion.CLICK
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.prefs.Preference
import org.dhis2.commons.prefs.Preference.Companion.DEFAULT_CAT_COMBO
import org.dhis2.commons.prefs.Preference.Companion.PREF_DEFAULT_CAT_OPTION_COMBO
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.server.UserManager
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.data.service.workManager.WorkerItem
import org.dhis2.data.service.workManager.WorkerType
import org.dhis2.usescases.login.SyncIsPerformedInteractor
import org.dhis2.usescases.settings.DeleteUserData
import org.dhis2.usescases.sync.WAS_INITIAL_SYNC_DONE
import org.dhis2.utils.TRUE
import org.hisp.dhis.android.core.systeminfo.SystemInfo
import org.hisp.dhis.android.core.user.User
import timber.log.Timber

const val DEFAULT = "default"
const val MIN_USERS = 1
const val SERVER_ACTION = "Server"
const val DHIS2 = "dhis2_server"

class MainPresenter(
    private val view: MainView,
    private val repository: HomeRepository,
    private val schedulerProvider: SchedulerProvider,
    private val preferences: PreferenceProvider,
    private val workManagerController: WorkManagerController,
    private val filterManager: FilterManager,
    private val filterRepository: FilterRepository,
    private val matomoAnalyticsController: MatomoAnalyticsController,
    private val userManager: UserManager,
    private val deleteUserData: DeleteUserData,
    private val syncIsPerformedInteractor: SyncIsPerformedInteractor
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
        trackDhis2Server()
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

    fun trackDhis2Server() {
        disposable.add(
            repository.getServerVersion()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { systemInfo -> compareAndTrack(systemInfo) },
                    { Timber.e(it) }
                )
        )
    }

    private fun compareAndTrack(systemInfo: SystemInfo) {
        val dhis2ServerTracked = preferences.getString("$DHIS2${getUserUid()}", "")
        val currentDhis2Server = systemInfo.version() ?: ""

        if ((dhis2ServerTracked.isNullOrEmpty() || dhis2ServerTracked != currentDhis2Server) &&
            currentDhis2Server.isNotEmpty()
        ) {
            matomoAnalyticsController.trackEvent(
                HOME,
                SERVER_ACTION,
                currentDhis2Server
            )
            preferences.setValue("$DHIS2${getUserUid()}", currentDhis2Server)
        }
    }

    private fun getUserUid(): String {
        return try {
            userManager.d2.userModule().user().blockingGet().uid()
        } catch (e: Exception) {
            ""
        }
    }

    fun logOut() {
        disposable.add(
            repository.logOut()
                .subscribeOn(schedulerProvider.ui())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    {
                        workManagerController.cancelAllWork()
                        FilterManager.getInstance().clearAllFilters()
                        preferences.setValue(Preference.SESSION_LOCKED, false)
                        preferences.setValue(Preference.PIN, null)
                        view.goToLogin(repository.accountsCount(), isDeletion = false)
                    },
                    { Timber.e(it) }
                )
        )
    }

    fun onDeleteAccount() {
        view.showProgressDeleteNotification()
        try {
            workManagerController.cancelAllWork()
            deleteUserData.wipeCacheAndPreferences(view.obtainFileView())
            userManager.d2?.wipeModule()?.wipeEverything()
            userManager.d2?.userModule()?.accountManager()?.deleteCurrentAccount()
            view.cancelNotifications()

            view.goToLogin(repository.accountsCount(), isDeletion = true)
        } catch (exception: Exception) {
            Timber.e(exception)
        }
    }

    fun onSyncAllClick() {
        view.showGranularSync()
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

    fun launchInitialDataSync() {
        workManagerController
            .syncDataForWorker(Constants.DATA_NOW, Constants.INITIAL_SYNC)
        val workerItem = WorkerItem(
            Constants.RESERVED,
            WorkerType.RESERVED,
            null,
            null,
            null,
            null
        )
        workManagerController.cancelAllWorkByTag(workerItem.workerName)
        workManagerController.syncDataForWorker(workerItem)
    }

    fun observeDataSync(): LiveData<List<WorkInfo>> {
        return workManagerController.getWorkInfosForTags(
            Constants.INITIAL_SYNC,
            Constants.DATA_NOW,
            Constants.DATA
        )
    }

    fun wasSyncAlreadyDone(): Boolean {
        if (view.hasToNotSync()) {
            return true
        }
        return syncIsPerformedInteractor.execute()
    }

    fun onDataSuccess() {
        userManager.d2.dataStoreModule().localDataStore().value(WAS_INITIAL_SYNC_DONE)
            .blockingSet(TRUE)
    }

    fun trackHomeAnalytics() {
        matomoAnalyticsController.trackEvent(HOME, OPEN_ANALYTICS, CLICK)
    }

    fun trackPinDialog() {
        matomoAnalyticsController.trackEvent(HOME, BLOCK_SESSION_PIN, CLICK)
    }

    fun trackQRScanner() {
        matomoAnalyticsController.trackEvent(HOME, QR_SCANNER, CLICK)
    }
}
