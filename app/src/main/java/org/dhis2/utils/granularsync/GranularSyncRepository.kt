package org.dhis2.utils.granularsync

import io.reactivex.Single
import org.dhis2.R
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.sync.ConflictType
import org.dhis2.data.dhislogic.DhisProgramUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.imports.ImportStatus
import org.hisp.dhis.android.core.program.ProgramType

class GranularSyncRepository(
    private val d2: D2,
    private val conflictType: ConflictType,
    private val recordUid: String,
    private val dvOrgUnit: String?,
    private val dvAttrCombo: String?,
    private val dvPeriodId: String?,
    private val preferenceProvider: PreferenceProvider,
    private val dhisProgramUtils: DhisProgramUtils,
    private val resourceManager: ResourceManager
) {
    fun getHomeItemsWithStates(vararg states: State): List<SyncStatusItem> {
        val programList = d2.programModule().programs()
            .blockingGet().filter {
                states.contains(dhisProgramUtils.getProgramState(it))
            }.map { program ->
                SyncStatusItem(
                    type = when (program.programType()) {
                        ProgramType.WITH_REGISTRATION -> SyncStatusType.EventProgram(program.uid())
                        ProgramType.WITHOUT_REGISTRATION -> SyncStatusType.TrackerProgram(program.uid())
                        null -> throw NullPointerException("Program ${program.uid()}: program type can't be null")
                    },
                    displayName = program.displayName() ?: program.uid(),
                    description = "PLACEHOLDER",
                    state = dhisProgramUtils.getProgramState(program)
                )
            }
        val dataSetList = d2.dataSetModule().dataSetInstanceSummaries()
            .blockingGet().filter {
                states.contains(it.state())
            }.map { dataSetInstanceSummary ->
                SyncStatusItem(
                    type = SyncStatusType.DataSet(dataSetInstanceSummary.dataSetUid()),
                    displayName = dataSetInstanceSummary.dataSetDisplayName(),
                    description = "PLACEHOLDER",
                    state = dataSetInstanceSummary.state()
                )
            }
        return (programList + dataSetList).sortedByState()
    }

    fun getProgramItemsWithStates(programUid: String, vararg states: State): List<SyncStatusItem> {
        val program = d2.programModule().programs().uid(programUid).blockingGet()
        return if (program.programType() == ProgramType.WITH_REGISTRATION) {
            d2.trackedEntityModule().trackedEntityInstances()
                .byProgramUids(listOf(programUid))
                .byAggregatedSyncState().`in`(*states)
                .blockingGet().map { tei ->

                    val description = when (tei.aggregatedSyncState()) {
                        State.ERROR -> "%d errors".format(getNumberOfConflictsForTei(tei.uid()))
                        State.TO_POST, State.TO_UPDATE -> "Sync needed"
                        else -> ""
                    }

                    val trackedEntityTypeName = d2.trackedEntityModule().trackedEntityTypes()
                        .uid(tei.trackedEntityType())
                        .blockingGet().displayName()

                    SyncStatusItem(
                        type = SyncStatusType.TrackedEntity(tei.uid(), null),
                        displayName = trackedEntityTypeName!!,
                        description = description,
                        state = tei.aggregatedSyncState()!!
                    )
                }.sortedByState()
        } else {
            d2.eventModule().events()
                .byProgramUid().eq(programUid)
                .byAggregatedSyncState().`in`(*states)
                .blockingGet().map { event ->

                    val description = when (event.aggregatedSyncState()) {
                        State.ERROR -> "%d errors".format(getNumberOfConflictsForEvent(event.uid()))
                        State.TO_POST, State.TO_UPDATE -> "Sync needed"
                        else -> ""
                    }

                    val stageName = d2.programModule().programStages()
                        .uid(event.programStage())
                        .blockingGet().displayName()

                    SyncStatusItem(
                        type = SyncStatusType.Event(event.uid(), event.programStage()),
                        displayName = stageName!!,
                        description = description,
                        state = event.aggregatedSyncState()!!
                    )
                }.sortedByState()
        }

    }

    private fun getNumberOfConflictsForTei(teiUid: String): Int {
        return d2.importModule().trackerImportConflicts().byTrackedEntityInstanceUid()
            .eq(teiUid)
            .blockingCount()
    }

    private fun getNumberOfConflictsForEvent(eventUid: String): Int {
        return d2.importModule().trackerImportConflicts().byEventUid()
            .eq(eventUid)
            .blockingCount()
    }

    fun getEnrollmentItemsWithStates(enrollmentUid: String, vararg states: State): List<SyncStatusItem> {
        val enrollment = d2.enrollmentModule().enrollments()
            .uid(enrollmentUid)
            .blockingGet()
        val conflicts = if (states.contains(State.ERROR) || states.contains(State.WARNING)) {
            d2.importModule().trackerImportConflicts()
                .byEnrollmentUid().eq(enrollmentUid)
                .blockingGet().mapNotNull { trackerImportConflict ->
                    val tei = d2.trackedEntityModule().trackedEntityInstances()
                        .uid(enrollment.trackedEntityInstance())
                        .blockingGet()
                    when {
                        trackerImportConflict.event() != null -> {
                            mapEventToSyncStatusItem(
                                trackerImportConflict.event()!!,
                                trackerImportConflict.dataElement(),
                                trackerImportConflict.displayDescription() ?: ""
                            )
                        }
                        trackerImportConflict.trackedEntityAttribute() != null -> {
                            val attribute = d2.trackedEntityModule().trackedEntityAttributes()
                                .uid(trackerImportConflict.trackedEntityAttribute()!!)
                                .blockingGet()
                            SyncStatusItem(
                                type = SyncStatusType.TrackedEntity(
                                    enrollment.trackedEntityInstance()!!,
                                    trackerImportConflict.enrollment()!!
                                ),
                                displayName = attribute.displayFormName()?:attribute.uid(),
                                description = trackerImportConflict.displayDescription() ?: "",
                                state = trackerImportConflict.status()?.toState()
                                    ?: enrollment.aggregatedSyncState()!!
                            )
                        }

                        trackerImportConflict.enrollment() != null -> {
                            val enrollment = d2.enrollmentModule().enrollments()
                                .uid(trackerImportConflict.enrollment())
                                .blockingGet()
                            val program = d2.programModule().programs()
                                .uid(enrollment.program())
                                .blockingGet()
                            SyncStatusItem(
                                type = SyncStatusType.TrackedEntity(
                                    enrollment.trackedEntityInstance()!!,
                                    trackerImportConflict.enrollment()!!
                                ),
                                displayName = "Enrollment in ${program.displayName()}",
                                description = trackerImportConflict.displayDescription() ?: "",
                                state = trackerImportConflict.status()?.toState()
                                    ?: enrollment.aggregatedSyncState()!!
                            )
                        }
                        trackerImportConflict.trackedEntityInstance()!= null -> {
                            SyncStatusItem(
                                type = SyncStatusType.TrackedEntity(
                                    enrollment.trackedEntityInstance()!!,
                                    null
                                ),
                                displayName = "Error in TEI",
                                description = trackerImportConflict.displayDescription() ?: "",
                                state = trackerImportConflict.status()?.toState()
                                    ?: tei.aggregatedSyncState()!!
                            )
                        }
                        else -> null
                    }
                }
        } else {
            emptyList()
        }

        val teiConflicts = if (states.contains(State.ERROR) || states.contains(State.WARNING)) {
            d2.importModule().trackerImportConflicts()
                .byTrackedEntityInstanceUid().eq(enrollment.trackedEntityInstance())
                .byEnrollmentUid().isNull
                .blockingGet().mapNotNull { trackerImportConflict ->
                    val tei = d2.trackedEntityModule().trackedEntityInstances()
                        .uid(enrollment.trackedEntityInstance())
                        .blockingGet()
                    when {
                        trackerImportConflict.trackedEntityAttribute() != null -> {
                            val attribute = d2.trackedEntityModule().trackedEntityAttributes()
                                .uid(trackerImportConflict.trackedEntityAttribute()!!)
                                .blockingGet()
                            SyncStatusItem(
                                type = SyncStatusType.TrackedEntity(
                                    enrollment.trackedEntityInstance()!!,
                                    trackerImportConflict.enrollment()!!
                                ),
                                displayName = attribute.displayFormName()?:attribute.uid(),
                                description = trackerImportConflict.displayDescription() ?: "",
                                state = trackerImportConflict.status()?.toState()
                                    ?: enrollment.aggregatedSyncState()!!
                            )
                        }

                        trackerImportConflict.trackedEntityInstance()!= null -> {
                            SyncStatusItem(
                                type = SyncStatusType.TrackedEntity(
                                    enrollment.trackedEntityInstance()!!,
                                    null
                                ),
                                displayName = "Error in TEI",
                                description = trackerImportConflict.displayDescription() ?: "",
                                state = trackerImportConflict.status()?.toState()
                                    ?: tei.aggregatedSyncState()!!
                            )
                        }
                        else -> null
                    }
                }
        } else {
            emptyList()
        }

        val statesWithoutError = states.filter { it != State.ERROR && it != State.WARNING }
        val notSyncedEvents = d2.eventModule().events()
            .byEnrollmentUid().eq(enrollmentUid)
            .byAggregatedSyncState().`in`(statesWithoutError)
            .blockingGet().map { event ->
                mapEventToSyncStatusItem(event.uid(), null, "Sync needed")
            }

        return (conflicts + teiConflicts + notSyncedEvents).sortedByState()
    }

    private fun mapEventToSyncStatusItem(
        eventUid: String,
        dataElementUid: String?,
        description: String
    ): SyncStatusItem {
        val event = d2.eventModule().events()
            .uid(eventUid)
            .blockingGet()
        val stage = d2.programModule().programStages()
            .uid(event.programStage())
            .blockingGet()
        val dataElement = dataElementUid?.let {
            d2.dataElementModule().dataElements()
                .uid(dataElementUid)
                .blockingGet()
        }
        return SyncStatusItem(
            type = SyncStatusType.Event(
                eventUid = event.uid(),
                programStageUid = event.programStage()
            ),
            displayName = dataElement?.displayFormName() ?: stage.displayName() ?: event.uid(),
            description = description,
            state = event.aggregatedSyncState()!!
        )
    }

    fun getEventItemsWithStates(eventUid: String, vararg states: State): List<SyncStatusItem> {
        return if (states.contains(State.ERROR) || states.contains(State.WARNING)) {
            d2.importModule().trackerImportConflicts()
                .byEventUid().eq(eventUid)
                .blockingGet().map { trackerImportConflict ->
                    mapEventToSyncStatusItem(
                        trackerImportConflict.event()!!,
                        trackerImportConflict.dataElement(),
                        trackerImportConflict.displayDescription() ?: ""
                    )
                }
        } else {
            emptyList()
        }
    }

    fun getUiState(forcedState: State? = null): SyncUiState {
        return Single.zip(
            getState(),
            getLastSynced(),
        ) { state, lastSync ->
            buildUiState(
                forcedState.takeIf { it != null } ?: state,
                lastSync
            )
        }.blockingGet()
    }

    private fun buildUiState(state: State, lastSync: SyncDate): SyncUiState {
        return SyncUiState(
            syncState = state,
            title = getTitleForState(state),
            lastSyncDate = lastSync.takeIf { state != State.UPLOADING },
            message = getMessageForState(state),
            mainActionLabel = getMainActionLabel(state),
            secondaryActionLabel = getSecondaryActionLabel(state),
            content = getContent(state)
        )
    }

    private fun getState(): Single<State> {
        return when (conflictType) {
            ConflictType.PROGRAM ->
                d2.programModule().programs().uid(recordUid).get()
                    .map {
                        dhisProgramUtils.getProgramState(it)
                    }
            ConflictType.TEI -> {
                val enrollment = d2.enrollmentModule().enrollments().uid(recordUid).blockingGet()
                d2.trackedEntityModule().trackedEntityInstances()
                    .uid(enrollment.trackedEntityInstance()).get()
                    .map { it.aggregatedSyncState() }
            }
            ConflictType.EVENT ->
                d2.eventModule().events().uid(recordUid).get()
                    .map { it.aggregatedSyncState() }
            ConflictType.DATA_SET ->
                d2.dataSetModule().dataSetInstances()
                    .byDataSetUid().eq(recordUid).get()
                    .map { dataSetInstances ->
                        getStateFromCanditates(dataSetInstances.map { it.state() }.toMutableList())
                    }
            ConflictType.DATA_VALUES ->
                d2.dataSetModule().dataSetInstances()
                    .byOrganisationUnitUid().eq(dvOrgUnit)
                    .byPeriod().eq(dvPeriodId)
                    .byAttributeOptionComboUid().eq(dvAttrCombo)
                    .byDataSetUid().eq(recordUid).get()
                    .map { dataSetInstance ->
                        getStateFromCanditates(dataSetInstance.map { it.state() }.toMutableList())
                    }
            ConflictType.ALL -> Single.just(dhisProgramUtils.getServerState())
        }
    }

    private fun getStateFromCanditates(stateCandidates: MutableList<State?>): State {
        if (conflictType == ConflictType.DATA_SET) {
            stateCandidates.addAll(
                d2.dataSetModule().dataSetCompleteRegistrations()
                    .byDataSetUid().eq(recordUid)
                    .blockingGet().map { it.syncState() }
            )
        } else {
            stateCandidates.addAll(
                d2.dataSetModule().dataSetCompleteRegistrations()
                    .byOrganisationUnitUid().eq(dvOrgUnit)
                    .byPeriod().eq(dvPeriodId)
                    .byAttributeOptionComboUid().eq(dvAttrCombo)
                    .byDataSetUid().eq(recordUid).get()
                    .blockingGet().map { it.syncState() }
            )
        }

        return when {
            stateCandidates.contains(State.ERROR) -> State.ERROR
            stateCandidates.contains(State.WARNING) -> State.WARNING
            stateCandidates.contains(State.SENT_VIA_SMS) ||
                    stateCandidates.contains(State.SYNCED_VIA_SMS) ->
                State.SENT_VIA_SMS
            stateCandidates.contains(State.TO_POST) ||
                    stateCandidates.contains(State.UPLOADING) ||
                    stateCandidates.contains(State.TO_UPDATE) ->
                State.TO_UPDATE
            else -> State.SYNCED
        }
    }

    private fun getLastSynced(): Single<SyncDate> {
        return when (conflictType) {
            ConflictType.PROGRAM ->
                d2.programModule().programs().uid(recordUid).get()
                    .map {
                        SyncDate(it.lastUpdated())
                    }
            ConflictType.TEI -> {
                val enrollment = d2.enrollmentModule().enrollments().uid(recordUid).blockingGet()
                d2.trackedEntityModule().trackedEntityInstances()
                    .uid(enrollment.trackedEntityInstance()).get()
                    .map { SyncDate(it.lastUpdated()) }
            }
            ConflictType.EVENT ->
                d2.eventModule().events().uid(recordUid).get()
                    .map { SyncDate(it.lastUpdated()) }
            ConflictType.DATA_SET ->
                d2.dataSetModule().dataSets()
                    .uid(recordUid).get()
                    .map { dataSet ->
                        SyncDate(dataSet.lastUpdated())
                    }
            ConflictType.DATA_VALUES ->
                d2.dataSetModule().dataSetInstances()
                    .byOrganisationUnitUid().eq(dvOrgUnit)
                    .byPeriod().eq(dvPeriodId)
                    .byAttributeOptionComboUid().eq(dvAttrCombo)
                    .byDataSetUid().eq(recordUid).get()
                    .map { dataSetInstance ->
                        dataSetInstance.sortBy { it.lastUpdated() }
                        SyncDate(
                            dataSetInstance.apply {
                                sortBy { it.lastUpdated() }
                            }.first().lastUpdated()
                        )
                    }
            ConflictType.ALL ->
                Single.just(SyncDate(preferenceProvider.lastSync()))
        }
    }

    private fun getTitleForState(state: State): String {
        return when (state) {
            State.TO_POST,
            State.TO_UPDATE -> resourceManager.getString(R.string.sync_dialog_title_not_synced)
            State.ERROR -> resourceManager.getString(R.string.sync_dialog_title_error)
            State.RELATIONSHIP,
            State.SYNCED -> resourceManager.getString(R.string.sync_dialog_title_synced)
            State.WARNING -> resourceManager.getString(R.string.sync_dialog_title_warning)
            State.UPLOADING -> resourceManager.getString(R.string.sync_dialog_title_syncing)
            State.SENT_VIA_SMS,
            State.SYNCED_VIA_SMS -> resourceManager.getString(R.string.sync_dialog_title_sms_syced)
        }
    }

    private fun getMessageForState(state: State): String? {
        return when (state) {
            State.TO_POST,
            State.TO_UPDATE -> getNotSyncedMessage()
            State.SYNCED -> getSyncedMessage()
            State.SENT_VIA_SMS,
            State.SYNCED_VIA_SMS -> getSmsSyncedMessage()
            State.ERROR,
            State.WARNING,
            State.RELATIONSHIP,
            State.UPLOADING -> null
        }
    }

    private fun getNotSyncedMessage(): String? {
        return when (conflictType) {
            ConflictType.ALL -> resourceManager.getString(R.string.sync_dialog_message_not_synced_all)
            ConflictType.DATA_SET,
            ConflictType.DATA_VALUES,
            ConflictType.PROGRAM -> resourceManager.getString(
                R.string.sync_dialog_message_not_synced_program,
                getMessageArgument()
            )
            ConflictType.TEI, ConflictType.EVENT -> resourceManager.getString(
                R.string.sync_dialog_message_not_synced_tei,
                getMessageArgument()
            )
        }
    }

    private fun getSyncedMessage(): String? {
        return when (conflictType) {
            ConflictType.ALL -> resourceManager.getString(R.string.sync_dialog_message_synced_all)
            ConflictType.DATA_SET,
            ConflictType.DATA_VALUES,
            ConflictType.PROGRAM -> resourceManager.getString(
                R.string.sync_dialog_message_synced_program,
                getMessageArgument()
            )
            ConflictType.TEI, ConflictType.EVENT -> resourceManager.getString(
                R.string.sync_dialog_message_synced_tei,
                getMessageArgument()
            )
        }
    }

    private fun getSmsSyncedMessage(): String? {
        return when (conflictType) {
            ConflictType.ALL,
            ConflictType.DATA_SET,
            ConflictType.PROGRAM -> null
            ConflictType.DATA_VALUES,
            ConflictType.TEI,
            ConflictType.EVENT -> resourceManager.getString(R.string.sync_dialog_message_sms_synced)
        }
    }

    private fun getMessageArgument(): String {
        return getTitle().blockingGet()
    }

    private fun getTitle(): Single<String> {
        return when (conflictType) {
            ConflictType.ALL -> Single.just("")
            ConflictType.PROGRAM -> d2.programModule().programs().uid(recordUid).get()
                .map { it.displayName() }
            ConflictType.TEI -> {
                val enrollment =
                    d2.enrollmentModule().enrollments().uid(recordUid).blockingGet()
                d2.trackedEntityModule().trackedEntityTypes().uid(
                    d2.trackedEntityModule().trackedEntityInstances()
                        .uid(enrollment.trackedEntityInstance()!!)
                        .blockingGet().trackedEntityType()
                )
                    .get().map { it.displayName() }
            }
            ConflictType.EVENT ->
                d2.programModule().programStages().uid(
                    d2.eventModule().events().uid(recordUid).blockingGet().programStage()
                ).get().map { it.displayName() }
            ConflictType.DATA_SET, ConflictType.DATA_VALUES ->
                d2.dataSetModule().dataSets().uid(recordUid).get()
                    .map { it.displayName() }
        }
    }

    private fun getMainActionLabel(state: State): String? {
        return when (state) {
            State.TO_POST,
            State.SENT_VIA_SMS,
            State.SYNCED_VIA_SMS,
            State.TO_UPDATE -> resourceManager.getString(R.string.sync_dialog_action_send)
            State.ERROR,
            State.SYNCED,
            State.WARNING -> resourceManager.getString(R.string.sync_dialog_action_refresh)
            State.UPLOADING,
            State.RELATIONSHIP -> null
        }
    }

    private fun getSecondaryActionLabel(state: State): String? {
        return when (state) {
            State.UPLOADING,
            State.RELATIONSHIP -> null
            State.TO_POST,
            State.TO_UPDATE,
            State.ERROR,
            State.SYNCED,
            State.SENT_VIA_SMS,
            State.SYNCED_VIA_SMS,
            State.WARNING -> resourceManager.getString(R.string.sync_dialog_action_not_now)
        }
    }

    private fun getContent(state: State): List<SyncStatusItem> {
        return when (state) {
            State.TO_POST,
            State.TO_UPDATE -> getContentItems(State.TO_POST, State.TO_UPDATE)
            State.ERROR -> getContentItems(
                State.ERROR,
                State.WARNING,
                State.TO_UPDATE,
                State.TO_POST
            )
            State.WARNING -> getContentItems(State.WARNING, State.TO_POST, State.TO_UPDATE)
            State.UPLOADING,
            State.RELATIONSHIP,
            State.SYNCED,
            State.SENT_VIA_SMS,
            State.SYNCED_VIA_SMS -> emptyList()
        }
    }

    private fun getContentItems(vararg states: State): List<SyncStatusItem> {
        return when (conflictType) {
            ConflictType.ALL -> getHomeItemsWithStates(*states)
            ConflictType.PROGRAM -> getProgramItemsWithStates(recordUid, *states)
            ConflictType.TEI -> getEnrollmentItemsWithStates(recordUid, *states)
            ConflictType.EVENT -> getEventItemsWithStates(recordUid, *states)
            ConflictType.DATA_SET -> TODO()
            ConflictType.DATA_VALUES -> TODO()
        }
    }
}

fun List<SyncStatusItem>.sortedByState(): List<SyncStatusItem> {
    return sortedWith { item1, item2 ->
        item1.state.priority().compareTo(item2.state.priority())
    }
}

fun State.priority(): Int {
    return when (this) {
        State.TO_POST,
        State.TO_UPDATE -> 3
        State.ERROR -> 1
        State.WARNING -> 2
        State.SYNCED,
        State.UPLOADING,
        State.RELATIONSHIP,
        State.SENT_VIA_SMS,
        State.SYNCED_VIA_SMS -> 4
    }
}

fun ImportStatus.toState(): State {
    return when (this) {
        ImportStatus.SUCCESS -> State.SYNCED
        ImportStatus.WARNING -> State.WARNING
        ImportStatus.ERROR -> State.ERROR
    }
}
