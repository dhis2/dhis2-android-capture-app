package org.dhis2.mobile.sync.domain

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.commons.session.SessionRenewalNotifier
import org.dhis2.mobile.sync.data.SyncDataValueRepository
import org.dhis2.mobile.sync.model.SyncDataValueInput

internal class SyncDataValue(
    private val syncDataValueRepository: SyncDataValueRepository,
    private val sessionRenewalNotifier: SessionRenewalNotifier,
) : UseCase<SyncDataValueInput, Unit> {
    override suspend fun invoke(input: SyncDataValueInput): Result<Unit> =
        try {
            syncDataValueRepository.uploadDataValues(
                dataSetUid = input.dataSetUid,
                orgUnitUid = input.orgUnitUid,
                periodId = input.periodId,
                attributeOptionComboUid = input.attrOptionComboUid,
                categoryOptionComboUids = input.categoryOptionComboUid,
            )
            syncDataValueRepository.uploadCompleteRegistrations(
                dataSetUid = input.dataSetUid,
                orgUnitUid = input.orgUnitUid,
                periodId = input.periodId,
                attributeOptionComboUid = input.attrOptionComboUid,
            )
            syncDataValueRepository.downloadDataValues(
                dataSetUid = input.dataSetUid,
                orgUnitUid = input.orgUnitUid,
                periodId = input.periodId,
                attributeOptionComboUid = input.attrOptionComboUid,
            )
            Result.success(Unit)
        } catch (domainError: DomainError) {
            sessionRenewalNotifier.notifyIfRequired(domainError)
            Result.failure(domainError)
        } catch (e: Exception) {
            Result.failure(e)
        }
}
