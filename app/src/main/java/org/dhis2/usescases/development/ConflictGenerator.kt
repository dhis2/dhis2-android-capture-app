package org.dhis2.usescases.development

import kotlin.random.Random
import org.dhis2.metadata.usecases.sdkextensions.enrollment
import org.dhis2.metadata.usecases.sdkextensions.event
import org.dhis2.metadata.usecases.sdkextensions.tei
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.datavalue.DataValueConflict
import org.hisp.dhis.android.core.datavalue.DataValueConflictTableInfo
import org.hisp.dhis.android.core.datavalue.DataValueTableInfo
import org.hisp.dhis.android.core.enrollment.EnrollmentTableInfo
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventTableInfo
import org.hisp.dhis.android.core.imports.ImportStatus
import org.hisp.dhis.android.core.imports.TrackerImportConflict
import org.hisp.dhis.android.core.imports.TrackerImportConflictTableInfo
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceTableInfo
import timber.log.Timber

class ConflictGenerator(private val d2: D2) {
    fun generate() {
        val enrollmentUid = generateConflictInAttribute()
        generateConflictInDataElementForEnrollment(enrollmentUid)?.let { enrollmentEventUid ->
            generateConflictInEventForEnrollment(enrollmentEventUid)
        }
        generateConflictInEnrollment(enrollmentUid)
        generateConflictInTeiForEnrollment(enrollmentUid)
        val eventUid = generateConflictInDataElement()
        generateConflictInEvent(eventUid)
        generateConflictInDataSetValue()
    }

    fun clear() {
        d2.importModule().trackerImportConflicts()
            .byConflict().like("Generated%")
            .blockingGet().forEach { trackerImportConflict ->
                trackerImportConflict.apply {
                    trackedEntityInstance()?.let { teiUid ->
                        val cv = d2.tei(teiUid).toBuilder().syncState(State.SYNCED)
                            .aggregatedSyncState(State.SYNCED).build().toContentValues()
                        d2.databaseAdapter().update(
                            TrackedEntityInstanceTableInfo.TABLE_INFO.name(),
                            cv,
                            "uid = '$teiUid'",
                            emptyArray()
                        )
                    }
                    enrollment()?.let { enrollmentUid ->
                        val cv = d2.enrollment(enrollmentUid).toBuilder().syncState(State.SYNCED)
                            .aggregatedSyncState(State.SYNCED).build().toContentValues()
                        d2.databaseAdapter().update(
                            EnrollmentTableInfo.TABLE_INFO.name(),
                            cv,
                            "uid = '$enrollmentUid'",
                            emptyArray()
                        )
                    }
                    event()?.let { eventUid ->
                        val cv = d2.event(eventUid).toBuilder().syncState(State.SYNCED)
                            .aggregatedSyncState(State.SYNCED).build().toContentValues()
                        d2.databaseAdapter().update(
                            EventTableInfo.TABLE_INFO.name(),
                            cv,
                            "uid = '$eventUid'",
                            emptyArray()
                        )
                    }
                }
            }.also {
                d2.databaseAdapter().delete(
                    TrackerImportConflictTableInfo.TABLE_INFO.name(),
                    "conflict LIKE 'Generated%'",
                    emptyArray()
                )
            }

        d2.dataValueModule().dataValueConflicts()
            .byConflict().like("Generated%")
            .blockingGet().forEach { dataValueConflict ->
                val dataValue = d2.dataValueModule().dataValues().value(
                    dataValueConflict.period(),
                    dataValueConflict.orgUnit(),
                    dataValueConflict.dataElement(),
                    dataValueConflict.categoryOptionCombo(),
                    dataValueConflict.attributeOptionCombo()
                ).blockingGet()
                val cv = dataValue.toBuilder().syncState(State.SYNCED).build().toContentValues()
                d2.databaseAdapter().update(
                    DataValueTableInfo.TABLE_INFO.name(),
                    cv,
                    "_id = ${dataValue.id()}",
                    emptyArray()
                )
            }.also {
                d2.databaseAdapter().delete(
                    DataValueConflictTableInfo.TABLE_INFO.name(),
                    "conflict LIKE 'Generated%'",
                    emptyArray()
                )
            }
    }

    private fun generateConflictInAttribute(): String {
        val attributeValue = d2.trackedEntityModule().trackedEntityAttributeValues().blockingGet()
            ?.let { attributeValues ->
                attributeValues[Random.nextInt(attributeValues.size)]
            }!!

        val programAttribute =
            d2.programModule().programTrackedEntityAttributes().byTrackedEntityAttribute()
                .eq(attributeValue.trackedEntityAttribute()).one().blockingGet()

        val programUid = programAttribute.program()!!.uid()
        val enrollment = d2.enrollmentModule().enrollments().byTrackedEntityInstance()
            .eq(attributeValue.trackedEntityInstance()).byProgram().eq(programUid).one()
            .blockingGet()
        val enrollmentUid = enrollment?.uid()
        val teiUid = attributeValue.trackedEntityInstance()

        val build =
            TrackerImportConflict.builder().conflict("Generated error conflict in attribute")
                .value(attributeValue.value())
                .trackedEntityAttribute(attributeValue.trackedEntityAttribute())
                .trackedEntityInstance(attributeValue.trackedEntityInstance()!!)
                .enrollment(enrollmentUid)
                .displayDescription("Generated error description in attribute")
                .status(ImportStatus.ERROR).build()
        val cv = build.toContentValues()
        try {
            d2.databaseAdapter().insert("TrackerImportConflict", null, cv)
            enrollmentUid?.let {
                d2.databaseAdapter().execSQL(updateEnrollment(enrollmentUid))
            }
            teiUid?.let {
                d2.databaseAdapter().execSQL(updateTei(teiUid))
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
        return enrollment.uid()
    }

    private fun generateConflictInDataElementForEnrollment(enrollmentUid: String): String? {
        val event = d2.eventModule().events().byEnrollmentUid().eq(enrollmentUid).blockingGetUids()

        if (event.isEmpty()) return null

        val attributeValue =
            d2.trackedEntityModule().trackedEntityDataValues().byEvent().`in`(event).blockingGet()
                ?.let { attributeValues ->
                    if (attributeValues.isNotEmpty()) {
                        attributeValues[Random.nextInt(attributeValues.size)]
                    } else {
                        null
                    }
                } ?: return event.first()

        val enrollment = d2.enrollmentModule().enrollments().uid(enrollmentUid).blockingGet()
        val teiUid = enrollment?.trackedEntityInstance()

        val build =
            TrackerImportConflict.builder()
                .conflict("Generated error conflict in data element")
                .value(attributeValue.value()).event(attributeValue.event())
                .dataElement(attributeValue.dataElement()).trackedEntityInstance(teiUid)
                .enrollment(enrollmentUid)
                .displayDescription("Generated error description in data element")
                .status(ImportStatus.ERROR).build()
        val cv = build.toContentValues()
        try {
            d2.databaseAdapter().insert("TrackerImportConflict", null, cv)
            attributeValue.event()?.let { eventUid ->
                d2.databaseAdapter().execSQL(updateEvent(eventUid))
            }
            enrollmentUid.let {
                d2.databaseAdapter().execSQL(updateEnrollment(enrollmentUid))
            }
            teiUid?.let {
                d2.databaseAdapter().execSQL(updateTei(teiUid))
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
        return attributeValue.event()!!
    }

    private fun generateConflictInEventForEnrollment(eventUid: String) {
        val build =
            TrackerImportConflict.builder()
                .conflict("Generated error conflict in enrollment event")
                .event(eventUid)
                .displayDescription("Generated error description in enrollment event")
                .status(ImportStatus.ERROR).build()
        val cv = build.toContentValues()
        try {
            d2.databaseAdapter().insert("TrackerImportConflict", null, cv)
            d2.databaseAdapter().execSQL(updateEvent(eventUid))
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun generateConflictInEnrollment(enrollmentUid: String) {
        val enrollment = d2.enrollment(enrollmentUid)
        val build =
            TrackerImportConflict.builder().conflict("Generated error conflict in enrollment")
                .trackedEntityInstance(enrollment.trackedEntityInstance()).enrollment(enrollmentUid)
                .displayDescription("Generated error description in enrollment")
                .status(ImportStatus.ERROR).build()
        val cv = build.toContentValues()
        try {
            d2.databaseAdapter().insert("TrackerImportConflict", null, cv)
            d2.databaseAdapter().execSQL(updateEnrollment(enrollmentUid))
            enrollment.trackedEntityInstance()?.let {
                d2.databaseAdapter().execSQL(updateTei(it))
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun generateConflictInTeiForEnrollment(enrollmentUid: String) {
        val enrollment = d2.enrollment(enrollmentUid)
        val build =
            TrackerImportConflict.builder().conflict("Generated error conflict in TEI level")
                .trackedEntityInstance(enrollment.trackedEntityInstance())
                .displayDescription("Generated error description in TEI level")
                .status(ImportStatus.ERROR).build()
        val cv = build.toContentValues()
        try {
            d2.databaseAdapter().insert("TrackerImportConflict", null, cv)
            enrollment.trackedEntityInstance()?.let {
                d2.databaseAdapter().execSQL(updateTei(it))
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun generateConflictInDataElement(): String {
        var event: Event? = null
        var attributeValue: TrackedEntityDataValue? = null
        while (event == null) {
            attributeValue = d2.trackedEntityModule().trackedEntityDataValues().blockingGet()
                ?.let { attributeValues ->
                    attributeValues[Random.nextInt(attributeValues.size)]
                }!!

            event = d2.eventModule().events().uid(attributeValue.event()).blockingGet()
                ?.takeIf { it.enrollment() == null }
        }

        val build =
            TrackerImportConflict.builder().conflict("Generated error conflict in data element")
                .value(attributeValue!!.value()).event(attributeValue.event())
                .dataElement(attributeValue.dataElement())
                .displayDescription("Generated error description in data element")
                .status(ImportStatus.ERROR).build()
        val cv = build.toContentValues()
        try {
            d2.databaseAdapter().insert("TrackerImportConflict", null, cv)
            d2.databaseAdapter().execSQL(updateEvent(event.uid()))
        } catch (e: Exception) {
            Timber.e(e)
        }
        return event.uid()
    }

    private fun generateConflictInEvent(eventUid: String) {
        val build = TrackerImportConflict.builder().conflict("Generated error conflict in event")
            .event(eventUid).displayDescription("Generated error description in event")
            .status(ImportStatus.ERROR).build()
        val cv = build.toContentValues()
        try {
            d2.databaseAdapter().insert("TrackerImportConflict", null, cv)
            d2.databaseAdapter().execSQL(updateEvent(eventUid))
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun generateConflictInDataSetValue() {
        val attributeValue =
            d2.dataValueModule().dataValues().blockingGet()?.let { attributeValues ->
                attributeValues[Random.nextInt(attributeValues.size)]
            }!!

        val build = DataValueConflict.builder().conflict("Generated error conflict in data value")
            .value(attributeValue.value()).dataElement(attributeValue.dataElement())
            .period(attributeValue.period()).orgUnit(attributeValue.organisationUnit())
            .attributeOptionCombo(attributeValue.attributeOptionCombo())
            .categoryOptionCombo(attributeValue.categoryOptionCombo())
            .displayDescription("Generated error description in data value")
            .status(ImportStatus.ERROR).build()
        val cv = build.toContentValues()
        val updatedDataValueCV =
            attributeValue.toBuilder().syncState(State.ERROR).build().toContentValues()
        try {
            d2.databaseAdapter().insert("DataValueConflict", null, cv)
            d2.databaseAdapter().update(
                DataValueTableInfo.TABLE_INFO.name(),
                updatedDataValueCV,
                "_id = ${attributeValue.id()}",
                emptyArray()
            )
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun updateEnrollment(enrollmentUid: String): String =
        "UPDATE Enrollment SET syncState = 'ERROR'," +
            " aggregatedSyncState = 'ERROR' where uid = '$enrollmentUid'"

    private fun updateTei(teiUid: String) =
        "UPDATE TrackedEntityInstance SET aggregatedSyncState = 'ERROR' where uid = '$teiUid'"

    private fun updateEvent(eventUid: String) =
        "UPDATE Event SET aggregatedSyncState = 'ERROR' where uid = '$eventUid'"
}