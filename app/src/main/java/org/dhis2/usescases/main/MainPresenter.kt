package org.dhis2.usescases.main

import android.content.Context
import android.net.Uri
import android.view.Gravity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.work.ExistingWorkPolicy
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.dhis2.BuildConfig
import org.dhis2.commons.Constants
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.matomo.Actions.Companion.BLOCK_SESSION_PIN
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
import kotlin.coroutines.CoroutineContext

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
    private val matomoAnalyticsController: MatomoAnalyticsController,
    private val userManager: UserManager,
    private val deleteUserData: DeleteUserData,
    private val syncIsPerformedInteractor: SyncIsPerformedInteractor,
    private val syncStatusController: SyncStatusController,
    private val versionRepository: VersionRepository,
    private val dispatcherProvider: DispatcherProvider,
    private val forceToNotSynced: Boolean,
) : CoroutineScope {

    private var job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + dispatcherProvider.io()

    var disposable: CompositeDisposable = CompositeDisposable()

    val versionToUpdate = versionRepository.newAppVersion.asLiveData(coroutineContext)
    val downloadingVersion = MutableLiveData(false)

    fun init() {
        filterManager.clearAllFilters()
        preferences.removeValue(Preference.CURRENT_ORG_UNIT)
        disposable.add(
            repository.user()
                .map { username(it) }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.renderUsername(it) },
                    { Timber.e(it) },
                ),
        )

        disposable.add(
            repository.defaultCatCombo()
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { categoryCombo ->
                        preferences.setValue(DEFAULT_CAT_COMBO, categoryCombo?.uid())
                    },
                    { Timber.e(it) },
                ),
        )

        disposable.add(
            repository.defaultCatOptCombo()
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                    { categoryOptionCombo ->
                        preferences.setValue(
                            PREF_DEFAULT_CAT_OPTION_COMBO,
                            categoryOptionCombo?.uid(),
                        )
                    },
                    { Timber.e(it) },
                ),
        )
        trackDhis2Server()
    }

    fun trackDhis2Server() {
        disposable.add(
            repository.getServerVersion()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { systemInfo -> systemInfo?.let { compareAndTrack(systemInfo) } },
                    { Timber.e(it) },
                ),
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
                currentDhis2Server,
            )
            preferences.setValue("$DHIS2${getUserUid()}", currentDhis2Server)
        }
    }

    private fun getUserUid(): String {
        return try {
            userManager.d2.userModule().user().blockingGet()?.uid() ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    fun logOut() {
        disposable.add(
            Completable.fromCallable {
                workManagerController.cancelAllWork()
                syncStatusController.restore()
                filterManager.clearAllFilters()
                preferences.setValue(Preference.SESSION_LOCKED, false)
                preferences.setValue(Preference.PIN_ENABLED, false)
                userManager.d2.dataStoreModule().localDataStore().value(PIN).blockingDeleteIfExist()
            }.andThen(
                repository.logOut(),
            )
                .subscribeOn(schedulerProvider.ui())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    {
                        view.goToLogin(repository.accountsCount(), isDeletion = false)
                    },
                    { Timber.e(it) },
                ),
        )
    }

    fun onDeleteAccount() {
        view.showProgressDeleteNotification()
        try {
            workManagerController.cancelAllWork()
            syncStatusController.restore()
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
            if (user.surname().isNullOrEmpty()) "" else user.surname(),
        )
    }

    fun onNavigateBackToHome() {
        view.goToHome()
    }

    fun onClickSyncManager() {
        matomoAnalyticsController.trackEvent(HOME, SETTINGS, CLICK)
    }

    fun isPinStored() = repository.isPinStored()

    fun launchInitialDataSync() {
        checkVersionUpdate()
        workManagerController
            .syncDataForWorker(Constants.DATA_NOW, Constants.INITIAL_SYNC)
    }

    fun observeDataSync(): StateFlow<SyncStatusData> {
        return syncStatusController.observeDownloadProcess()
    }

    fun wasSyncAlreadyDone(): Boolean {
        if (forceToNotSynced) {
            return true
        }
        return syncIsPerformedInteractor.execute()
    }

    fun onDataSuccess() {
        launch(dispatcherProvider.io()) {
            userManager.d2.dataStoreModule().localDataStore().value(WAS_INITIAL_SYNC_DONE)
                .blockingSet(TRUE)
        }
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
            policy = ExistingWorkPolicy.REPLACE,
        )
        workManagerController.beginUniqueWork(workerItem)
        versionRepository.removeVersionInfo()
    }

    fun downloadVersion(
        context: Context,
        onDownloadCompleted: (Uri) -> Unit,
        onLaunchUrl: (Uri) -> Unit,
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
                onDownloading = { downloadingVersion.value = true },
            )
        }
    }

    fun hasOneHomeItem(): Boolean {
        return repository.homeItemCount() == 1
    }

    fun getSingleItemData(): HomeItemData? {
        return repository.singleHomeItemData()
    }
}
