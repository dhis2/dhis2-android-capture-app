package org.dhis2.usescases.main.program;

import androidx.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.period.DatePeriod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;

import static org.hisp.dhis.android.core.program.ProgramType.WITHOUT_REGISTRATION;
import static org.hisp.dhis.android.core.program.ProgramType.WITH_REGISTRATION;

class HomeRepositoryImpl implements HomeRepository {


    private final D2 d2;
    private final String eventLabel;

    HomeRepositoryImpl(D2 d2,String eventLabel) {
        this.d2 = d2;
        this.eventLabel = eventLabel;
    }

    @NonNull
    @Override
    public Flowable<List<ProgramViewModel>> programModels(List<DatePeriod> dateFilter, List<String> orgUnitFilter) {

        return Flowable.just(d2.organisationUnitModule().organisationUnits.byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE).get())
                .map(captureOrgUnits -> {
                    Iterator<OrganisationUnit> it = captureOrgUnits.iterator();
                    List<String> captureOrgUnitUids = new ArrayList();
                    while (it.hasNext()) {
                        OrganisationUnit ou = it.next();
                        captureOrgUnitUids.add(ou.uid());
                    }
                    return captureOrgUnitUids;
                })
                .flatMap(orgUnits -> Flowable.just(d2.programModule().programs.byOrganisationUnitList(orgUnits)))
                .flatMap(programRepo -> {
                    if (orgUnitFilter != null && !orgUnitFilter.isEmpty())
                        return Flowable.fromIterable(programRepo.byOrganisationUnitList(orgUnitFilter).withStyle().withAllChildren().get());
                    else
                        return Flowable.fromIterable(programRepo.withStyle().withAllChildren().get());
                })
                .map(program -> {

                    String typeName;
                    if (program.programType() == WITH_REGISTRATION) {
                        typeName = program.trackedEntityType() != null ? program.trackedEntityType().displayName() : "TEI";
                        if (typeName == null)
                            typeName = d2.trackedEntityModule().trackedEntityTypes.uid(program.trackedEntityType().uid()).get().displayName();
                    } else if (program.programType() == WITHOUT_REGISTRATION)
                        typeName = eventLabel;
                    else
                        typeName = "DataSets";

                    int count;
                    State state = State.SYNCED;
                    if (program.programType() == WITHOUT_REGISTRATION) {
                        if (!dateFilter.isEmpty()) {
                            if (!orgUnitFilter.isEmpty()) {
                                count = d2.eventModule().events
                                        .byProgramUid().eq(program.uid())
                                        .byEventDate().inDatePeriods(dateFilter)
                                        .byOrganisationUnitUid().in(orgUnitFilter)
                                        .byState().notIn(State.TO_DELETE)
                                        .count();
                            } else {
                                count = d2.eventModule().events
                                        .byProgramUid().eq(program.uid())
                                        .byEventDate().inDatePeriods(dateFilter)
                                        .byState().notIn(State.TO_DELETE)
                                        .count();
                            }
                        } else if (!orgUnitFilter.isEmpty()) {
                            count = d2.eventModule().events
                                    .byProgramUid().eq(program.uid())
                                    .byOrganisationUnitUid().in(orgUnitFilter)
                                    .byState().notIn(State.TO_DELETE)
                                    .count();
                        } else {
                            count = d2.eventModule().events
                                    .byProgramUid().eq(program.uid())
                                    .byState().notIn(State.TO_DELETE)
                                    .count();
                        }

                        if (!d2.eventModule().events.byProgramUid().eq(program.uid()).byState().in(State.ERROR, State.WARNING).get().isEmpty())
                            state = State.WARNING;
                        else if (!d2.eventModule().events.byProgramUid().eq(program.uid()).byState().in(State.SENT_VIA_SMS, State.SYNCED_VIA_SMS).get().isEmpty())
                            state = State.SENT_VIA_SMS;
                        else if (!d2.eventModule().events.byProgramUid().eq(program.uid()).byState().in(State.TO_UPDATE, State.TO_POST, State.TO_DELETE).get().isEmpty())
                            state = State.TO_UPDATE;

                    } else {
                        List<String> programUids = new ArrayList<>();
                        programUids.add(program.uid());
                        if (!dateFilter.isEmpty()) {
                            List<Enrollment> enrollments;
                            if (!orgUnitFilter.isEmpty()) {
                                enrollments = d2.enrollmentModule().enrollments
                                        .byProgram().in(programUids)
                                        .byEnrollmentDate().inDatePeriods(dateFilter)
                                        .byOrganisationUnit().in(orgUnitFilter)
                                        .byStatus().eq(EnrollmentStatus.ACTIVE)
                                        .byState().notIn(State.TO_DELETE)
                                        .get();
                            } else {
                                enrollments = d2.enrollmentModule().enrollments
                                        .byProgram().in(programUids)
                                        .byEnrollmentDate().inDatePeriods(dateFilter)
                                        .byStatus().eq(EnrollmentStatus.ACTIVE)
                                        .byState().notIn(State.TO_DELETE)
                                        .get();
                            }
                            count = countEnrollment(enrollments);
                        } else if (!orgUnitFilter.isEmpty()) {
                            List<Enrollment> enrollments = d2.enrollmentModule().enrollments
                                    .byProgram().in(programUids)
                                    .byOrganisationUnit().in(orgUnitFilter)
                                    .byStatus().eq(EnrollmentStatus.ACTIVE)
                                    .byState().notIn(State.TO_DELETE)
                                    .get();
                            count = countEnrollment(enrollments);
                        } else {
                            List<Enrollment> enrollments = d2.enrollmentModule().enrollments
                                    .byProgram().in(programUids)
                                    .byStatus().eq(EnrollmentStatus.ACTIVE)
                                    .byState().notIn(State.TO_DELETE)
                                    .get();
                            count = countEnrollment(enrollments);
                        }

                        if (!d2.trackedEntityModule().trackedEntityInstances.byProgramUids(programUids).byState().in(State.ERROR, State.WARNING).get().isEmpty())
                            state = State.WARNING;
                        else if (!d2.trackedEntityModule().trackedEntityInstances.byProgramUids(programUids).byState().in(State.SENT_VIA_SMS, State.SYNCED_VIA_SMS).get().isEmpty())
                            state = State.SENT_VIA_SMS;
                        else if (!d2.trackedEntityModule().trackedEntityInstances.byProgramUids(programUids).byState().in(State.TO_UPDATE, State.TO_POST, State.TO_DELETE).get().isEmpty())
                            state = State.TO_UPDATE;
                    }


                    return ProgramViewModel.create(
                            program.uid(),
                            program.displayName(),
                            program.style() != null ? program.style().color() : null,
                            program.style() != null ? program.style().icon() : null,
                            count,
                            program.trackedEntityType() != null ? program.trackedEntityType().uid() : null,
                            typeName,
                            program.programType() != null ? program.programType().name() : null,
                            program.displayDescription(),
                            true,
                            true,
                            state.name()
                    );
                }).toList().toFlowable();
    }

    private int countEnrollment(List<Enrollment> enrollments) {
        List<String> teiUids = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            if (!teiUids.contains(enrollment.trackedEntityInstance()))
                teiUids.add(enrollment.trackedEntityInstance());

        }
        return teiUids.size();
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnit>> orgUnits(String parentUid) {
        return Observable.defer(() -> Observable.just(
                d2.organisationUnitModule().organisationUnits
                        .byParentUid().eq(parentUid)
                        .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                        .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                        .get()
        ));
    }


    @NonNull
    @Override
    public Observable<List<OrganisationUnit>> orgUnits() {
        return Observable.defer(() -> Observable.just(
                d2.organisationUnitModule().organisationUnits
                        .byRootOrganisationUnit(true)
                        .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                        .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                        .get()
        ));
    }
}
