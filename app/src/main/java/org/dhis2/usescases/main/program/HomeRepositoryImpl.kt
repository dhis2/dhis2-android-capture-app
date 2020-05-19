package org.dhis2.usescases.main.program

import io.reactivex.Flowable
import io.reactivex.parallel.ParallelFlowable
import java.util.Date
import org.dhis2.data.schedulers.SchedulerProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.dataset.DataSet
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentCollectionRepository
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.period.DatePeriod
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramType.WITHOUT_REGISTRATION
import org.hisp.dhis.android.core.program.ProgramType.WITH_REGISTRATION

internal class HomeRepositoryImpl(
    private val d2: D2,
    private val eventLabel: String,
    private val dataSetLabel: String,
    private val teiLabel: String,
    private val schedulerProvider: SchedulerProvider
) :
    HomeRepository {

    private var captureOrgUnits: List<String> = ArrayList()

    override fun aggregatesModels(
        dateFilter: List<DatePeriod>,
        orgUnitFilter: List<String>,
        statesFilter: List<State>,
        assignedToUser: Boolean?
    ): Flowable<List<ProgramViewModel>> {
        return ParallelFlowable.from<DataSet>(
            Flowable.fromIterable<DataSet>(d2.dataSetModule().dataSets().blockingGet())
        )
            .runOn(schedulerProvider.io())
            .map { dataSet ->
                var repo = d2.dataSetModule().dataSetInstances().byDataSetUid().eq(dataSet.uid())
                if (orgUnitFilter.isNotEmpty()) {
                    repo = repo.byOrganisationUnitUid().`in`(orgUnitFilter)
                }
                if (dateFilter.isNotEmpty()) {
                    repo = repo.byPeriodStartDate().inDatePeriods(dateFilter)
                }

                var count = 0
                if (assignedToUser != true) {
                    if (statesFilter.isNotEmpty()) {
                        for (instance in repo.blockingGet()) {
                            if (statesFilter.contains(instance.state())) {
                                count++
                            }
                        }
                    } else {
                        count = repo.blockingCount()
                    }
                }

                val possibleStates = repo.blockingGet().map { it.state() }.toMutableList()

                possibleStates.addAll(
                    d2.dataSetModule().dataSetCompleteRegistrations()
                        .byDataSetUid().eq(dataSet.uid())
                        .blockingGet().map { it.state() }
                )

                val state = when {
                    possibleStates.contains(State.ERROR) ||
                        possibleStates.contains(State.WARNING) ->
                        State.WARNING
                    possibleStates.contains(State.SENT_VIA_SMS) ||
                        possibleStates.contains(State.SYNCED_VIA_SMS) ->
                        State.SENT_VIA_SMS
                    possibleStates.contains(State.TO_UPDATE) ||
                        possibleStates.contains(State.UPLOADING) ||
                        possibleStates.contains(State.TO_POST) ->
                        State.TO_UPDATE
                    else -> State.SYNCED
                }

                ProgramViewModel.create(
                    dataSet.uid(),
                    dataSet.displayName()!!,
                    if (dataSet.style() != null) dataSet.style()!!.color() else null,
                    if (dataSet.style() != null) dataSet.style()!!.icon() else null,
                    count, null,
                    dataSetLabel,
                    "",
                    dataSet.displayDescription(),
                    true,
                    dataSet.access().data().write()!!,
                    state.name
                )
            }.sequential()
            .map { program ->
                program.setTranslucent(
                    (
                        dateFilter.isNotEmpty() ||
                            orgUnitFilter.isNotEmpty() ||
                            statesFilter.isNotEmpty() ||
                            assignedToUser == true
                        ) &&
                        program.count() == 0
                )
            }
            .toList().toFlowable()
    }

    override fun programModels(
        dateFilter: List<DatePeriod>,
        orgUnitFilter: List<String>,
        statesFilter: List<State>,
        assignedToUser: Boolean?
    ): Flowable<List<ProgramViewModel>> {
        return getCaptureOrgUnits()
            .map { captureOrgUnits ->
                this.captureOrgUnits = captureOrgUnits
                d2.programModule().programs()
                    .withTrackedEntityType()
                    .byOrganisationUnitList(captureOrgUnits)
            }
            .flatMap { programRepo ->
                ParallelFlowable.from(Flowable.fromIterable(programRepo.blockingGet()))
                    .runOn(schedulerProvider.io())
                    .sequential()
            }
            .map { program ->
                val typeName = getProgramTypeName(program)

                var (count, hasOverdue) = Pair(0, false)
                val state: State

                if (program.programType() == WITHOUT_REGISTRATION) {
                    count = getCountForProgramWithoutRegistration(
                        program,
                        dateFilter,
                        statesFilter,
                        orgUnitFilter,
                        assignedToUser
                    )

                    state = getStateForProgramWithoutRegistration(program)
                } else {
                    val (mCount, mOverdue) = getCountForProgramWithRegistration(
                        program,
                        dateFilter,
                        statesFilter,
                        orgUnitFilter,
                        assignedToUser
                    )

                    count = mCount
                    hasOverdue = mOverdue

                    state = getStateForProgramWithRegistration(program)
                }

                ProgramViewModel.create(
                    program.uid(),
                    program.displayName()!!,
                    if (program.style() != null) program.style()!!.color() else null,
                    if (program.style() != null) program.style()!!.icon() else null,
                    count,
                    if (program.trackedEntityType() != null) {
                        program.trackedEntityType()!!.uid()
                    } else {
                        null
                    },
                    typeName,
                    program.programType()!!.name,
                    program.displayDescription(),
                    onlyEnrollOnce = true,
                    accessDataWrite = true,
                    state = state.name,
                    hasOverdueEvent = hasOverdue
                )
            }.map { program ->
                program.setTranslucent(
                    (
                        dateFilter.isNotEmpty() ||
                            orgUnitFilter.isNotEmpty() ||
                            statesFilter.isNotEmpty() ||
                            assignedToUser == true
                        ) &&
                        program.count() == 0
                )
            }
            .toList().toFlowable()
    }

    private fun getStateForProgramWithRegistration(program: Program): State {
        return if (d2.trackedEntityModule().trackedEntityInstances()
            .byProgramUids(arrayListOf(program.uid())).byState().`in`(
                State.ERROR,
                State.WARNING
            )
            .blockingGet().isNotEmpty()
        ) {
            State.WARNING
        } else if (d2.trackedEntityModule().trackedEntityInstances()
            .byProgramUids(arrayListOf(program.uid()))
            .byState().`in`(
                State.SENT_VIA_SMS,
                State.SYNCED_VIA_SMS
            ).blockingGet().isNotEmpty()
        ) {
            State.SENT_VIA_SMS
        } else if (d2.trackedEntityModule().trackedEntityInstances()
            .byProgramUids(arrayListOf(program.uid()))
            .byState().`in`(
                State.TO_UPDATE,
                State.TO_POST,
                State.UPLOADING
            ).blockingGet().isNotEmpty() ||
            d2.trackedEntityModule().trackedEntityInstances()
                .byProgramUids(arrayListOf(program.uid()))
                .byDeleted().isTrue.blockingGet().isNotEmpty()
        ) {
            State.TO_UPDATE
        } else {
            State.SYNCED
        }
    }

    private fun getCountForProgramWithRegistration(
        program: Program,
        dateFilter: List<DatePeriod>,
        statesFilter: List<State>,
        orgUnitFilter: List<String>,
        assignedToUser: Boolean?
    ): Pair<Int, Boolean> {
        var enrollmentRepository = d2.enrollmentModule().enrollments()
            .byProgram().`in`(arrayListOf(program.uid()))
        if (dateFilter.isNotEmpty()) {
            enrollmentRepository = enrollmentRepository
                .byEnrollmentDate().inDatePeriods(dateFilter)
        }
        if (statesFilter.isNotEmpty()) {
            enrollmentRepository = enrollmentRepository
                .byState().`in`(statesFilter)
        }
        if (orgUnitFilter.isNotEmpty()) {
            enrollmentRepository = enrollmentRepository
                .byOrganisationUnit().`in`(orgUnitFilter)
        }
        assignedToUser?.let {
            if (assignedToUser) {
                enrollmentRepository = enrollmentRepository
                    .byUid()
                    .`in`(getEnrollmentsWithAssignedEvents(enrollmentRepository))
            }
        }

        val enrollments = enrollmentRepository.blockingGet()
        return countEnrollment(enrollments)
    }

    private fun getProgramTypeName(program: Program): String {
        var typeName: String?
        if (program.programType() == WITH_REGISTRATION) {
            typeName =
                if (program.trackedEntityType() != null) {
                    program.trackedEntityType()!!.displayName()
                } else {
                    teiLabel
                }
            if (typeName == null) {
                typeName =
                    d2.trackedEntityModule()
                        .trackedEntityTypes()
                        .uid(program.trackedEntityType()!!.uid())
                        .blockingGet()!!
                        .displayName()
            }
        } else if (program.programType() == WITHOUT_REGISTRATION) {
            typeName = eventLabel
        } else {
            typeName = dataSetLabel
        }

        return typeName!!
    }

    private fun getCurrentUser(): String {
        return d2.userModule().user().blockingGet().uid()
    }

    private fun getCountForProgramWithoutRegistration(
        program: Program,
        dateFilter: List<DatePeriod>,
        statesFilter: List<State>,
        orgUnitFilter: List<String>,
        assignedToUser: Boolean?
    ): Int {
        var eventRepository = d2.eventModule().events()
            .byDeleted().isFalse
            .byProgramUid().eq(program.uid())

        if (dateFilter.isNotEmpty()) {
            eventRepository = eventRepository
                .byEventDate().inDatePeriods(dateFilter)
        }
        if (statesFilter.isNotEmpty()) {
            eventRepository = eventRepository
                .byState().`in`(statesFilter)
        }
        if (orgUnitFilter.isNotEmpty()) {
            eventRepository = eventRepository
                .byOrganisationUnitUid().`in`(orgUnitFilter)
        }
        assignedToUser?.let {
            if (assignedToUser) {
                eventRepository = eventRepository
                    .byAssignedUser().eq(getCurrentUser())
            }
        }
        return eventRepository.blockingCount()
    }

    private fun getStateForProgramWithoutRegistration(
        program: Program
    ): State {
        return if (
            d2.eventModule().events()
                .byDeleted().isFalse
                .byProgramUid().eq(program.uid()).byState().`in`(
                    State.ERROR,
                    State.WARNING
                ).blockingGet().isNotEmpty()
        ) {
            State.WARNING
        } else if (
            d2.eventModule().events()
                .byDeleted().isFalse
                .byProgramUid().eq(program.uid()).byState().`in`(
                    State.SENT_VIA_SMS,
                    State.SYNCED_VIA_SMS
                ).blockingGet().isNotEmpty()
        ) {
            State.SENT_VIA_SMS
        } else if (
            d2.eventModule().events()
                .byDeleted().isFalse
                .byProgramUid().eq(program.uid()).byState().`in`(
                    State.TO_UPDATE,
                    State.TO_POST,
                    State.UPLOADING
                )
                .blockingGet().isNotEmpty() ||
            d2.eventModule().events()
                .byDeleted().isFalse
                .byProgramUid().eq(program.uid())
                .byDeleted().isTrue.blockingGet().isNotEmpty()
        ) {
            State.TO_UPDATE
        } else {
            State.SYNCED
        }
    }

    private fun getEnrollmentsWithAssignedEvents(
        enrollmentRepository: EnrollmentCollectionRepository
    ): List<String> {
        val currentEnrollments = enrollmentRepository.blockingGet()
            .map { it.uid() }

        return d2.eventModule().events()
            .byDeleted().isFalse
            .byAssignedUser().eq(getCurrentUser())
            .byEnrollmentUid().`in`(currentEnrollments)
            .blockingGet()
            .distinctBy { it.enrollment() }
            .mapNotNull { it.enrollment() }
    }

    private fun getCaptureOrgUnits(): Flowable<List<String>> {
        return if (captureOrgUnits.isNotEmpty()) {
            Flowable.just(captureOrgUnits)
        } else {
            d2.organisationUnitModule()
                .organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .get()
                .toFlowable()
                .map { UidsHelper.getUidsList(it) }
        }
    }

    private fun countEnrollment(enrollments: List<Enrollment>): Pair<Int, Boolean> {
        val teiUids = ArrayList<String>()
        var hasOverdue = false
        for (enrollment in enrollments) {
            if (!teiUids.contains(enrollment.trackedEntityInstance())) {
                teiUids.add(enrollment.trackedEntityInstance()!!)
            }
            if (enrollment.status() == EnrollmentStatus.ACTIVE && !hasOverdue) {
                hasOverdue = !d2.eventModule().events()
                    .byDeleted().isFalse
                    .byEnrollmentUid().eq(enrollment.uid())
                    .byStatus().eq(EventStatus.OVERDUE).blockingIsEmpty() ||
                    !d2.eventModule().events()
                        .byDeleted().isFalse
                        .byEnrollmentUid().eq(enrollment.uid())
                        .byStatus().eq(EventStatus.SCHEDULE)
                        .byDueDate().before(Date()).blockingIsEmpty()
            }
        }
        return Pair(teiUids.size, hasOverdue)
    }
}
