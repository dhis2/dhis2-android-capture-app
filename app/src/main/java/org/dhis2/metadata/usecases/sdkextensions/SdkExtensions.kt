package org.dhis2.metadata.usecases.sdkextensions

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.dataset.DataSetInstance
import org.hisp.dhis.android.core.dataset.DataSetInstanceSummary
import org.hisp.dhis.android.core.datavalue.DataValueConflict
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.imports.TrackerImportConflict
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType

fun D2.programs(): List<Program> =
    programModule().programs().blockingGet()

fun D2.program(programUid: String): Program =
    programModule().programs().uid(programUid).blockingGet()

fun D2.dataSetInstanceSummaries(): List<DataSetInstanceSummary> =
    dataSetModule().dataSetInstanceSummaries().blockingGet()

fun D2.dataElement(uid: String): DataElement =
    dataElementModule().dataElements()
        .uid(uid).blockingGet()

fun D2.categoryOptionCombo(uid: String): CategoryOptionCombo =
    categoryModule().categoryOptionCombos()
        .uid(uid).blockingGet()

fun D2.trackedEntityType(uid: String): TrackedEntityType =
    trackedEntityModule().trackedEntityTypes()
        .uid(uid).blockingGet()

fun D2.tei(teiUid: String): TrackedEntityInstance =
    trackedEntityModule().trackedEntityInstances()
        .uid(teiUid).blockingGet()

fun D2.teisBy(
    programs: List<String>? = null,
    aggregatedSynStates: List<State>? = null
): List<TrackedEntityInstance> {
    var repository = trackedEntityModule().trackedEntityInstances()
    repository = programs.let { repository.byProgramUids(programs) } ?: repository
    repository =
        aggregatedSynStates.let { repository.byAggregatedSyncState().`in`(aggregatedSynStates) }
        ?: repository
    return repository.blockingGet()
}

fun D2.teiImportConflicts(teiUid: String): List<TrackerImportConflict> =
    importModule().trackerImportConflicts()
        .byTrackedEntityInstanceUid().eq(teiUid)
        .blockingGet()

fun D2.teiImportConflictsBy(
    teiUid: String? = null,
    enrollmentUid: String? = null,
    byNullEnrollment: Boolean = false
): List<TrackerImportConflict> {
    var repository = importModule().trackerImportConflicts()
    repository = teiUid?.let { repository.byTrackedEntityInstanceUid().eq(teiUid) } ?: repository
    repository = if (byNullEnrollment) {
        repository.byEnrollmentUid().isNull
    } else {
        enrollmentUid?.let { repository.byEnrollmentUid().eq(enrollmentUid) } ?: repository
    }
    return repository.blockingGet()
}

fun D2.countTeiImportConflicts(teiUid: String): Int = importModule().trackerImportConflicts()
    .byTrackedEntityInstanceUid().eq(teiUid)
    .blockingCount()

fun D2.enrollment(uid: String): Enrollment =
    enrollmentModule().enrollments()
        .uid(uid)
        .blockingGet()

fun D2.enrollmentImportConflicts(enrollmentUid: String): List<TrackerImportConflict> =
    importModule().trackerImportConflicts()
        .byEnrollmentUid().eq(enrollmentUid)
        .blockingGet()

fun D2.teiAttribute(uid: String): TrackedEntityAttribute =
    trackedEntityModule().trackedEntityAttributes()
        .uid(uid).blockingGet()

fun D2.programStage(uid: String): ProgramStage =
    programModule().programStages().uid(uid).blockingGet()

fun D2.event(uid: String): Event = eventModule().events()
    .uid(uid).blockingGet()

fun D2.eventsBy(
    programUid: String? = null,
    enrollmentUid: String? = null,
    aggregatedSynStates: List<State>? = null
): List<Event> {
    var repository = eventModule().events()
    repository = programUid?.let { repository.byProgramUid().eq(programUid) } ?: repository
    repository = enrollmentUid?.let { repository.byEnrollmentUid().eq(enrollmentUid) } ?: repository
    repository =
        aggregatedSynStates?.let { repository.byAggregatedSyncState().`in`(aggregatedSynStates) }
        ?: repository
    return repository.blockingGet()
}

fun D2.countEventImportConflicts(eventUid: String): Int = importModule().trackerImportConflicts()
    .byEventUid().eq(eventUid)
    .blockingCount()

fun D2.eventImportConflictsBy(
    eventUid: String? = null
): List<TrackerImportConflict> {
    var repository = importModule().trackerImportConflicts()
    repository = eventUid?.let { repository.byEventUid().eq(it) } ?: repository
    return repository.blockingGet()
}

fun D2.dataSetInstancesBy(
    dataSetUid: String? = null,
    states: List<State>? = null
): List<DataSetInstance> {
    var repository = dataSetModule().dataSetInstances()
    repository = dataSetUid?.let { repository.byDataSetUid().eq(dataSetUid) } ?: repository
    repository = states?.let {
        repository.byState().`in`(states)
    } ?: repository
    return repository.blockingGet()
}

fun D2.dataValueConflicts(
    dataSetUid: String,
    periodId: String,
    orgUnitUid: String,
    attrOptionComboUid: String
): List<DataValueConflict> = dataValueModule().dataValueConflicts()
    .byDataSet(dataSetUid)
    .byPeriod().eq(periodId)
    .byOrganisationUnitUid().eq(orgUnitUid)
    .byAttributeOptionCombo().eq(attrOptionComboUid)
    .blockingGet()
