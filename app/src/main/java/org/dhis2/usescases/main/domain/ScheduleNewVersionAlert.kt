package org.dhis2.usescases.main.domain

import androidx.work.ExistingWorkPolicy
import org.dhis2.commons.Constants
import org.dhis2.data.service.VersionRepository
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.data.service.workManager.WorkerItem
import org.dhis2.data.service.workManager.WorkerType
import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import kotlin.time.Duration.Companion.days

class ScheduleNewVersionAlert(
    private val workManagerController: WorkManagerController,
    private val versionRepository: VersionRepository,
) : UseCase<Unit, Unit> {
    override suspend fun invoke(input: Unit): Result<Unit> =
        try {
            val workerItem =
                WorkerItem(
                    Constants.NEW_APP_VERSION,
                    WorkerType.NEW_VERSION,
                    delayInSeconds = 1.days.inWholeSeconds,
                    policy = ExistingWorkPolicy.REPLACE,
                )
            workManagerController.beginUniqueWork(workerItem)
            versionRepository.removeVersionInfo()
            Result.success(Unit)
        } catch (domainError: DomainError) {
            Result.failure(domainError)
        }
}
