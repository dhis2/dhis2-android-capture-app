package org.dhis2.android.rtsm.services

import androidx.lifecycle.LiveData
import androidx.work.WorkInfo
import org.dhis2.android.rtsm.data.WorkItem

interface WorkManagerController {
    fun sync(workName: String, metadataTag: String, dataTag: String)
    fun sync(workItem: WorkItem)

    fun getWorkInfo(workName: String): LiveData<List<WorkInfo>>
    fun getWorkInfoByTag(tag: String): LiveData<List<WorkInfo>>

    fun cancelUniqueWork(workName: String)
    fun cancelWorkByTag(tag: String)
    fun cancelAllWork()
}