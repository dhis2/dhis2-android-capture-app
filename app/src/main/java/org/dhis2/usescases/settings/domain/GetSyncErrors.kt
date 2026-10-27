package org.dhis2.usescases.settings.domain

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.usescases.settings.SettingsRepository
import org.dhis2.usescases.settings.models.ErrorModelMapper
import org.dhis2.usescases.settings.models.ErrorViewModel
import org.dhis2.usescases.settings.ui.ErrorLogType

class GetSyncErrors(
    private val errorLogType: ErrorLogType,
    private val settingsRepository: SettingsRepository,
    private val errorMapper: ErrorModelMapper,
) : UseCase<Unit, List<ErrorViewModel>> {
    override suspend fun invoke(input: Unit): Result<List<ErrorViewModel>> =
        try {
            val errors =
                buildList {
                    when (errorLogType) {
                        ErrorLogType.MetadataIntegrity ->
                            addAll(
                                errorMapper.mapFKViolation(settingsRepository.foreignKeyViolations()),
                            )

                        ErrorLogType.Sync -> {
                            addAll(
                                errorMapper.mapD2Error(settingsRepository.d2Errors()),
                            )
                            addAll(
                                errorMapper.mapConflict(settingsRepository.trackerImportConflicts()),
                            )
                        }
                    }
                }

            return Result.success(errors.sortedByDescending { it.creationDate })
        } catch (domainError: DomainError) {
            Result.failure(domainError)
        }
}
