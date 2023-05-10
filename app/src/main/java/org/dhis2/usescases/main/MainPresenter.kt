package org.dhis2.usescases.main

import android.content.Context
import android.net.Uri
import android.view.Gravity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.work.ExistingWorkPolicy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.dhis2.BuildConfig
import org.dhis2.commons.Constants
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.data.FilterRepository
import org.dhis2.commons.matomo.Actions.Companion.BLOCK_SESSION_PIN
import org.dhis2.commons.matomo.Actions.Companion.JIRA_REPORT
import org.dhis2.commons.matomo.Actions.Companion.OPEN_ANALYTICS
import org.dhis2.commons.matomo.Actions.Companion.QR_SCANNER
import org.dhis2.commons.matomo.Actions.Companion.SETTINGS
import org.dhis2.commons.matomo.Categories.Companion.HOME
import org.dhis2.commons.matomo.Labels.Companion.CLICK
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.prefs.Preference
import org.dhis2.commons.prefs.Preference.Companion.DEFAULT_CAT_COMBO
import org.dhis2.commons.prefs.Preference.Companion.PIN
import org.dhis2.commons.prefs.Preference.Companion.PREF_DEFAULT_CAT_OPTION_COMBO
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.server.UserManager
import org.dhis2.data.service.SyncStatusController
import org.dhis2.data.service.SyncStatusData
import org.dhis2.data.service.VersionRepository
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
const val SERVER_ACTION = "Server"
const val DHIS2 = "dhis2_server"
const val PLAY_FLAVOR = "dhisPlayServices"

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
    private val syncIsPerformedInteractor: SyncIsPerformedInteractor,
    private val syncStatusController: SyncStatusController,
    private val versionRepository: VersionRepository,
    private val dispatcherProvider: DispatcherProvider
) : CoroutineScope {

    private var job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + dispatcherProvider.io()

    var disposable: CompositeDisposable = CompositeDisposable()

    val versionToUpdate = versionRepository.newAppVersion.asLiveData(coroutineContext)
    val downloadingVersion = MutableLiveData(false)

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
            Completable.fromCallable {
                workManagerController.cancelAllWork()
                FilterManager.getInstance().clearAllFilters()
                preferences.setValue(Preference.SESSION_LOCKED, false)
                userManager.d2.dataStoreModule().localDataStore().value(PIN).blockingDeleteIfExist()
            }.andThen(
                repository.logOut()
            )
                .subscribeOn(schedulerProvider.ui())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    {
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

    fun isPinStored() = repository.isPinStored()

    fun launchInitialDataSync() {
        checkVersionUpdate()
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

    fun observeDataSync(): LiveData<SyncStatusData> {
        return syncStatusController.observeDownloadProcess()
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

    fun trackJiraReport() {
        matomoAnalyticsController.trackEvent(HOME, JIRA_REPORT, CLICK)
    }

    fun checkVersionUpdate() {
        launch {
            versionRepository.checkVersionUpdates()
        }
    }

    fun remindLaterAlertNewVersion() {
        val workerItem = WorkerItem(
            Constants.NEW_APP_VERSION,
            WorkerType.NEW_VERSION,
            delayInSeconds = 24 * 60 * 60,
            policy = ExistingWorkPolicy.REPLACE
        )
        workManagerController.beginUniqueWork(workerItem)
        versionRepository.removeVersionInfo()
    }

    fun downloadVersion(
        context: Context,
        onDownloadCompleted: (Uri) -> Unit,
        onLaunchUrl: (Uri) -> Unit
    ) {
        if (BuildConfig.FLAVOR == PLAY_FLAVOR) {
            val url = versionRepository.getUrl()
            onLaunchUrl(Uri.parse(url))
        } else {
            versionRepository.download(
                context = context,
                onDownloadCompleted = {
                    onDownloadCompleted(it)
                    downloadingVersion.value = false
                },
                onDownloading = { downloadingVersion.value = true }
            )
        }
    }
}
