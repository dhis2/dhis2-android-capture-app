package org.dhis2.utils.granularsync

import io.reactivex.Single
import java.util.Locale
import org.dhis2.R
import org.dhis2.commons.bindings.categoryOptionCombo
import org.dhis2.commons.bindings.countEventImportConflicts
import org.dhis2.commons.bindings.countTeiImportConflicts
import org.dhis2.commons.bindings.dataElement
import org.dhis2.commons.bindings.dataSetInstanceSummaries
import org.dhis2.commons.bindings.dataSetInstancesBy
import org.dhis2.commons.bindings.dataSetSummaryBy
import org.dhis2.commons.bindings.dataValueConflicts
import org.dhis2.commons.bindings.dataValueConflictsBy
import org.dhis2.commons.bindings.enrollment
import org.dhis2.commons.bindings.enrollmentImportConflicts
import org.dhis2.commons.bindings.enrollmentInProgram
import org.dhis2.commons.bindings.event
import org.dhis2.commons.bindings.eventImportConflictsBy
import org.dhis2.commons.bindings.eventsBy
import org.dhis2.commons.bindings.isStockProgram
import org.dhis2.commons.bindings.observeDataSetInstancesBy
import org.dhis2.commons.bindings.observeEvent
import org.dhis2.commons.bindings.observeProgram
import org.dhis2.commons.bindings.observeTei
import org.dhis2.commons.bindings.organisationUnit
import org.dhis2.commons.bindings.period
import org.dhis2.commons.bindings.program
import org.dhis2.commons.bindings.programs
import org.dhis2.commons.bindings.stockUseCase
import org.dhis2.commons.bindings.tei
import org.dhis2.commons.bindings.teiAttribute
import org.dhis2.commons.bindings.teiImportConflictsBy
import org.dhis2.commons.bindings.teiMainAttributes
import org.dhis2.commons.bindings.teisBy
import org.dhis2.commons.bindings.trackedEntityType
import org.dhis2.commons.date.toUi
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.sync.ConflictType
import org.dhis2.commons.sync.SyncContext
import org.dhis2.commons.sync.SyncStatusItem
import org.dhis2.commons.sync.SyncStatusType
import org.dhis2.data.dhislogic.DhisPeriodUtils
import org.dhis2.data.dhislogic.DhisProgramUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.dataset.DataSetInstance
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.imports.ImportStatus
import org.hisp.dhis.android.core.program.ProgramType

class GranularSyncRepository(
    private val d2: D2,
    private val syncContext: SyncContext,
    private val preferenceProvider: PreferenceProvider,
    private val dhisProgramUtils: DhisProgramUtils,
    private val periodUtils: DhisPeriodUtils,
    private val resourceManager: ResourceManager
) {

    fun getUiState(forcedState: State? = null): SyncUiState {
        return Single.zip(
            getState(),
            getLastSynced()
        ) { state, lastSync ->
            buildUiState(
                forcedState.takeIf { it != null } ?: state,
                lastSync
            )
        }.blockingGet()
    }

    private fun getState(): Single<State> {
        return when (syncContext.conflictType()) {
            ConflictType.PROGRAM ->
                d2.observeProgram(syncContext.recordUid())
                    .map { dhisProgramUtils.getProgramState(it) }
            ConflictType.TEI -> {
                val enrollment = d2.enrollment(syncContext.recordUid())
                d2.observeTei(enrollment.trackedEntityInstance()!!).map { it.aggregatedSyncState() }
            }
            ConflictType.EVENT ->
                d2.observeEvent(syncContext.recordUid()).map { it.aggregatedSyncState() }
            ConflictType.DATA_SET ->
                d2.observeDataSetInstancesBy(syncContext.recordUid())
                    .map { dataSetInstances ->
                        getStateFromCandidates(dataSetInstances.map { it.state() }.toMutableList())
                    }
            ConflictType.DATA_VALUES ->
                with(syncContext as SyncContext.DataSetInstance) {
                    d2.observeDataSetInstancesBy(
                        dataSetUid = dataSetUid,
                        orgUnitUid = orgUnitUid,
                        periodId = periodId,
                        attrOptionComboUid = attributeOptionComboUid
                    ).map { dataSetInstance ->
                        getStateFromCandidates(dataSetInstance.map { it.state() }.toMutableList())
                    }
                }
            ConflictType.ALL -> Single.just(dhisProgramUtils.getServerState())
        }
    }

    private fun getLastSynced(returnEmpty: Boolean = true): Single<SyncDate> {
        return if (returnEmpty) {
            Single.just(SyncDate(null))
        } else {
            when (syncContext.conflictType()) {
                ConflictType.PROGRAM ->
                    d2.observeProgram(syncContext.recordUid()).map { SyncDate(it.lastUpdated()) }
                ConflictType.TEI -> {
                    val enrollment = d2.enrollment(syncContext.recordUid())
                    d2.observeTei(enrollment.trackedEntityInstance()!!)
                        .map { SyncDate(it.lastUpdated()) }
                }
                ConflictType.EVENT ->
                    d2.observeEvent(syncContext.recordUid()).map { SyncDate(it.lastUpdated()) }
                ConflictType.DATA_SET ->
                    d2.dataSetModule().dataSets()
                        .uid(syncContext.recordUid()).get()
                        .map { dataSet -> SyncDate(dataSet.lastUpdated()) }
                ConflictType.DATA_VALUES ->
                    with(syncContext as SyncContext.DataSetInstance) {
                        d2.observeDataSetInstancesBy(
                            dataSetUid = dataSetUid,
                            orgUnitUid = orgUnitUid,
                            periodId = periodId,
                            attrOptionComboUid = attributeOptionComboUid
                        ).map { dataSetInstance ->
                            dataSetInstance.sortedBy { it.lastUpdated() }
                            SyncDate(
                                dataSetInstance.apply {
                                    sortedBy { it.lastUpdated() }
                                }.first().lastUpdated()
                            )
                        }
                    }
                ConflictType.ALL ->
                    Single.just(SyncDate(preferenceProvider.lastSync()))
            }
        }
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

    private fun getNotSyncedMessage(): String {
        return when (syncContext.conflictType()) {
            ConflictType.ALL ->
                resourceManager.getString(R.string.sync_dialog_message_not_synced_all)
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

    private fun getSyncedMessage(): String {
        return when (syncContext.conflictType()) {
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
        return when (syncContext.conflictType()) {
            ConflictType.ALL,
            ConflictType.DATA_SET,
            ConflictType.PROGRAM -> null
            ConflictType.DATA_VALUES,
            ConflictType.TEI,
            ConflictType.EVENT -> resourceManager.getString(R.string.sync_dialog_message_sms_synced)
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
        return when (syncContext) {
            is SyncContext.DataSet ->
                getDataSetItemsWithStates(syncContext.dataSetUid, *states)
            is SyncContext.DataSetInstance ->
                getDataSetInstanceItemsWithStates(syncContext.dataSetUid)
            is SyncContext.Enrollment ->
                getEnrollmentItemsWithStates(syncContext.enrollmentUid, *states)
            is SyncContext.EnrollmentEvent ->
                getEventItemsWithStates(syncContext.eventUid, *states)
            is SyncContext.Event ->
                getEventItemsWithStates(syncContext.eventUid, *states)
            is SyncContext.EventProgram ->
                getProgramItemsWithStates(syncContext.programUid, *states)
            is SyncContext.Global ->
                getHomeItemsWithStates(*states)
            is SyncContext.GlobalDataSet ->
                getDataSetItemsForGlobalContext(syncContext.dataSetUid)
            is SyncContext.GlobalEventProgram ->
                getProgramItemsForGlobalContext(syncContext.programUid, *states)
            is SyncContext.GlobalTrackerProgram ->
                getProgramItemsForGlobalContext(syncContext.programUid, *states)
            is SyncContext.TrackerProgram ->
                getProgramItemsWithStates(syncContext.programUid, *states)
            is SyncContext.TrackerProgramTei ->
                getTeiItemWithStates(syncContext.enrollmentUid)
        }
    }

    private fun getHomeItemsWithStates(vararg states: State): List<SyncStatusItem> {
        val programList = d2.programs().filter {
            states.contains(dhisProgramUtils.getProgramState(it))
        }.map { program ->
            SyncStatusItem(
                type = when (program.programType()) {
                    ProgramType.WITHOUT_REGISTRATION -> SyncStatusType.EventProgram(program.uid())
                    ProgramType.WITH_REGISTRATION ->
                        if (d2.isStockProgram(program.uid())) {
                            SyncStatusType.StockProgram(
                                programUid = program.uid(),
                                stockUsecase = d2.stockUseCase(program.uid())
                            )
                        } else {
                            SyncStatusType.TrackerProgram(
                                programUid = program.uid(),
                                trackedEntityTypeUid = program.trackedEntityType()!!.uid()
                            )
                        }
                    null ->
                        throw NullPointerException(
                            "Program ${program.uid()}: program type can't be null"
                        )
                },
                displayName = program.displayName() ?: program.uid(),
                description = resourceManager.getString(R.string.tap_to_explore_action),
                state = dhisProgramUtils.getProgramState(program)
            )
        }
        val dataSetList = d2.dataSetInstanceSummaries().filter {
            states.contains(it.state())
        }.map { dataSetInstanceSummary ->
            SyncStatusItem(
                type = SyncStatusType.DataSet(dataSetInstanceSummary.dataSetUid()),
                displayName = dataSetInstanceSummary.dataSetDisplayName(),
                description = resourceManager.getString(R.string.tap_to_explore_action),
                state = dataSetInstanceSummary.state()
            )
        }
        return (programList + dataSetList).sortedByState()
    }

    private fun getProgramItemsForGlobalContext(
        programUid: String,
        vararg state: State
    ): List<SyncStatusItem> {
        val program = d2.program(programUid)
        val programState = dhisProgramUtils.getProgramState(program)
        var errorCount = 0
        var warningCount = 0
        val syncStatusType = if (program.programType() == ProgramType.WITH_REGISTRATION) {
            d2.teisBy(
                programs = listOf(programUid),
                aggregatedSynStates = state.toList()
            ).forEach { tei ->
                if (tei.aggregatedSyncState() == State.ERROR) {
                    errorCount += getNumberOfConflictsForTei(tei.uid())
                } else if (tei.aggregatedSyncState() == State.WARNING) {
                    warningCount += getNumberOfConflictsForEvent(tei.uid())
                }
            }
            if (d2.isStockProgram(program.uid())) {
                SyncStatusType.StockProgram(
                    programUid = program.uid(),
                    stockUsecase = d2.stockUseCase(programUid)
                )
            } else {
                SyncStatusType.TrackerProgram(
                    programUid = program.uid(),
                    trackedEntityTypeUid = program.trackedEntityType()!!.uid()
                )
            }
        } else {
            d2.eventsBy(
                programUid = programUid,
                aggregatedSynStates = state.toList()
            ).forEach { event ->
                if (event.aggregatedSyncState() == State.ERROR) {
                    errorCount += getNumberOfConflictsForEvent(event.uid())
                } else if (event.aggregatedSyncState() == State.WARNING) {
                    warningCount += getNumberOfConflictsForEvent(event.uid())
                }
            }
            SyncStatusType.EventProgram(
                programUid = programUid
            )
        }
        val description = errorWarningDescriptionLabel(errorCount, warningCount)
        return listOf(
            SyncStatusItem(
                type = syncStatusType,
                displayName = program.displayName()!!,
                description = description,
                state = programState
            )
        )
    }

    private fun errorWarningDescriptionLabel(errorCount: Int, warningCount: Int) = when {
        errorCount > 0 && warningCount > 0 -> {
            val errorLabel = resourceManager.getPlural(R.plurals.error_count_label, errorCount)
                .format(errorCount)
            val warningLabel = resourceManager
                .getPlural(R.plurals.warning_count_label, warningCount)
                .format(warningCount)
            "$errorLabel, $warningLabel"
        }
        errorCount == 0 && warningCount > 0 ->
            resourceManager.getPlural(R.plurals.warning_count_label, warningCount)
                .format(warningCount)
        errorCount > 0 && warningCount == 0 ->
            resourceManager.getPlural(R.plurals.error_count_label, errorCount)
                .format(errorCount)
        else -> resourceManager.getString(R.string.tap_to_explore_action)
    }

    private fun getDataSetItemsForGlobalContext(dataSetUid: String): List<SyncStatusItem> {
        val datasetSummary = d2.dataSetSummaryBy(dataSetUid)
        var errorCount = 0
        var warningCount = 0
        d2.dataValueConflictsBy(dataSetUid).forEach { dataValueConflict ->
            if (dataValueConflict.status() == ImportStatus.ERROR) {
                errorCount += 1
            } else if (dataValueConflict.status() == ImportStatus.WARNING) {
                warningCount += 1
            }
        }
        val description = errorWarningDescriptionLabel(errorCount, warningCount)
        return listOf(
            SyncStatusItem(
                type = SyncStatusType.DataSet(dataSetUid),
                displayName = datasetSummary.dataSetDisplayName(),
                description = description,
                state = datasetSummary.state()
            )
        )
    }

    private fun getProgramItemsWithStates(
        programUid: String,
        vararg states: State
    ): List<SyncStatusItem> {
        val program = d2.program(programUid)
        return if (program.programType() == ProgramType.WITH_REGISTRATION) {
            d2.teisBy(
                programs = listOf(programUid),
                aggregatedSynStates = states.toList()
            ).map { tei ->
                mapTeiToSyncStatusItem(programUid, tei.uid())
            }.sortedByState()
        } else {
            d2.eventsBy(
                programUid = programUid,
                aggregatedSynStates = states.toList()
            ).map { event ->
                mapEventToSyncStatusItem(programUid, event.uid())
            }.sortedByState()
        }
    }

    private fun getTeiItemWithStates(enrollmentUid: String): List<SyncStatusItem> {
        val enrollment = d2.enrollment(enrollmentUid)
        return listOf(
            mapTeiToSyncStatusItem(
                enrollment.program()!!,
                enrollment.trackedEntityInstance()!!
            )
        )
    }

    private fun mapTeiToSyncStatusItem(programUid: String, teiUid: String): SyncStatusItem {
        val tei = d2.tei(teiUid)
        val description = when (tei.aggregatedSyncState()) {
            State.WARNING,
            State.ERROR -> {
                val conflicts = d2.teiImportConflictsBy(teiUid = teiUid)
                errorWarningDescriptionLabel(
                    errorCount = conflicts.count { it.status() == ImportStatus.ERROR },
                    warningCount = conflicts.count { it.status() == ImportStatus.WARNING }
                )
            }
            State.TO_POST,
            State.TO_UPDATE ->
                resourceManager.getString(R.string.tap_to_explore_action)
            else -> ""
        }

        val trackedEntityTypeName =
            d2.trackedEntityType(tei.trackedEntityType()!!).displayName()

        val teiMainAttribute =
            d2.teiMainAttributes(tei.uid(), programUid)

        val label = teiMainAttribute.firstOrNull()?.let { (attributeName, value) ->
            "$attributeName: $value"
        } ?: trackedEntityTypeName!!

        val activeEnrollmentInProgram = d2.enrollmentInProgram(
            teiUid = tei.uid(),
            programUid = programUid
        )
        return SyncStatusItem(
            type = SyncStatusType.TrackedEntity(
                teiUid = tei.uid(),
                programUid = programUid,
                enrollmentUid = activeEnrollmentInProgram?.uid()
            ),
            displayName = label,
            description = description,
            state = tei.aggregatedSyncState()!!
        )
    }

    private fun mapEventToSyncStatusItem(programUid: String, eventUid: String): SyncStatusItem {
        val event = d2.event(eventUid)
        val eventConflicts = d2.eventImportConflictsBy(eventUid)
        val description = when (event.aggregatedSyncState()) {
            State.WARNING,
            State.ERROR -> {
                errorWarningDescriptionLabel(
                    eventConflicts.count { it.status() == ImportStatus.ERROR },
                    eventConflicts.count { it.status() == ImportStatus.WARNING }
                )
            }
            State.TO_POST, State.TO_UPDATE ->
                resourceManager.getString(R.string.tap_to_explore_action)
            else -> ""
        }
        val hasNullDataElementConflict = eventConflicts.any { it.dataElement() == null }

        return SyncStatusItem(
            type = SyncStatusType.Event(
                eventUid = event.uid(),
                programUid = programUid,
                programStageUid = event.programStage(),
                hasNullDataElementConflict = hasNullDataElementConflict
            ),
            displayName = getEventLabel(event),
            description = description,
            state = event.aggregatedSyncState()!!
        )
    }

    private fun getNumberOfConflictsForTei(teiUid: String): Int {
        return d2.countTeiImportConflicts(teiUid)
    }

    private fun getNumberOfConflictsForEvent(eventUid: String): Int {
        return d2.countEventImportConflicts(eventUid)
    }

    private fun getEnrollmentItemsWithStates(
        enrollmentUid: String,
        vararg states: State
    ): List<SyncStatusItem> {
        val enrollment = d2.enrollment(enrollmentUid)
        val conflicts = if (states.contains(State.ERROR) || states.contains(State.WARNING)) {
            val allConflicts = d2.enrollmentImportConflicts(enrollmentUid)
            val enrollmentConflicts =
                allConflicts.filter { it.event() == null }.map { trackerImportConflict ->
                    when {
                        trackerImportConflict.trackedEntityAttribute() != null -> {
                            val attribute =
                                d2.teiAttribute(trackerImportConflict.trackedEntityAttribute()!!)
                            SyncStatusItem(
                                type = SyncStatusType.Enrollment(
                                    enrollmentUid = trackerImportConflict.enrollment()!!,
                                    programUid = enrollment.program()!!
                                ),
                                displayName = attribute.displayFormName() ?: attribute.uid(),
                                description = trackerImportConflict.displayDescription() ?: "",
                                state = trackerImportConflict.status()?.toState()
                                    ?: enrollment.aggregatedSyncState()!!
                            )
                        }
                        else -> {
                            val program = d2.program(enrollment.program()!!)
                            SyncStatusItem(
                                type = SyncStatusType.Enrollment(
                                    enrollmentUid = enrollment.uid(),
                                    programUid = program.uid()
                                ),
                                displayName = program.displayName() ?: program.uid(),
                                description = trackerImportConflict.displayDescription() ?: "",
                                state = trackerImportConflict.status()?.toState()
                                    ?: enrollment.aggregatedSyncState()!!
                            )
                        }
                    }
                }
            val eventConflicts =
                allConflicts.filter { it.event() != null }.map { trackerImportConflict ->
                    mapEventToSyncStatusItem(
                        enrollment.program()!!,
                        trackerImportConflict.event()!!
                    )
                }
            (enrollmentConflicts + eventConflicts)
        } else {
            emptyList()
        }

        val teiConflicts = if (states.contains(State.ERROR) || states.contains(State.WARNING)) {
            d2.teiImportConflictsBy(
                teiUid = enrollment.trackedEntityInstance(),
                byNullEnrollment = true
            ).mapNotNull { trackerImportConflict ->
                val tei = d2.tei(enrollment.trackedEntityInstance()!!)
                when {
                    trackerImportConflict.trackedEntityAttribute() != null -> {
                        val attribute =
                            d2.teiAttribute(trackerImportConflict.trackedEntityAttribute()!!)
                        SyncStatusItem(
                            type = SyncStatusType.TrackedEntity(
                                teiUid = enrollment.trackedEntityInstance()!!,
                                programUid = enrollment.program(),
                                enrollmentUid = trackerImportConflict.enrollment()
                            ),
                            displayName = attribute.displayFormName() ?: attribute.uid(),
                            description = trackerImportConflict.displayDescription() ?: "",
                            state = trackerImportConflict.status()?.toState()
                                ?: enrollment.aggregatedSyncState()!!
                        )
                    }

                    trackerImportConflict.trackedEntityInstance() != null -> {
                        SyncStatusItem(
                            type = SyncStatusType.TrackedEntity(
                                enrollment.trackedEntityInstance()!!,
                                enrollment.program(),
                                enrollmentUid
                            ),
                            displayName = resourceManager.getString(R.string.conflict),
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
        val notSyncedEvents = d2.eventsBy(
            enrollmentUid = enrollmentUid,
            aggregatedSynStates = statesWithoutError.toList()
        ).map { event ->
            mapEventToSyncStatusItem(
                eventUid = event.uid(),
                dataElementUid = null,
                description = resourceManager.getString(R.string.tap_to_explore_action)
            )
        }

        return (conflicts + teiConflicts + notSyncedEvents).sortedByState()
    }

    private fun mapEventToSyncStatusItem(
        eventUid: String,
        dataElementUid: String?,
        description: String
    ): SyncStatusItem {
        val event = d2.event(eventUid)
        val dataElement = dataElementUid?.let { d2.dataElement(dataElementUid) }
        return SyncStatusItem(
            type = SyncStatusType.Event(
                eventUid = event.uid(),
                programUid = event.program()!!,
                programStageUid = event.programStage(),
                hasNullDataElementConflict = dataElementUid == null
            ),
            displayName = dataElement?.displayFormName() ?: getEventLabel(event),
            description = description,
            state = event.aggregatedSyncState()!!
        )
    }

    private fun getEventLabel(event: Event): String {
        val eventLabelData = mutableListOf<String>().apply {
            (event.eventDate() ?: event.dueDate()).toUi()?.let {
                add(it)
            }
            event.organisationUnit()?.let { orgUnitUid ->
                d2.organisationUnit(orgUnitUid).displayName()?.let { orgUnitName ->
                    add(orgUnitName)
                }
            }
            event.attributeOptionCombo()?.let { categoryOptionComboUid ->
                d2.categoryOptionCombo(categoryOptionComboUid).displayName()
                    ?.takeIf { it != "default" }
                    ?.let { categoryOptionComboName ->
                        add(categoryOptionComboName)
                    }
            }
        }
        return eventLabelData.joinToString(" | ")
    }

    private fun getEventItemsWithStates(
        eventUid: String,
        vararg states: State
    ): List<SyncStatusItem> {
        return if (states.contains(State.ERROR) || states.contains(State.WARNING)) {
            d2.eventImportConflictsBy(eventUid).map { trackerImportConflict ->
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

    private fun getDataSetItemsWithStates(
        dataSetUid: String,
        vararg states: State
    ): List<SyncStatusItem> {
        return d2.dataSetInstancesBy(
            dataSetUid = dataSetUid,
            states = states.toList()
        ).map { dataSetInstance ->
            if (
                dataSetInstance.state() == State.ERROR ||
                dataSetInstance.state() == State.WARNING
            ) {
                var errorCount = 0
                var warningCount = 0
                d2.dataValueConflictsBy(dataSetUid).forEach { dataValueConflict ->
                    if (dataValueConflict.status() == ImportStatus.ERROR) {
                        errorCount += 1
                    } else if (dataValueConflict.status() == ImportStatus.WARNING) {
                        warningCount += 1
                    }
                }
                SyncStatusItem(
                    type = SyncStatusType.DataSetInstance(
                        dataSetUid,
                        dataSetInstance.organisationUnitUid(),
                        dataSetInstance.period(),
                        dataSetInstance.attributeOptionComboUid()
                    ),
                    displayName = getDataSetInstanceLabel(dataSetInstance),
                    description = errorWarningDescriptionLabel(errorCount, warningCount),
                    state = dataSetInstance.state()!!
                )
            } else {
                SyncStatusItem(
                    type = SyncStatusType.DataSetInstance(
                        dataSetUid,
                        dataSetInstance.organisationUnitUid(),
                        dataSetInstance.period(),
                        dataSetInstance.attributeOptionComboUid()
                    ),
                    displayName = getDataSetInstanceLabel(dataSetInstance),
                    description = resourceManager.getString(R.string.tap_to_explore_action),
                    state = dataSetInstance.state()!!
                )
            }
        }.sortedByState()
    }

    private fun getDataSetInstanceLabel(dataSetInstance: DataSetInstance): String {
        val dataSetInstanceLabelData = mutableListOf<String>().apply {
            add(dataSetInstance.organisationUnitDisplayName())
            dataSetInstance.period().let { periodId ->
                val period = d2.period(periodId)
                add(
                    periodUtils.getPeriodUIString(
                        period.periodType(),
                        period.startDate()!!,
                        Locale.getDefault()
                    )
                )
            }
            dataSetInstance.attributeOptionComboDisplayName().takeIf { it != "default" }?.let {
                add(it)
            }
        }
        return dataSetInstanceLabelData.joinToString(" | ")
    }

    private fun getDataSetInstanceItemsWithStates(dataSetUid: String): List<SyncStatusItem> {
        val conflicts = with(syncContext as SyncContext.DataSetInstance) {
            d2.dataValueConflicts(
                dataSetUid = dataSetUid,
                periodId = periodId,
                orgUnitUid = orgUnitUid,
                attrOptionComboUid = attributeOptionComboUid
            )
        }

        return conflicts.mapNotNull { dataValueConflict ->
            when {
                dataValueConflict.dataElement() != null -> {
                    val dataElement = d2.dataElement(dataValueConflict.dataElement()!!)
                    val catOptComboLabel =
                        dataValueConflict.categoryOptionCombo()?.let { catOptComboUid ->
                            d2.categoryOptionCombo(catOptComboUid).displayName() ?: catOptComboUid
                        }
                    val dataElementLabel = dataElement.displayFormName() ?: dataElement.uid()
                    SyncStatusItem(
                        type = SyncStatusType.DataSetInstance(
                            dataSetUid = dataSetUid,
                            orgUnitUid = dataValueConflict.orgUnit()!!,
                            periodId = dataValueConflict.period()!!,
                            attrOptComboUid = dataValueConflict.attributeOptionCombo()!!
                        ),
                        displayName = "$dataElementLabel | $catOptComboLabel",
                        description = dataValueConflict.displayDescription() ?: "",
                        state = dataValueConflict.status()?.toState() ?: State.ERROR
                    )
                }
                dataValueConflict.categoryOptionCombo() != null -> {
                    val catOptCombo =
                        d2.categoryOptionCombo(dataValueConflict.categoryOptionCombo()!!)
                    val catOptComboLabel = catOptCombo.displayName() ?: catOptCombo.uid()
                    SyncStatusItem(
                        type = SyncStatusType.DataSetInstance(
                            dataSetUid = dataSetUid,
                            orgUnitUid = dataValueConflict.orgUnit()!!,
                            periodId = dataValueConflict.period()!!,
                            attrOptComboUid = dataValueConflict.attributeOptionCombo()!!
                        ),
                        displayName = catOptComboLabel,
                        description = dataValueConflict.displayDescription() ?: "",
                        state = dataValueConflict.status()?.toState() ?: State.ERROR
                    )
                }
                else -> null
            }
        }
    }

    private fun getStateFromCandidates(stateCandidates: MutableList<State?>): State {
        if (syncContext.conflictType() == ConflictType.DATA_SET) {
            stateCandidates.addAll(
                d2.dataSetModule().dataSetCompleteRegistrations()
                    .byDataSetUid().eq(syncContext.recordUid())
                    .blockingGet().map { it.syncState() }
            )
        } else {
            with(syncContext as SyncContext.DataSetInstance) {
                stateCandidates.addAll(
                    d2.dataSetModule().dataSetCompleteRegistrations()
                        .byOrganisationUnitUid().eq(orgUnitUid)
                        .byPeriod().eq(periodId)
                        .byAttributeOptionComboUid().eq(attributeOptionComboUid)
                        .byDataSetUid().eq(dataSetUid).get()
                        .blockingGet().map { it.syncState() }
                )
            }
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

    private fun getMessageArgument(): String {
        return getTitle().blockingGet()
    }

    private fun getTitle(): Single<String> {
        return when (syncContext.conflictType()) {
            ConflictType.ALL -> Single.just("")
            ConflictType.PROGRAM ->
                d2.programModule().programs().uid(syncContext.recordUid()).get()
                    .map { it.displayName() }
            ConflictType.TEI -> {
                val enrollment =
                    d2.enrollmentModule().enrollments().uid(syncContext.recordUid()).blockingGet()
                d2.trackedEntityModule().trackedEntityTypes().uid(
                    d2.trackedEntityModule().trackedEntityInstances()
                        .uid(enrollment.trackedEntityInstance()!!)
                        .blockingGet().trackedEntityType()
                )
                    .get().map { it.displayName() }
            }
            ConflictType.EVENT ->
                d2.programModule().programStages().uid(
                    d2.eventModule().events().uid(syncContext.recordUid()).blockingGet()
                        .programStage()
                ).get().map { it.displayName() }
            ConflictType.DATA_SET, ConflictType.DATA_VALUES ->
                d2.dataSetModule().dataSets().uid(syncContext.recordUid()).get()
                    .map { it.displayName() }
        }
    }
}

fun List<SyncStatusItem>.sortedByState(): List<SyncStatusItem> {
    return sortedWith(
        compareBy<SyncStatusItem> { it.state.priority() }
            .thenBy { it.priority() }
    )
}

fun SyncStatusItem.priority(): Int {
    return when (this.type) {
        is SyncStatusType.DataSet,
        is SyncStatusType.EventProgram,
        is SyncStatusType.TrackerProgram,
        is SyncStatusType.StockProgram -> 1
        is SyncStatusType.Enrollment -> 2
        is SyncStatusType.DataSetInstance,
        is SyncStatusType.Event,
        is SyncStatusType.TrackedEntity -> 3
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
