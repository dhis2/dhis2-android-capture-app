package org.dhis2.usescases.settings.domain

import org.dhis2.usescases.settings.SettingsRepository
import org.dhis2.usescases.settings.models.ErrorModelMapper
import org.dhis2.usescases.settings.models.ErrorViewModel

class GetSyncErrors(
    private val settingsRepository: SettingsRepository,
    private val errorMapper: ErrorModelMapper,
) {
    suspend operator fun invoke(): List<ErrorViewModel> {
        val errors: MutableList<ErrorViewModel> = ArrayList()
        errors.addAll(
            errorMapper.mapD2Error(settingsRepository.d2Errors()),
        )
        errors.addAll(
            errorMapper.mapConflict(settingsRepository.trackerImportConflicts()),
        )
        errors.addAll(
            errorMapper.mapFKViolation(settingsRepository.foreignKeyViolations()),
        )

        return errors.sortedBy { it.creationDate }
    }
}
