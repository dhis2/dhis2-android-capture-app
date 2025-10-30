package org.dhis2.mobile.aggregates.domain

import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository

internal class UploadFile(
    val repository: DataSetInstanceRepository,
) {
    suspend operator fun invoke(
        path: String,
        isImage: Boolean = false,
    ): Result<String?> = repository.uploadFile(path, isImage)
}
