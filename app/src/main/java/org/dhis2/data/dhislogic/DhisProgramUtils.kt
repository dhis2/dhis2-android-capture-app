package org.dhis2.data.dhislogic

import io.reactivex.Flowable
import javax.inject.Inject
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.enrollment.EnrollmentCollectionRepository
import org.hisp.dhis.android.core.event.EventCollectionRepository
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceCollectionRepository

class DhisProgramUtils @Inject constructor(val d2: D2) {

    fun getServerState(): State {
        val states = d2.programModule().programs().blockingGet().map {
            getProgramState(it)
        }.plus(
            d2.dataSetModule().dataSetInstanceSummaries().blockingGet().map {
                it.state()
            }
        ).distinct()

        return when {
            states.contains(State.ERROR) -> State.ERROR
            states.contains(State.WARNING) -> State.WARNING
            states.contains(State.TO_POST) -> State.TO_POST
            states.contains(State.TO_UPDATE) -> State.TO_UPDATE
            states.contains(State.SENT_VIA_SMS) or states.contains(State.SYNCED_VIA_SMS) ->
                State.SENT_VIA_SMS
            else -> State.SYNCED
        }
    }

    fun getProgramState(program: Program): State {
        return when (program.programType()) {
            ProgramType.WITH_REGISTRATION -> getTrackerProgramState(program)
            ProgramType.WITHOUT_REGISTRATION -> getEventProgramState(program)
            else -> throw Exception("Unsupported program type")
        }
    }

    fun getProgramState(programUid: String): State {
        return getProgramState(
            d2.programModule().programs().uid(programUid).blockingGet()
        )
    }

    fun getProgramsInCaptureOrgUnits(): Flowable<List<Program>> {
        return d2.programModule().programs()
            .withTrackedEntityType()
            .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
            .get().toFlowable()
    }

    private fun getTrackerProgramState(program: Program): State {
        val teiRepository = d2.trackedEntityModule().trackedEntityInstances()
            .byProgramUids(arrayListOf(program.uid()))
        val enrollmentRepository = d2.enrollmentModule().enrollments()
            .byProgram().eq(program.uid())

        return when {
            hasTeiWithErrorState(teiRepository, enrollmentRepository) -> State.ERROR
            hasTeiWithWarningState(teiRepository, enrollmentRepository) -> State.WARNING
            hasTeiWithSMSState(teiRepository) -> State.SENT_VIA_SMS
            hasTeiWithNotSyncedStateOrDeleted(teiRepository) -> State.TO_UPDATE
            else -> State.SYNCED
        }
    }

    private fun hasTeiWithWarningState(
        teiRepository: TrackedEntityInstanceCollectionRepository,
        enrollmentRepository: EnrollmentCollectionRepository
    ): Boolean {
        return teiRepository
            .byDeleted().isFalse
            .byAggregatedSyncState().`in`(State.WARNING)
            .blockingGet().isNotEmpty() ||
            enrollmentRepository.byAggregatedSyncState().`in`(State.WARNING)
                .blockingGet().isNotEmpty()
    }

    private fun hasTeiWithErrorState(
        teiRepository: TrackedEntityInstanceCollectionRepository,
        enrollmentRepository: EnrollmentCollectionRepository
    ): Boolean {
        return teiRepository
            .byDeleted().isFalse
            .byAggregatedSyncState().`in`(State.ERROR)
            .blockingGet().isNotEmpty() ||
            enrollmentRepository.byAggregatedSyncState().`in`(State.ERROR)
                .blockingGet().isNotEmpty()
    }

    private fun hasTeiWithSMSState(
        teiRepository: TrackedEntityInstanceCollectionRepository
    ): Boolean {
        return teiRepository
            .byDeleted().isFalse
            .byAggregatedSyncState().`in`(State.SENT_VIA_SMS, State.SYNCED_VIA_SMS)
            .blockingGet().isNotEmpty()
    }

    private fun hasTeiWithNotSyncedStateOrDeleted(
        teiRepository: TrackedEntityInstanceCollectionRepository
    ): Boolean {
        return teiRepository
            .byDeleted().isFalse
            .byAggregatedSyncState().`in`(State.TO_UPDATE, State.TO_POST, State.UPLOADING)
            .blockingGet().isNotEmpty() ||
            teiRepository
                .byDeleted().isTrue.blockingGet().isNotEmpty()
    }

    private fun getEventProgramState(program: Program): State {
        val eventRepository = d2.eventModule().events()
            .byProgramUid().eq(program.uid())
        return when {
            hasEventWithErrorState(eventRepository) -> State.ERROR
            hasEventWithWarningState(eventRepository) -> State.WARNING
            hasEventWithSMSState(eventRepository) -> State.SENT_VIA_SMS
            hasEventWithNotSyncedStateOrDeleted(eventRepository) -> State.TO_UPDATE
            else -> State.SYNCED
        }
    }

    private fun hasEventWithErrorState(eventRepository: EventCollectionRepository): Boolean {
        return eventRepository
            .byDeleted().isFalse
            .byAggregatedSyncState().`in`(State.ERROR)
            .blockingGet().isNotEmpty()
    }

    private fun hasEventWithWarningState(eventRepository: EventCollectionRepository): Boolean {
        return eventRepository
            .byDeleted().isFalse
            .byAggregatedSyncState().`in`(State.WARNING)
            .blockingGet().isNotEmpty()
    }

    private fun hasEventWithSMSState(eventRepository: EventCollectionRepository): Boolean {
        return eventRepository
            .byDeleted().isFalse
            .byAggregatedSyncState().`in`(State.SENT_VIA_SMS, State.SYNCED_VIA_SMS)
            .blockingGet().isNotEmpty()
    }

    private fun hasEventWithNotSyncedStateOrDeleted(
        eventRepository: EventCollectionRepository
    ): Boolean {
        return eventRepository
            .byDeleted().isFalse
            .byAggregatedSyncState().`in`(State.TO_UPDATE, State.TO_POST, State.UPLOADING)
            .blockingGet().isNotEmpty() ||
            eventRepository
                .byDeleted().isTrue
                .blockingGet().isNotEmpty()
    }

    fun getProgramRecordLabel(
        program: Program,
        defaultTrackerLabel: String,
        defaultEventLabel: String
    ): String {
        return when (program.programType()) {
            ProgramType.WITH_REGISTRATION -> {
                program.trackedEntityType()?.displayName() ?: defaultTrackerLabel
            }
            ProgramType.WITHOUT_REGISTRATION -> defaultEventLabel
            null -> ""
        }
    }
}
