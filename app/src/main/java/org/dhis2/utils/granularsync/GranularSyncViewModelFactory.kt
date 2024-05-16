package org.dhis2.utils.granularsync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.sync.SyncContext
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.service.workManager.WorkManagerController
import org.hisp.dhis.android.core.D2

@Suppress("UNCHECKED_CAST")
class GranularSyncViewModelFactory(
    private val d2: D2,
    private val view: GranularSyncContracts.View,
    private val repository: GranularSyncRepository,
    private val schedulerProvider: SchedulerProvider,
    private val dispatcher: DispatcherProvider,
    private val syncContext: SyncContext,
    private val workManagerController: WorkManagerController,
    private val smsSyncProvider: SMSSyncProvider,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GranularSyncPresenter(
            d2,
            view,
            repository,
            schedulerProvider,
            dispatcher,
            syncContext,
            workManagerController,
            smsSyncProvider,
        ) as T
    }
}
