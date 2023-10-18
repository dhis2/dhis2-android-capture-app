package org.dhis2.commons.bindings

import io.reactivex.Single
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.dataset.DataSetInstance
import org.hisp.dhis.android.core.dataset.DataSetInstanceSummary
import org.hisp.dhis.android.core.datavalue.DataValueConflict
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.imports.TrackerImportConflict
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.hisp.dhis.android.core.trackedentity.TrackedEntityTypeAttribute
import org.hisp.dhis.android.core.usecase.stock.StockUseCase

fun D2.programs(): List<Program> = programModule().programs().blockingGet()

fun D2.program(programUid: String): Program? =
    programModule().programs().uid(programUid).blockingGet()

fun D2.observeProgram(programUid: String): Single<Program?> =
    programModule().programs().uid(programUid).get()

fun D2.isStockProgram(programUid: String): Boolean = useCaseModule()
    .stockUseCases()
    .uid(programUid)
    .blockingExists()

fun D2.stockUseCase(programUid: String): StockUseCase? = useCaseModule()
    .stockUseCases()
    .withTransactions()
    .uid(programUid)
    .blockingGet()

fun D2.dataSet(dataSetUid: String) = dataSetModule().dataSets().uid(dataSetUid).blockingGet()
fun D2.dataSetSummaryBy(dataSetUid: String): DataSetInstanceSummary {
    return dataSetModule().dataSetInstanceSummaries()
        .blockingGet()
        .find { it.dataSetUid() == dataSetUid }!!
}

fun D2.dataSetInstanceSummaries(): List<DataSetInstanceSummary> =
    dataSetModule().dataSetInstanceSummaries().blockingGet()

fun D2.dataElement(uid: String) = dataElementModule().dataElements()
    .uid(uid).blockingGet()

fun D2.categoryOptionCombo(uid: String) = categoryModule().categoryOptionCombos()
    .uid(uid).blockingGet()

fun D2.trackedEntityTypeForTei(teiUid: String) =
    trackedEntityModule().trackedEntityInstances().uid(teiUid).blockingGet()?.let { tei ->
        trackedEntityModule().trackedEntityTypes().uid(tei.trackedEntityType()).blockingGet()
    }

fun D2.trackedEntityType(uid: String) = trackedEntityModule().trackedEntityTypes()
    .uid(uid).blockingGet()

fun D2.tei(teiUid: String) = trackedEntityModule().trackedEntityInstances()
    .uid(teiUid).blockingGet()

fun D2.observeTei(teiUid: String) = trackedEntityModule().trackedEntityInstances()
    .uid(teiUid).get()

fun D2.teisBy(
    programs: List<String>? = null,
    aggregatedSynStates: List<State>? = null,
): List<TrackedEntityInstance> {
    var repository = trackedEntityModule().trackedEntityInstances()
    repository = programs?.let { repository.byProgramUids(programs) } ?: repository
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
    byNullEnrollment: Boolean = false,
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

fun D2.enrollment(uid: String) = enrollmentModule().enrollments()
    .uid(uid)
    .blockingGet()

fun D2.enrollmentInProgram(teiUid: String, programUid: String): Enrollment? =
    enrollmentModule().enrollments()
        .byTrackedEntityInstance().eq(teiUid)
        .byProgram().eq(programUid)
        .blockingGet().firstOrNull()

fun D2.enrollmentImportConflicts(enrollmentUid: String): List<TrackerImportConflict> =
    importModule().trackerImportConflicts()
        .byEnrollmentUid().eq(enrollmentUid)
        .blockingGet()

fun D2.teiAttribute(attributeUid: String) = trackedEntityModule().trackedEntityAttributes()
    .uid(attributeUid).blockingGet()

fun D2.teiMainAttributes(teiUid: String, programUid: String?): List<Pair<String?, String>> {
    val attributeValues = trackedEntityModule().trackedEntityAttributeValues()
        .byTrackedEntityInstance().eq(teiUid)
        .blockingGet()
    val attributeUids = if (programUid == null) {
        trackedEntityTypeMainAttributes(teiUid).mapNotNull { it.trackedEntityAttribute()?.uid() }
    } else {
        programMainAttributes(programUid).mapNotNull { it.trackedEntityAttribute()?.uid() }
    }
    return attributeUids.mapNotNull { attributeUid ->
        val attrValue = attributeValues.find { it.trackedEntityAttribute() == attributeUid }
        attrValue?.value()?.let { value ->
            val attribute = teiAttribute(attributeUid)
            Pair(attribute?.displayFormName() ?: attribute?.uid(), value)
        }
    }
}

fun D2.trackedEntityTypeMainAttributes(teiTypeUid: String): List<TrackedEntityTypeAttribute> =
    trackedEntityModule().trackedEntityTypeAttributes()
        .byTrackedEntityTypeUid().eq(teiTypeUid)
        .byDisplayInList().isTrue
        .blockingGet()

fun D2.programMainAttributes(programUid: String): List<ProgramTrackedEntityAttribute> =
    programModule().programTrackedEntityAttributes()
        .byProgram().eq(programUid)
        .byDisplayInList().isTrue
        .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
        .blockingGet()

fun D2.programStage(uid: String) = programModule().programStages().uid(uid).blockingGet()

fun D2.event(uid: String) = eventModule().events()
    .uid(uid).blockingGet()

fun D2.observeEvent(uid: String) = eventModule().events()
    .uid(uid).get()

fun D2.eventsBy(
    programUid: String? = null,
    enrollmentUid: String? = null,
    aggregatedSynStates: List<State>? = null,
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

fun D2.eventImportConflictsBy(eventUid: String? = null): List<TrackerImportConflict> {
    var repository = importModule().trackerImportConflicts()
    repository = eventUid?.let { repository.byEventUid().eq(it) } ?: repository
    return repository.blockingGet()
}

fun D2.organisationUnit(uid: String) = organisationUnitModule().organisationUnits()
    .uid(uid).blockingGet()

fun D2.dataSetInstancesBy(
    dataSetUid: String? = null,
    states: List<State>? = null,
): List<DataSetInstance> {
    var repository = dataSetModule().dataSetInstances()
    repository = dataSetUid?.let { repository.byDataSetUid().eq(dataSetUid) } ?: repository
    repository = states?.let {
        repository.byState().`in`(states)
    } ?: repository
    return repository.blockingGet()
}

fun D2.observeDataSetInstancesBy(
    dataSetUid: String? = null,
    orgUnitUid: String? = null,
    periodId: String? = null,
    attrOptionComboUid: String? = null,
    states: List<State>? = null,
): Single<List<DataSetInstance>> {
    var repository = dataSetModule().dataSetInstances()
    repository = dataSetUid?.let { repository.byDataSetUid().eq(dataSetUid) } ?: repository
    repository = states?.let {
        repository.byState().`in`(states)
    } ?: repository
    repository = orgUnitUid?.let { repository.byOrganisationUnitUid().eq(orgUnitUid) } ?: repository
    repository = periodId?.let { repository.byPeriod().eq(periodId) } ?: repository
    repository = attrOptionComboUid?.let {
        repository.byAttributeOptionComboUid().eq(attrOptionComboUid)
    } ?: repository
    return repository.get()
}

fun D2.dataValueConflictsBy(dataSetUid: String): List<DataValueConflict> {
    return dataValueModule().dataValueConflicts()
        .byDataSet(dataSetUid)
        .blockingGet()
}

fun D2.dataValueConflicts(
    dataSetUid: String,
    periodId: String,
    orgUnitUid: String,
    attrOptionComboUid: String,
): List<DataValueConflict> = dataValueModule().dataValueConflicts()
    .byDataSet(dataSetUid)
    .byPeriod().eq(periodId)
    .byOrganisationUnitUid().eq(orgUnitUid)
    .byAttributeOptionCombo().eq(attrOptionComboUid)
    .blockingGet()

fun D2.countDataValueConflicts(
    dataSetUid: String,
    periodId: String,
    orgUnitUid: String,
    attrOptionComboUid: String,
): Int = dataValueModule().dataValueConflicts()
    .byDataSet(dataSetUid)
    .byPeriod().eq(periodId)
    .byOrganisationUnitUid().eq(orgUnitUid)
    .byAttributeOptionCombo().eq(attrOptionComboUid)
    .blockingCount()

fun D2.period(periodId: String) = periodModule().periods()
    .byPeriodId().eq(periodId)
    .one()
    .blockingGet()

fun D2.disableCollapsableSectionsInProgram(programUid: String): Boolean {
    val globalSettingEnabled: Boolean? =
        settingModule().appearanceSettings()
            .getGlobalProgramConfigurationSetting()
            ?.disableCollapsibleSections()
    val specificSettingEnabled: Boolean? =
        settingModule().appearanceSettings()
            .getProgramConfigurationByUid(programUid)
            ?.disableCollapsibleSections()

    return when {
        globalSettingEnabled == null && specificSettingEnabled == null -> false
        specificSettingEnabled != null -> specificSettingEnabled
        globalSettingEnabled != null -> globalSettingEnabled
        else -> false
    }
}
