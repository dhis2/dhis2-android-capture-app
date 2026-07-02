package org.dhis2.mobile.sync.domain

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.sync.data.SyncDataSetRepository

internal typealias DataSetUid = String

internal class SyncDataSet(
    private val syncDataSetRepository: SyncDataSetRepository,
) : UseCase<DataSetUid, Unit> {
    override suspend fun invoke(input: DataSetUid): Result<Unit> =
        try {
            syncDataSetRepository.uploadDataSet(input)
            syncDataSetRepository.uploadCompleteRegistration(input)
            syncDataSetRepository.downloadDataSet(input)
            return Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
}
