package org.dhis2.usescases.settings.models

import org.hisp.dhis.android.core.imports.TrackerImportConflict
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.ForeignKeyViolation

class ErrorModelMapper(private val fkMessage: String) {

    companion object {
        const val FK = "FK"
    }

    fun mapD2Error(errors: List<D2Error>): List<ErrorViewModel> {
        return errors.map {
            map(it)
        }
    }

    fun map(error: D2Error): ErrorViewModel {
        return ErrorViewModel(
            error.created(),
            error.httpErrorCode().toString(),
            error.errorDescription(),
            error.errorComponent().name
        )
    }

    fun mapConflict(conflicts: List<TrackerImportConflict>): List<ErrorViewModel> {
        return conflicts.map {
            map(it)
        }
    }

    fun map(conflict: TrackerImportConflict): ErrorViewModel {
        return ErrorViewModel(
            conflict.created(),
            conflict.errorCode(),
            conflict.conflict(),
            conflict.status()?.name
        )
    }

    fun mapFKViolation(fKViolations: List<ForeignKeyViolation>): List<ErrorViewModel> {
        return fKViolations.map {
            map(it)
        }
    }

    fun map(fKViolation: ForeignKeyViolation): ErrorViewModel {
        val toTable = fKViolation.toTable() ?: ""
        val fromTable = fKViolation.fromTable() ?: ""
        val toUid = fKViolation.notFoundValue() ?: ""
        val fromUid = fKViolation.fromObjectUid() ?: ""
        return ErrorViewModel(
            fKViolation.created(),
            FK,
            fkMessage.format(toTable, toUid, fromTable, fromUid),
            ""
        )
    }
}
