package org.dhis2.usescases.settings.models

import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import org.dhis2.mobile.commons.dates.dateTimeFormat
import org.dhis2.mobile.commons.error.HttpStatusMessageProvider
import org.hisp.dhis.android.core.imports.TrackerImportConflict
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.ForeignKeyViolation
import java.util.Date
import kotlin.time.Instant

class ErrorModelMapper(
    private val fkMessage: String,
    private val httpStatusMessageProvider: HttpStatusMessageProvider = HttpStatusMessageProvider(),
) {
    companion object {
        const val FK = "FK"
    }

    suspend fun mapD2Error(errors: List<D2Error>): List<ErrorViewModel> =
        errors.map {
            map(it)
        }

    private suspend fun map(error: D2Error): ErrorViewModel =
        ErrorViewModel(
            creationDate = error.created(),
            creationDateLabel = dateLabel(error.created),
            errorCode = error.httpErrorCode?.let {errorCode->
                val message = httpStatusMessageProvider.httpStatusMessage(errorCode)
                "$errorCode $message"
            },
            errorDescription = error.errorDescription(),
            errorComponent = error.errorComponent()?.name ?: "",
        )

    // Used by SyncManagerPresenter's legacy Java dialog until it is cut over to SyncErrorLogViewModel.
    fun mapD2ErrorLegacy(errors: List<D2Error>): List<ErrorViewModel> =
        errors.map {
            mapLegacy(it)
        }

    private fun mapLegacy(error: D2Error): ErrorViewModel =
        ErrorViewModel(
            creationDate = error.created(),
            errorCode = error.httpErrorCode().toString(),
            errorDescription = error.errorDescription(),
            errorComponent = error.errorComponent()?.name ?: "",
        )

    fun mapConflict(conflicts: List<TrackerImportConflict>): List<ErrorViewModel> =
        conflicts.map {
            map(it)
        }

    fun map(conflict: TrackerImportConflict): ErrorViewModel =
        ErrorViewModel(
            conflict.created(),
            creationDateLabel = dateLabel(conflict.created),
            conflict.errorCode(),
            conflict.displayDescription() ?: conflict.conflict(),
            conflict.status()?.name,
        )

    fun mapFKViolation(fKViolations: List<ForeignKeyViolation>): List<ErrorViewModel> =
        fKViolations.map {
            map(it)
        }

    fun map(fKViolation: ForeignKeyViolation): ErrorViewModel {
        val toTable = fKViolation.toTable() ?: ""
        val fromTable = fKViolation.fromTable() ?: ""
        val toUid = fKViolation.notFoundValue() ?: ""
        val fromUid = fKViolation.fromObjectUid() ?: ""
        return ErrorViewModel(
            fKViolation.created(),
            creationDateLabel = dateLabel(fKViolation.created),
            FK,
            fkMessage.format(toTable, toUid, fromTable, fromUid),
            "",
        )
    }

    private fun dateLabel(date: Date?) =
        date?.let {
            Instant
                .fromEpochMilliseconds(it.time)
                .toLocalDateTime(
                    TimeZone.currentSystemDefault(),
                ).format(dateTimeFormat)
        }
}