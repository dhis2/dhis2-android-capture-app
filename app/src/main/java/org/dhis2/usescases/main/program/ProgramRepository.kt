package org.dhis2.usescases.main.program

import io.reactivex.Flowable
import org.dhis2.data.service.SyncStatusData

interface ProgramRepository {
    fun homeItems(syncStatusData: SyncStatusData): Flowable<List<ProgramUiModel>>
    fun programModels(syncStatusData: SyncStatusData): Flowable<List<ProgramUiModel>>
    fun aggregatesModels(syncStatusData: SyncStatusData): Flowable<List<ProgramUiModel>>
    fun clearCache()
}
