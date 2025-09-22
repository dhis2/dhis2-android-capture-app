package org.dhis2.usescases.development

import kotlinx.coroutines.runBlocking
import org.dhis2.commons.bindings.enrollment
import org.dhis2.commons.bindings.event
import org.dhis2.commons.bindings.tei
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.datavalue.DataValue
import org.hisp.dhis.android.core.datavalue.DataValueConflict
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.imports.ImportStatus
import org.hisp.dhis.android.core.imports.TrackerImportConflict
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.hisp.dhis.android.persistence.datavalue.DataValueConflictTableInfo
import org.hisp.dhis.android.persistence.imports.TrackerImportConflictTableInfo
import timber.log.Timber
import kotlin.random.Random

class ConflictGenerator(
    private val d2: D2,
) {
    fun generate() {
        generateErrorInEnrollment(ImportStatus.ERROR)
        generateErrorInEvent(ImportStatus.ERROR)
        generateConflictInDataSetValue(ImportStatus.ERROR)
        generateErrorInEnrollment(ImportStatus.WARNING)
        generateErrorInEvent(ImportStatus.WARNING)
        generateConflictInDataSetValue(ImportStatus.WARNING)
    }

    private fun generateErrorInEnrollment(importStatus: ImportStatus) {
        val enrollmentUid = generateConflictInAttribute(importStatus)
        generateConflictInDataElementForEnrollment(
            enrollmentUid,
            importStatus,
        )?.let { enrollmentEventUid ->
            generateConflictInEventForEnrollment(enrollmentEventUid, importStatus)
        }
        generateConflictInEnrollment(enrollmentUid, importStatus)
        generateConflictInTeiForEnrollment(enrollmentUid, importStatus)
    }

    private fun generateErrorInEvent(importStatus: ImportStatus) {
        val eventUid = generateConflictInDataElement(importStatus)
        generateConflictInEvent(eventUid, importStatus)
    }

    fun clear() {
        d2
            .importModule()
            .trackerImportConflicts()
            .byConflict()
            .like("Generated%")
            .blockingGet()
            .forEach { trackerImportConflict ->
                trackerImportConflict.apply {
                    trackedEntityInstance()?.let { teiUid ->
                        val tei =
                            d2
                                .tei(teiUid)
                                ?.toBuilder()
                                ?.syncState(State.SYNCED)
                                ?.aggregatedSyncState(State.SYNCED)
                                ?.build()

                        tei?.let {
                            runBlocking {
                                d2.databaseAdapter().upsertObject(it, TrackedEntityInstance::class)
                            }
                        }
                    }
                    enrollment()?.let { enrollmentUid ->
                        val enrollment =
                            d2
                                .enrollment(enrollmentUid)
                                ?.toBuilder()
                                ?.syncState(State.SYNCED)
                                ?.aggregatedSyncState(State.SYNCED)
                                ?.build()

                        enrollment?.let {
                            runBlocking {
                                d2.databaseAdapter().upsertObject(enrollment, Enrollment::class)
                            }
                        }
                    }
                    event()?.let { eventUid ->
                        val event =
                            d2
                                .event(eventUid)
                                ?.toBuilder()
                                ?.syncState(State.SYNCED)
                                ?.aggregatedSyncState(State.SYNCED)
                                ?.build()
                        event?.let {
                            runBlocking {
                                d2.databaseAdapter().upsertObject(event, Event::class)
                            }
                        }
                    }
                }
            }.also {
                runBlocking {
                    d2.databaseAdapter().delete(
                        TrackerImportConflictTableInfo.TABLE_INFO.name(),
                        "conflict LIKE 'Generated%'",
                        emptyArray(),
                    )
                }
            }

        d2
            .dataValueModule()
            .dataValueConflicts()
            .byConflict()
            .like("Generated%")
            .blockingGet()
            .forEach { dataValueConflict ->
                if (dataValueConflict.period() != null &&
                    dataValueConflict.orgUnit() != null &&
                    dataValueConflict.dataElement() != null &&
                    dataValueConflict.categoryOptionCombo() != null &&
                    dataValueConflict.attributeOptionCombo() != null
                ) {
                    val dataValue =
                        d2
                            .dataValueModule()
                            .dataValues()
                            .value(
                                dataValueConflict.period()!!,
                                dataValueConflict.orgUnit()!!,
                                dataValueConflict.dataElement()!!,
                                dataValueConflict.categoryOptionCombo()!!,
                                dataValueConflict.attributeOptionCombo()!!,
                            ).blockingGet()
                    val dv =
                        dataValue?.toBuilder()?.syncState(State.SYNCED)?.build()
                    dv?.let {
                        runBlocking {
                            d2.databaseAdapter().upsertObject(it, DataValue::class)
                        }
                    }
                }
            }.also {
                runBlocking {
                    d2.databaseAdapter().delete(
                        DataValueConflictTableInfo.TABLE_INFO.name(),
                        "conflict LIKE 'Generated%'",
                        emptyArray(),
                    )
                }
            }
    }

    private fun generateConflictInAttribute(importStatus: ImportStatus): String {
        val attributeValue =
            d2
                .trackedEntityModule()
                .trackedEntityAttributeValues()
                .blockingGet()
                .let { attributeValues ->
                    attributeValues[Random.nextInt(attributeValues.size)]
                }

        val programAttribute =
            d2
                .programModule()
                .programTrackedEntityAttributes()
                .byTrackedEntityAttribute()
                .eq(attributeValue.trackedEntityAttribute())
                .one()
                .blockingGet()

        val programUid = programAttribute?.program()!!.uid()
        val enrollment =
            d2
                .enrollmentModule()
                .enrollments()
                .byTrackedEntityInstance()
                .eq(attributeValue.trackedEntityInstance())
                .byProgram()
                .eq(programUid)
                .one()
                .blockingGet() ?: return generateConflictInAttribute(importStatus)

        val enrollmentUid = enrollment.uid()
        val teiUid = attributeValue.trackedEntityInstance()

        val conflict =
            TrackerImportConflict
                .builder()
                .conflict("Generated error conflict in attribute")
                .value(attributeValue.value())
                .trackedEntityAttribute(attributeValue.trackedEntityAttribute())
                .trackedEntityInstance(attributeValue.trackedEntityInstance()!!)
                .enrollment(enrollmentUid)
                .displayDescription("Generated error description in attribute")
                .status(importStatus)
                .build()

        try {
            runBlocking {
                d2.databaseAdapter().upsertObject(conflict, TrackerImportConflict::class)
            }
            enrollmentUid?.let {
                runBlocking {
                    d2
                        .databaseAdapter()
                        .execSQL(updateEnrollment(enrollmentUid, importStatus.toSyncState().name))
                }
            }
            teiUid?.let {
                runBlocking {
                    d2
                        .databaseAdapter()
                        .execSQL(updateTei(teiUid, importStatus.toSyncState().name))
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
        return enrollment.uid()
    }

    private fun generateConflictInDataElementForEnrollment(
        enrollmentUid: String,
        importStatus: ImportStatus,
    ): String? {
        val event =
            d2
                .eventModule()
                .events()
                .byEnrollmentUid()
                .eq(enrollmentUid)
                .blockingGetUids()

        if (event.isEmpty()) return null

        val attributeValue =
            d2
                .trackedEntityModule()
                .trackedEntityDataValues()
                .byEvent()
                .`in`(event)
                .blockingGet()
                .let { attributeValues ->
                    if (attributeValues.isNotEmpty()) {
                        attributeValues[Random.nextInt(attributeValues.size)]
                    } else {
                        null
                    }
                } ?: return event.first()

        val enrollment =
            d2
                .enrollmentModule()
                .enrollments()
                .uid(enrollmentUid)
                .blockingGet()
        val teiUid = enrollment?.trackedEntityInstance()

        val conflict =
            TrackerImportConflict
                .builder()
                .conflict("Generated error conflict in data element")
                .value(attributeValue.value())
                .event(attributeValue.event())
                .dataElement(attributeValue.dataElement())
                .trackedEntityInstance(teiUid)
                .enrollment(enrollmentUid)
                .displayDescription("Generated error description in data element")
                .status(importStatus)
                .build()

        try {
            runBlocking {
                d2.databaseAdapter().upsertObject(conflict, TrackerImportConflict::class)
            }
            attributeValue.event()?.let { eventUid ->
                runBlocking {
                    d2
                        .databaseAdapter()
                        .execSQL(updateEvent(eventUid, importStatus.toSyncState().name))
                }
            }
            runBlocking {
                d2
                    .databaseAdapter()
                    .execSQL(updateEnrollment(enrollmentUid, importStatus.toSyncState().name))
            }
            teiUid?.let {
                runBlocking {
                    d2
                        .databaseAdapter()
                        .execSQL(updateTei(teiUid, importStatus.toSyncState().name))
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
        return attributeValue.event()!!
    }

    private fun generateConflictInEventForEnrollment(
        eventUid: String,
        importStatus: ImportStatus,
    ) {
        val conflict =
            TrackerImportConflict
                .builder()
                .conflict("Generated error conflict in enrollment event")
                .event(eventUid)
                .displayDescription("Generated error description in enrollment event")
                .status(importStatus)
                .build()

        try {
            runBlocking {
                d2.databaseAdapter().upsertObject(conflict, TrackerImportConflict::class)
                d2.databaseAdapter().execSQL(updateEvent(eventUid, importStatus.toSyncState().name))
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun generateConflictInEnrollment(
        enrollmentUid: String,
        importStatus: ImportStatus,
    ) {
        val enrollment = d2.enrollment(enrollmentUid)
        val conflict =
            TrackerImportConflict
                .builder()
                .conflict("Generated error conflict in enrollment")
                .trackedEntityInstance(enrollment?.trackedEntityInstance())
                .enrollment(
                    enrollmentUid,
                ).displayDescription("Generated error description in enrollment")
                .status(importStatus)
                .build()
        try {
            runBlocking {
                d2.databaseAdapter().upsertObject(conflict, TrackerImportConflict::class)
                d2
                    .databaseAdapter()
                    .execSQL(updateEnrollment(enrollmentUid, importStatus.toSyncState().name))
            }

            enrollment?.trackedEntityInstance()?.let {
                runBlocking {
                    d2.databaseAdapter().execSQL(updateTei(it, importStatus.toSyncState().name))
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun generateConflictInTeiForEnrollment(
        enrollmentUid: String,
        importStatus: ImportStatus,
    ) {
        val enrollment = d2.enrollment(enrollmentUid)
        val conflict =
            TrackerImportConflict
                .builder()
                .conflict("Generated error conflict in TEI level")
                .trackedEntityInstance(enrollment?.trackedEntityInstance())
                .displayDescription("Generated error description in TEI level")
                .status(importStatus)
                .build()
        try {
            runBlocking {
                d2.databaseAdapter().upsertObject(conflict, TrackerImportConflict::class)
            }
            enrollment?.trackedEntityInstance()?.let {
                runBlocking {
                    d2.databaseAdapter().execSQL(updateTei(it, importStatus.toSyncState().name))
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun generateConflictInDataElement(importStatus: ImportStatus): String {
        var event: Event? = null
        var attributeValue: TrackedEntityDataValue? = null
        while (event == null) {
            attributeValue =
                d2
                    .trackedEntityModule()
                    .trackedEntityDataValues()
                    .blockingGet()
                    .let { attributeValues ->
                        attributeValues[Random.nextInt(attributeValues.size)]
                    }

            event =
                d2
                    .eventModule()
                    .events()
                    .uid(attributeValue.event())
                    .blockingGet()
                    ?.takeIf { it.enrollment() == null }
        }

        val conflict =
            TrackerImportConflict
                .builder()
                .conflict("Generated error conflict in data element")
                .value(attributeValue!!.value())
                .event(attributeValue.event())
                .dataElement(attributeValue.dataElement())
                .displayDescription("Generated error description in data element")
                .status(importStatus)
                .build()

        try {
            runBlocking {
                d2.databaseAdapter().upsertObject(conflict, TrackerImportConflict::class)
                d2.databaseAdapter().execSQL(updateEvent(event.uid(), importStatus.toSyncState().name))
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
        return event.uid()
    }

    fun generateConflictInEvent(
        eventUid: String,
        importStatus: ImportStatus,
    ) {
        val conflict =
            TrackerImportConflict
                .builder()
                .conflict("Generated error conflict in event")
                .event(eventUid)
                .displayDescription("Generated error description in event")
                .status(importStatus)
                .build()
        try {
            runBlocking {
                d2.databaseAdapter().upsertObject(conflict, TrackerImportConflict::class)
                d2.databaseAdapter().execSQL(updateEvent(eventUid, importStatus.toSyncState().name))
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun generateStatusConflictInDataSet(importStatus: ImportStatus) {
        val attributeValue =
            d2
                .dataValueModule()
                .dataValues()
                .bySyncState()
                .eq(State.TO_UPDATE)
                .blockingGet()
                .first()
        val conflict =
            DataValueConflict
                .builder()
                .conflict("Generated error conflict in data value")
                .value(attributeValue.value())
                .dataElement(attributeValue.dataElement())
                .period(attributeValue.period())
                .orgUnit(attributeValue.organisationUnit())
                .attributeOptionCombo(attributeValue.attributeOptionCombo())
                .categoryOptionCombo(attributeValue.categoryOptionCombo())
                .displayDescription("Generated error description in data value")
                .status(importStatus)
                .build()
        val updatedDataValue =
            attributeValue
                .toBuilder()
                .syncState(importStatus.toSyncState())
                .build()
        try {
            runBlocking {
                d2.databaseAdapter().upsertObject(conflict, DataValueConflict::class)
                d2.databaseAdapter().upsertObject(updatedDataValue, DataValue::class)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun generateConflictInDataSetValue(importStatus: ImportStatus) {
        val attributeValue =
            d2.dataValueModule().dataValues().blockingGet().let { attributeValues ->
                attributeValues[Random.nextInt(attributeValues.size)]
            }

        val conflict =
            DataValueConflict
                .builder()
                .conflict("Generated error conflict in data value")
                .value(attributeValue.value())
                .dataElement(attributeValue.dataElement())
                .period(attributeValue.period())
                .orgUnit(attributeValue.organisationUnit())
                .attributeOptionCombo(attributeValue.attributeOptionCombo())
                .categoryOptionCombo(attributeValue.categoryOptionCombo())
                .displayDescription("Generated error description in data value")
                .status(importStatus)
                .build()
        val updatedDataValue =
            attributeValue
                .toBuilder()
                .syncState(State.ERROR)
                .build()
        try {
            runBlocking {
                d2.databaseAdapter().upsertObject(conflict, DataValueConflict::class)
                d2.databaseAdapter().upsertObject(updatedDataValue, DataValue::class)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun updateEnrollment(
        enrollmentUid: String,
        syncState: String,
    ): String =
        "UPDATE Enrollment SET syncState = '$syncState'," +
            " aggregatedSyncState = '$syncState' where uid = '$enrollmentUid'"

    private fun updateTei(
        teiUid: String,
        syncState: String,
    ) = "UPDATE TrackedEntityInstance SET aggregatedSyncState = '$syncState' where uid = '$teiUid'"

    private fun updateEvent(
        eventUid: String,
        syncState: String,
    ) = "UPDATE Event SET aggregatedSyncState = '$syncState' where uid = '$eventUid'"

    private fun ImportStatus.toSyncState() =
        when (this) {
            ImportStatus.SUCCESS -> State.SYNCED
            ImportStatus.WARNING -> State.WARNING
            ImportStatus.ERROR -> State.ERROR
        }
}
