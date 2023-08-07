package org.dhis2.usescases.main.program

import androidx.lifecycle.MutableLiveData
import io.reactivex.Flowable
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow
import org.dhis2.data.service.SyncStatusData
import org.dhis2.usescases.uiboost.data.model.DataStoreAppConfig
import org.hisp.dhis.android.core.datastore.DataStoreEntry

internal interface ProgramRepository {
    fun homeItems(syncStatusData: SyncStatusData): Flowable<List<ProgramViewModel>>
    fun programModels(syncStatusData: SyncStatusData): Flowable<List<ProgramViewModel>>
    fun aggregatesModels(syncStatusData: SyncStatusData): Flowable<List<ProgramViewModel>>
    fun clearCache()
    fun getDataStoreData(): Flow<List<DataStoreEntry>>
    fun getFilteredDataStore():  Flow<DataStoreAppConfig?>
}
