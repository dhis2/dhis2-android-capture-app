package org.dhis2.usescases.settings.domain

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.usescases.settings.SettingsRepository
import org.dhis2.usescases.settings.models.ErrorModelMapper
import org.dhis2.usescases.settings.models.ErrorViewModel

class GetSyncErrors(
    private val settingsRepository: SettingsRepository,
    private val errorMapper: ErrorModelMapper,
) : UseCase<Unit, List<ErrorViewModel>> {
    override suspend fun invoke(input: Unit): Result<List<ErrorViewModel>> =
        try {
            val errors: MutableList<ErrorViewModel> = ArrayList()
            errors.addAll(
                errorMapper.mapD2Error(settingsRepository.d2Errors()),
            )
            errors.addAll(
                errorMapper.mapConflict(settingsRepository.trackerImportConflicts()),
            )

            return Result.success(errors.sortedByDescending { it.creationDate })
        } catch (domainError: DomainError) {
            Result.failure(domainError)
        }

    // Used by SyncManagerPresenter's legacy Java dialog until it is cut over to SyncErrorLogViewModel.
    suspend fun invokeLegacy(): List<ErrorViewModel> {
        val errors: MutableList<ErrorViewModel> = ArrayList()
        errors.addAll(errorMapper.mapD2ErrorLegacy(settingsRepository.d2Errors()))
        errors.addAll(errorMapper.mapConflict(settingsRepository.trackerImportConflicts()))
        errors.addAll(errorMapper.mapFKViolation(settingsRepository.foreignKeyViolations()))
        return errors.sortedBy { it.creationDate }
    }
}