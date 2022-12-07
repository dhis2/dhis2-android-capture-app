package org.dhis2.usescases.main.program

import io.reactivex.Flowable
import org.dhis2.data.service.SyncStatusData

internal interface ProgramRepository {
    fun homeItems(syncStatusData: SyncStatusData): Flowable<List<ProgramViewModel>>
    fun programModels(syncStatusData: SyncStatusData): Flowable<List<ProgramViewModel>>
    fun aggregatesModels(syncStatusData: SyncStatusData): Flowable<List<ProgramViewModel>>
    fun clearCache()
}
