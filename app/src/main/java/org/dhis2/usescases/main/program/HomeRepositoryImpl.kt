package org.dhis2.usescases.main.program

import io.reactivex.Flowable
import io.reactivex.parallel.ParallelFlowable
import io.reactivex.schedulers.Schedulers
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.dataset.DataSet
import org.hisp.dhis.android.core.dataset.DataSetCollectionRepository
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.period.DatePeriod
import org.hisp.dhis.android.core.program.ProgramCollectionRepository
import org.hisp.dhis.android.core.program.ProgramType.WITHOUT_REGISTRATION
import org.hisp.dhis.android.core.program.ProgramType.WITH_REGISTRATION

internal class HomeRepositoryImpl(private val d2: D2, private val eventLabel: String) :
    HomeRepository {
    private val dataSetRepository: DataSetCollectionRepository = d2.dataSetModule().dataSets()
        .withDataSetElements()
    private val programRepository: ProgramCollectionRepository = d2.programModule().programs()
        .withTrackedEntityType()

    private var captureOrgUnits: List<String> = ArrayList()

    override fun aggregatesModels(
        dateFilter: List<DatePeriod>,
        orgUnitFilter: List<String>,
        statesFilter: List<State>
    ): Flowable<List<ProgramViewModel>> {
        return ParallelFlowable.from<DataSet>(
            Flowable.fromIterable<DataSet>(dataSetRepository.blockingGet())
        )
            .runOn(Schedulers.io())
            .map { dataSet ->
                var repo = d2.dataSetModule().dataSetInstances().byDataSetUid().eq(dataSet.uid())
                if (orgUnitFilter.isNotEmpty()) {
                    repo = repo.byOrganisationUnitUid().`in`(orgUnitFilter)
                }
                if (dateFilter.isNotEmpty()) {
                    repo = repo.byPeriodStartDate().inDatePeriods(dateFilter)
                }

                var count = 0
                if (statesFilter.isNotEmpty()) {
                    for (instance in repo.blockingGet()) {
                        if (statesFilter.contains(instance.state())) {
                            count++
                        }
                    }
                } else {
                    count = repo.blockingCount()
                }

                val possibleStates = repo.blockingGet().map { it.state() }.toMutableList()

                possibleStates.addAll(d2.dataSetModule().dataSetCompleteRegistrations()
                    .byDataSetUid().eq(dataSet.uid())
                    .blockingGet().map { it.state() })


                val state = when {
                    possibleStates.contains(State.ERROR) ||
                            possibleStates.contains(State.WARNING) -> State.WARNING
                    possibleStates.contains(State.SENT_VIA_SMS) ||
                            possibleStates.contains(State.SYNCED_VIA_SMS) -> State.SENT_VIA_SMS
                    possibleStates.contains(State.TO_UPDATE) ||
                            possibleStates.contains(State.TO_POST) -> State.TO_UPDATE
                    else -> State.SYNCED
                }

                ProgramViewModel.create(
                    dataSet.uid(),
                    dataSet.displayName()!!,
                    if (dataSet.style() != null) dataSet.style()!!.color() else null,
                    if (dataSet.style() != null) dataSet.style()!!.icon() else null,
                    count, null,
                    "DataSets",
                    "",
                    dataSet.displayDescription(),
                    true,
                    dataSet.access().data().write()!!,
                    state.name
                )
            }.sequential().toList().toFlowable()
    }

    override fun programModels(
        dateFilter: List<DatePeriod>,
        orgUnitFilter: List<String>,
        statesFilter: List<State>
    ): Flowable<List<ProgramViewModel>> {
        return getCaptureOrgUnits()
            .map { captureOrgUnits ->
                this.captureOrgUnits = captureOrgUnits
                if (orgUnitFilter.isNotEmpty()) {
                    programRepository.byOrganisationUnitList(orgUnitFilter)
                } else {
                    programRepository.byOrganisationUnitList(captureOrgUnits)
                }
            }
            .flatMap { programRepo ->
                ParallelFlowable.from(Flowable.fromIterable(programRepo.blockingGet()))
                    .runOn(Schedulers.io())
                    .sequential()
            }
            .map { program ->
                var typeName: String?
                if (program.programType() == WITH_REGISTRATION) {
                    typeName =
                        if (program.trackedEntityType() != null) {
                            program.trackedEntityType()!!.displayName()
                        } else {
                            "TEI"
                        }
                    if (typeName == null) {
                        typeName =
                            d2
                                .trackedEntityModule()
                                .trackedEntityTypes()
                                .uid(program.trackedEntityType()!!.uid())
                                .blockingGet()!!
                                .displayName()
                    }
                } else if (program.programType() == WITHOUT_REGISTRATION) {
                    typeName = eventLabel
                } else {
                    typeName = "DataSets"
                }

                val count: Int
                var state = State.SYNCED
                if (program.programType() == WITHOUT_REGISTRATION) {
                    if (dateFilter.isNotEmpty()) {
                        if (orgUnitFilter.isNotEmpty()) {
                            count = if (statesFilter.isNotEmpty()) {
                                d2.eventModule().events()
                                    .byProgramUid().eq(program.uid())
                                    .byEventDate().inDatePeriods(dateFilter)
                                    .byOrganisationUnitUid().`in`(orgUnitFilter)
                                    .byState().`in`(statesFilter)
                                    .blockingCount()
                            } else {
                                d2.eventModule().events()
                                    .byProgramUid().eq(program.uid())
                                    .byEventDate().inDatePeriods(dateFilter)
                                    .byOrganisationUnitUid().`in`(orgUnitFilter)
                                    .blockingCount()
                            }
                        } else {
                            count = if (statesFilter.isNotEmpty()) {
                                d2.eventModule().events()
                                    .byProgramUid().eq(program.uid())
                                    .byEventDate().inDatePeriods(dateFilter)
                                    .byState().`in`(statesFilter)
                                    .blockingCount()
                            } else {
                                d2.eventModule().events()
                                    .byProgramUid().eq(program.uid())
                                    .byEventDate().inDatePeriods(dateFilter)
                                    .blockingCount()
                            }
                        }
                    } else if (orgUnitFilter.isNotEmpty()) {
                        count = if (statesFilter.isNotEmpty()) {
                            d2.eventModule().events()
                                .byProgramUid().eq(program.uid())
                                .byOrganisationUnitUid().`in`(orgUnitFilter)
                                .byState().`in`(statesFilter)
                                .blockingCount()
                        } else {
                            d2.eventModule().events()
                                .byProgramUid().eq(program.uid())
                                .byOrganisationUnitUid().`in`(orgUnitFilter)
                                .blockingCount()
                        }
                    } else {
                        count = if (statesFilter.isNotEmpty()) {
                            d2.eventModule().events()
                                .byProgramUid().eq(program.uid())
                                .byState().`in`(statesFilter)
                                .blockingCount()
                        } else {
                            d2.eventModule().events()
                                .byProgramUid().eq(program.uid())
                                .blockingCount()
                        }
                    }

                    if (
                        d2.eventModule().events().byProgramUid().eq(program.uid()).byState().`in`(
                            State.ERROR,
                            State.WARNING
                        ).blockingGet().isNotEmpty()
                    ) {
                        state = State.WARNING
                    } else if (
                        d2.eventModule().events()
                            .byProgramUid().eq(program.uid()).byState().`in`(
                                State.SENT_VIA_SMS,
                                State.SYNCED_VIA_SMS
                            ).blockingGet().isNotEmpty()
                    ) {
                        state = State.SENT_VIA_SMS
                    } else if (
                        d2.eventModule().events()
                            .byProgramUid().eq(program.uid()).byState().`in`(
                                State.TO_UPDATE,
                                State.TO_POST
                            )
                            .blockingGet().isNotEmpty() ||
                        d2.eventModule().events().byProgramUid().eq(program.uid())
                            .byDeleted().isTrue.blockingGet().isNotEmpty()
                    ) {
                        state = State.TO_UPDATE
                    }
                } else {
                    val programUids = ArrayList<String>()
                    programUids.add(program.uid())
                    if (dateFilter.isNotEmpty()) {
                        val enrollments: List<Enrollment>
                        if (orgUnitFilter.isNotEmpty()) {
                            if (statesFilter.isNotEmpty()) {
                                enrollments = d2.enrollmentModule().enrollments()
                                    .byProgram().`in`(programUids)
                                    .byEnrollmentDate().inDatePeriods(dateFilter)
                                    .byOrganisationUnit().`in`(orgUnitFilter)
                                    .byStatus().eq(EnrollmentStatus.ACTIVE)
                                    .byDeleted().isFalse
                                    .byState().`in`(statesFilter)
                                    .blockingGet()
                            } else {
                                enrollments = d2.enrollmentModule().enrollments()
                                    .byProgram().`in`(programUids)
                                    .byEnrollmentDate().inDatePeriods(dateFilter)
                                    .byOrganisationUnit().`in`(orgUnitFilter)
                                    .byStatus().eq(EnrollmentStatus.ACTIVE)
                                    .byDeleted().isFalse
                                    .blockingGet()
                            }
                        } else {
                            enrollments = if (statesFilter.isNotEmpty()) {
                                d2.enrollmentModule().enrollments()
                                    .byProgram().`in`(programUids)
                                    .byEnrollmentDate().inDatePeriods(dateFilter)
                                    .byStatus().eq(EnrollmentStatus.ACTIVE)
                                    .byDeleted().isFalse
                                    .byState().`in`(statesFilter)
                                    .blockingGet()
                            } else {
                                d2.enrollmentModule().enrollments()
                                    .byProgram().`in`(programUids)
                                    .byEnrollmentDate().inDatePeriods(dateFilter)
                                    .byStatus().eq(EnrollmentStatus.ACTIVE)
                                    .byDeleted().isFalse
                                    .blockingGet()
                            }
                        }
                        count = countEnrollment(enrollments)
                    } else if (orgUnitFilter.isNotEmpty()) {
                        val enrollments = if (statesFilter.isNotEmpty()) {
                            d2.enrollmentModule().enrollments()
                                .byProgram().`in`(programUids)
                                .byOrganisationUnit().`in`(orgUnitFilter)
                                .byStatus().eq(EnrollmentStatus.ACTIVE)
                                .byDeleted().isFalse
                                .byState().`in`(statesFilter)
                                .blockingGet()
                        } else {
                            d2.enrollmentModule().enrollments()
                                .byProgram().`in`(programUids)
                                .byOrganisationUnit().`in`(orgUnitFilter)
                                .byStatus().eq(EnrollmentStatus.ACTIVE)
                                .byDeleted().isFalse
                                .blockingGet()
                        }

                        count = countEnrollment(enrollments)
                    } else {
                        val enrollments = if (statesFilter.isNotEmpty()) {
                            d2.enrollmentModule().enrollments()
                                .byProgram().`in`(programUids)
                                .byStatus().eq(EnrollmentStatus.ACTIVE)
                                .byDeleted().isFalse
                                .byState().`in`(statesFilter)
                                .blockingGet()
                        } else {
                            d2.enrollmentModule().enrollments()
                                .byProgram().`in`(programUids)
                                .byStatus().eq(EnrollmentStatus.ACTIVE)
                                .byDeleted().isFalse
                                .blockingGet()
                        }

                        count = countEnrollment(enrollments)
                    }

                    if (d2.trackedEntityModule().trackedEntityInstances()
                            .byProgramUids(programUids).byState().`in`(State.ERROR, State.WARNING)
                            .blockingGet().isNotEmpty()
                    ) {
                        state = State.WARNING
                    } else if (d2.trackedEntityModule().trackedEntityInstances().byProgramUids(
                            programUids
                        ).byState().`in`(
                            State.SENT_VIA_SMS,
                            State.SYNCED_VIA_SMS
                        ).blockingGet().isNotEmpty()
                    ) {
                        state = State.SENT_VIA_SMS
                    } else if (d2.trackedEntityModule().trackedEntityInstances().byProgramUids(
                            programUids
                        ).byState().`in`(
                            State.TO_UPDATE,
                            State.TO_POST
                        ).blockingGet().isNotEmpty() ||
                        d2.trackedEntityModule().trackedEntityInstances().byProgramUids(programUids)
                            .byDeleted().isTrue.blockingGet().isNotEmpty()
                    ) {
                        state = State.TO_UPDATE
                    }
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
                    typeName!!,
                    program.programType()!!.name,
                    program.displayDescription(),
                    onlyEnrollOnce = true,
                    accessDataWrite = true,
                    state = state.name
                )
            }.toList().toFlowable()
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

    private fun countEnrollment(enrollments: List<Enrollment>): Int {
        val teiUids = ArrayList<String>()
        for (enrollment in enrollments) {
            if (!teiUids.contains(enrollment.trackedEntityInstance())) {
                teiUids.add(enrollment.trackedEntityInstance()!!)
            }
        }
        return teiUids.size
    }
}
