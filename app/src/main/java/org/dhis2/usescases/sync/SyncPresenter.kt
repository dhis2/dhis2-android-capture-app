package org.dhis2.usescases.sync

import io.reactivex.disposables.CompositeDisposable
import org.dhis2.commons.prefs.Preference
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.server.UserManager
import org.dhis2.mobile.sync.data.SyncBackgroundJobAction
import org.dhis2.mobile.sync.model.SyncJobStatus
import org.dhis2.mobile.sync.model.SyncStatus
import timber.log.Timber

const val WAS_INITIAL_SYNC_DONE = "WasInitialSyncDone"

class SyncPresenter internal constructor(
    private val view: SyncView,
    private val userManager: UserManager?,
    private val schedulerProvider: SchedulerProvider,
    private val backgroundJobAction: SyncBackgroundJobAction,
    private val preferences: PreferenceProvider,
) {
    private val disposable = CompositeDisposable()

    fun sync() {
        backgroundJobAction.launchMetadataSync(0)
    }

    fun observeSyncProcess() = backgroundJobAction.observeMetadataJob()

    fun handleSyncInfo(workInfoList: List<SyncJobStatus>) {
        workInfoList.forEach { workInfo ->
            if (workInfo.tags.contains("METADATA_SYNC_NOW")) {
                handleMetaState(workInfo.status, workInfo.message)
            }
        }
    }

    private fun handleMetaState(
        state: SyncStatus,
        message: String?,
    ) {
        when (state) {
            SyncStatus.Running -> view.setMetadataSyncStarted()
            SyncStatus.Succeed -> view.setMetadataSyncSucceed()
            SyncStatus.Failed -> view.showMetadataFailedMessage(message)
            else -> {
                // do nothing
            }
        }
    }

    fun onMetadataSyncSuccess() {
        preferences.setValue(Preference.INITIAL_METADATA_SYNC_DONE, true)
        userManager?.let { userManager ->
            disposable.add(
                userManager.theme
                    .doOnSuccess { flagAndTheme ->
                        preferences.setValue(Preference.FLAG, flagAndTheme.first)
                        preferences.setValue(Preference.THEME, flagAndTheme.second)
                    }.subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(
                        { (first, second) ->
                            view.setFlag(first)
                            view.setServerTheme(second)
                            view.goToMain()
                        },
                        { t: Throwable? ->
                            Timber.e(t)
                        },
                    ),
            )
        }
    }

    fun onLogout() {
        userManager?.let { userManager ->
            disposable.add(
                userManager
                    .logout()
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(
                        { view.goToLogin() },
                    ) { t: Throwable? ->
                        Timber.e(t)
                    },
            )
        }
    }

    fun onDetach() {
        disposable.clear()
    }
}
