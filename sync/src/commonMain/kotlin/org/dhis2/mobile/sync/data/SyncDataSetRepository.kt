package org.dhis2.mobile.sync.data

import org.dhis2.mobile.sync.domain.DataSetUid

internal interface SyncDataSetRepository {
    suspend fun uploadDataSet(dataSetUid: DataSetUid)

    suspend fun uploadCompleteRegistration(dataSetUid: DataSetUid)

    suspend fun downloadDataSet(dataSetUid: DataSetUid)
}
