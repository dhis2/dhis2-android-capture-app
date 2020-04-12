package org.dhis2.usescases.teiDashboard.teiProgramList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;

import androidx.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.usescases.main.program.ProgramViewModel;
import org.dhis2.utils.CodeGenerator;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;

/**
 * QUADRAM. Created by ppajuelo on 02/11/2017.
 */

public class TeiProgramListRepositoryImpl implements TeiProgramListRepository {

    private final BriteDatabase briteDatabase;
    private final CodeGenerator codeGenerator;
    private final D2 d2;

    TeiProgramListRepositoryImpl(CodeGenerator codeGenerator, BriteDatabase briteDatabase, D2 d2) {
        this.briteDatabase = briteDatabase;
        this.codeGenerator = codeGenerator;
        this.d2 = d2;
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
                            program.style() != null ? program.style().color() : null,
                            program.style() != null ? program.style().icon() : null,
                            program.displayName(),
                            orgUnit.displayName(),
                            enrollment.followUp()!=null?enrollment.followUp():false,
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
                            program.style() != null ? program.style().color() : null,
                            program.style() != null ? program.style().icon() : null,
                            program.displayName(),
                            orgUnit.displayName(),
                            enrollment.followUp()!=null?enrollment.followUp():false,
                            program.uid()
                    );
                })
                .toList()
                .toObservable();
    }

    @NonNull
    @Override
    public Observable<List<ProgramViewModel>> allPrograms(String trackedEntityId) {
        String trackedEntityType = d2.trackedEntityModule().trackedEntityInstances().byUid().eq(trackedEntityId).one().blockingGet().trackedEntityType();
        return Observable.just(d2.organisationUnitModule().organisationUnits().byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE).blockingGet())
                .map(captureOrgUnits -> {
                    Iterator<OrganisationUnit> it = captureOrgUnits.iterator();
                    List<String> captureOrgUnitUids = new ArrayList();
                    while (it.hasNext()) {
                        OrganisationUnit ou = it.next();
                        captureOrgUnitUids.add(ou.uid());
                    }
                    return captureOrgUnitUids;
                })
                .flatMap(orgUnits->Observable.fromCallable(() -> d2.programModule().programs()
                        .byOrganisationUnitList(orgUnits)
                        .byTrackedEntityTypeUid().eq(trackedEntityType).blockingGet()))
                .flatMapIterable(programs -> programs)
                .map(program -> ProgramViewModel.Companion.create(
                        program.uid(),
                        program.displayName(),
                        program.style() != null ? program.style().color() : null,
                        program.style() != null ? program.style().icon() : null,
                        0,
                        program.trackedEntityType().name(),
                        "",
                        program.programType().name(),
                        program.displayDescription(),
                        program.onlyEnrollOnce(),
                        program.access().data().write(),
                        State.SYNCED.name(),
                        false
                ))
                .toList()
                .toObservable();
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
        Date currentDate = Calendar.getInstance().getTime();
        return Observable.defer(() -> {

            ContentValues dataValue = new ContentValues();

            // renderSearchResults time stamp
            dataValue.put("lastUpdated",
                    BaseIdentifiableObject.DATE_FORMAT.format(currentDate));
            dataValue.put("state",
                    State.TO_POST.toString());

            if (briteDatabase.update("TrackedEntityInstance", dataValue,
                    "uid = ? ", teiUid == null ? "" : teiUid) <= 0) {
                String message = String.format(Locale.US, "Failed to update tracked entity " +
                                "instance for uid=[%s]",
                        teiUid);
                return Observable.error(new SQLiteConstraintException(message));
            }

            Enrollment enrollmentModel = Enrollment.builder()
                    .uid(codeGenerator.generate())
                    .created(currentDate)
                    .lastUpdated(currentDate)
                    .enrollmentDate(enrollmentDate)
                    .program(programUid)
                    .organisationUnit(orgUnit)
                    .trackedEntityInstance(teiUid)
                    .status(EnrollmentStatus.ACTIVE)
                    .followUp(false)
                    .state(State.TO_POST)
                    .build();

            if (briteDatabase.insert("Enrollment", enrollmentModel.toContentValues()) < 0) {
                String message = String.format(Locale.US, "Failed to insert new enrollment " +
                        "instance for organisationUnit=[%s] and program=[%s]", orgUnit, programUid);
                return Observable.error(new SQLiteConstraintException(message));
            }

            return Observable.just(enrollmentModel.uid());
        });
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
}