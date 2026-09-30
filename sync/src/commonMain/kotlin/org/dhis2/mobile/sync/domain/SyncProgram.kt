package org.dhis2.mobile.sync.domain

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.commons.session.SessionRenewalNotifier
import org.dhis2.mobile.sync.data.SyncProgramRepository
import org.dhis2.mobile.sync.model.ProgramType

internal typealias ProgramUid = String

internal class SyncProgram(
    private val syncProgramRepository: SyncProgramRepository,
    private val syncStatusController: SyncStatusController,
    private val sessionRenewalNotifier: SessionRenewalNotifier,
) : UseCase<ProgramUid, Unit> {
    override suspend fun invoke(input: ProgramUid): Result<Unit> =
        try {
            val isSynced =
                when (syncProgramRepository.getProgramType(input)) {
                    ProgramType.Event -> syncEvents(input)
                    ProgramType.Tracker -> syncTracker(input)
                    ProgramType.None -> return Result.failure(Exception("Unknown program"))
                }

            if (isSynced) {
                syncStatusController.updateSingleProgramToSuccess(input)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Status is not synced"))
            }
        } catch (domainError: DomainError) {
            sessionRenewalNotifier.notifyIfRequired(domainError)
            Result.failure(domainError)
        } catch (e: Exception) {
            Result.failure(e)
        }

    private suspend fun syncEvents(programUid: ProgramUid): Boolean =
        with(syncProgramRepository) {
            uploadEventProgram(programUid)
            val isSynced = allEventsAreSynced(programUid)
            if (isSynced) {
                downloadEventProgram(programUid)
                downloadFileResources(programUid)
            }
            isSynced
        }

    private suspend fun syncTracker(programUid: ProgramUid): Boolean =
        with(syncProgramRepository) {
            uploadTrackerProgram(programUid)
            val isSynced = allTeisAreSynced(programUid)
            if (isSynced) {
                downloadTrackerProgram(programUid)
                downloadFileResources(programUid)
            }
            isSynced
        }
}
