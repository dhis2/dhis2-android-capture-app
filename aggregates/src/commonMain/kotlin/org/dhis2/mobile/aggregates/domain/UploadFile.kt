package org.dhis2.mobile.aggregates.domain

import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository

internal class UploadFile(
    val repository: DataSetInstanceRepository,
) {
    suspend operator fun invoke(
        path: String,
        dataElementUid: String,
        dataSetUid: String,
        isImage: Boolean = false,
    ): Result<String?> = repository.uploadFile(path, dataElementUid, dataSetUid, isImage)
}
