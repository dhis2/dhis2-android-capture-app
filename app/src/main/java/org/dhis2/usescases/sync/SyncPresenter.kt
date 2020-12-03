package org.dhis2.usescases.sync

import io.reactivex.disposables.CompositeDisposable
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.data.service.workManager.WorkerItem
import org.dhis2.data.service.workManager.WorkerType
import org.dhis2.utils.Constants
import timber.log.Timber

class SyncPresenter internal constructor(
    private val view: SyncView,
    private val syncRepository: SyncRepository,
    private val schedulerProvider: SchedulerProvider,
    private val workManagerController: WorkManagerController
) {
    private val disposable = CompositeDisposable()

    fun sync() {
        workManagerController
            .syncDataForWorkers(Constants.META_NOW, Constants.DATA_NOW, Constants.INITIAL_SYNC)
    }

    fun onMetadataSyncSuccess() {
        disposable.add(
            syncRepository.getTheme()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { (first, second) ->
                        view.saveFlag(first)
                        view.saveTheme(second)
                    },
                    { t: Throwable? ->
                        Timber.e(t)
                    }
                )
        )
    }

    fun syncReservedValues() {
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

    fun logout() {
        disposable.add(
            syncRepository.logout()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.goToLogin() }
                ) { t: Throwable? ->
                    Timber.e(t)
                }
        )
    }

    fun onDetach() {
        disposable.clear()
    }

}