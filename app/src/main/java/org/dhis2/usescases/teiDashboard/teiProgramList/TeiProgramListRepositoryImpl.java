package org.dhis2.usescases.teiDashboard.teiProgramList;

import androidx.annotation.NonNull;

import org.dhis2.R;
import org.dhis2.commons.resources.MetadataIconProvider;
import org.dhis2.commons.resources.ResourceManager;
import org.dhis2.usescases.main.program.ProgramDownloadState;
import org.dhis2.usescases.main.program.ProgramViewModel;
import org.dhis2.usescases.main.program.ProgramViewModelMapper;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.enrollment.EnrollmentCreateProjection;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;

public class TeiProgramListRepositoryImpl implements TeiProgramListRepository {

    private final D2 d2;
    private final ProgramViewModelMapper programViewModelMapper;
    private final MetadataIconProvider metadataIconProvider;

    TeiProgramListRepositoryImpl(D2 d2, ProgramViewModelMapper programViewModelMapper, MetadataIconProvider metadataIconProvider) {
        this.d2 = d2;
        this.programViewModelMapper = programViewModelMapper;
        this.metadataIconProvider = metadataIconProvider;
    }

    @NonNull
    @Override
    public Observable<List<EnrollmentViewModel>> activeEnrollments(String trackedEntityId) {
        return Observable.fromCallable(() ->
                        d2.enrollmentModule().enrollments()
                                .byTrackedEntityInstance().eq(trackedEntityId)
                                .byStatus().eq(EnrollmentStatus.ACTIVE)
                                .byDeleted().eq(false).blockingGet())
                .flatMapIterable(enrollments -> enrollments)
                .map(enrollment -> {
                    Program program = d2.programModule().programs().byUid().eq(enrollment.program()).one().blockingGet();
                    OrganisationUnit orgUnit = d2.organisationUnitModule().organisationUnits().byUid().eq(enrollment.organisationUnit()).one().blockingGet();
                    return EnrollmentViewModel.create(
                            enrollment.uid(),
                            DateUtils.getInstance().formatDate(enrollment.enrollmentDate()),
                            metadataIconProvider.invoke(program.style()),
                            program.displayName(),
                            orgUnit.displayName(),
                            enrollment.followUp() != null ? enrollment.followUp() : false,
                            program.uid()
                    );
                })
                .toList()
                .toObservable();
    }

    @NonNull
    @Override
    public Observable<List<EnrollmentViewModel>> otherEnrollments(String trackedEntityId) {
        return Observable.fromCallable(() -> d2.enrollmentModule().enrollments().byTrackedEntityInstance().eq(trackedEntityId).byStatus().neq(EnrollmentStatus.ACTIVE).blockingGet())
                .flatMapIterable(enrollments -> enrollments)
                .map(enrollment -> {
                    Program program = d2.programModule().programs().byUid().eq(enrollment.program()).one().blockingGet();
                    OrganisationUnit orgUnit = d2.organisationUnitModule().organisationUnits().byUid().eq(enrollment.organisationUnit()).one().blockingGet();
                    return EnrollmentViewModel.create(
                            enrollment.uid(),
                            DateUtils.getInstance().formatDate(enrollment.enrollmentDate()),
                            metadataIconProvider.invoke(program.style()),
                            program.displayName(),
                            orgUnit.displayName(),
                            enrollment.followUp() != null ? enrollment.followUp() : false,
                            program.uid()
                    );
                })
                .toList()
                .toObservable();
    }

    @NonNull
    @Override
    public Flowable<List<ProgramViewModel>> allPrograms(String trackedEntityId) {
        String trackedEntityType = d2.trackedEntityModule().trackedEntityInstances().byUid().eq(trackedEntityId).one().blockingGet().trackedEntityType();
        return Flowable.just(d2.organisationUnitModule().organisationUnits().byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE).blockingGet())
                .map(captureOrgUnits -> {
                    Iterator<OrganisationUnit> it = captureOrgUnits.iterator();
                    List<String> captureOrgUnitUids = new ArrayList();
                    while (it.hasNext()) {
                        OrganisationUnit ou = it.next();
                        captureOrgUnitUids.add(ou.uid());
                    }
                    return captureOrgUnitUids;
                })
                .flatMap(orgUnits -> Flowable.fromCallable(() -> d2.programModule().programs()
                        .byOrganisationUnitList(orgUnits)
                        .byTrackedEntityTypeUid().eq(trackedEntityType).blockingGet()))
                .flatMapIterable(programs -> programs)
                .map(program ->
                        programViewModelMapper.map(
                                program,
                                0,
                                "",
                                State.SYNCED,
                                false,
                                false,
                                metadataIconProvider.invoke(program.style())
                        )
                )
                .toList()
                .toFlowable();
    }

    @NonNull
    @Override
    public Observable<List<Program>> alreadyEnrolledPrograms(String trackedEntityId) {
        return Observable.fromCallable(() ->
                        d2.enrollmentModule().enrollments()
                                .byTrackedEntityInstance().eq(trackedEntityId)
                                .byDeleted().eq(false).blockingGet())
                .flatMapIterable(enrollments -> enrollments)
                .map(enrollment -> d2.programModule().programs().byUid().eq(enrollment.program()).one().blockingGet())
                .toList()
                .toObservable();
    }

    @NonNull
    @Override
    public Observable<String> saveToEnroll(@NonNull String orgUnit, @NonNull String programUid, @NonNull String teiUid, Date enrollmentDate) {
        return d2.enrollmentModule().enrollments().add(
                        EnrollmentCreateProjection.builder()
                                .organisationUnit(orgUnit)
                                .program(programUid)
                                .trackedEntityInstance(teiUid)
                                .build())
                .map(enrollmentUid ->
                        d2.enrollmentModule().enrollments().uid(enrollmentUid))
                .map(enrollmentRepository -> {
                    if (d2.programModule().programs().uid(programUid).blockingGet().displayIncidentDate()) {
                        enrollmentRepository.setIncidentDate(DateUtils.getInstance().getToday());
                    }
                    enrollmentRepository.setEnrollmentDate(enrollmentDate);
                    enrollmentRepository.setFollowUp(false);
                    return enrollmentRepository.blockingGet().uid();
                }).toObservable();
    }

    @Override
    public Observable<List<OrganisationUnit>> getOrgUnits(String programUid) {
        if (programUid != null)
            return d2.organisationUnitModule().organisationUnits().byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                    .byProgramUids(Collections.singletonList(programUid)).get().toObservable();
        else
            return d2.organisationUnitModule().organisationUnits().byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE).get().toObservable();
    }

    @Override
    public String getProgramColor(@NonNull String programUid) {
        Program program = d2.programModule().programs().byUid().eq(programUid).one().blockingGet();
        return program.style() != null ? program.style().color() : null;
    }

    @Override
    public Program getProgram(String programUid) {
        Program program = d2.programModule().programs().byUid().eq(programUid).one().blockingGet();
        return program;
    }

    @Override
    public ProgramViewModel updateProgramViewModel(ProgramViewModel programViewModel, ProgramDownloadState programDownloadState) {
        return programViewModelMapper.map(programViewModel, programDownloadState);
    }
}