package org.dhis2.usescases.teiDashboard.teiProgramList;

import android.content.ContentValues;
import android.database.sqlite.SQLiteConstraintException;

import androidx.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.usescases.main.program.ProgramViewModel;
import org.dhis2.utils.CodeGenerator;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.ArrayList;
import java.util.Calendar;
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
        return Observable.fromCallable(() -> d2.enrollmentModule().enrollments.byTrackedEntityInstance().eq(trackedEntityId).byStatus().eq(EnrollmentStatus.ACTIVE).withAllChildren().get())
                .flatMapIterable(enrollments -> enrollments)
                .map(enrollment -> {
                    Program program = d2.programModule().programs.byUid().eq(enrollment.program()).withStyle().one().get();
                    OrganisationUnit orgUnit = d2.organisationUnitModule().organisationUnits.byUid().eq(enrollment.organisationUnit()).one().get();
                    return EnrollmentViewModel.create(
                            enrollment.uid(),
                            DateUtils.getInstance().formatDate(enrollment.enrollmentDate()),
                            program.style() != null ? program.style().color() : null,
                            program.style() != null ? program.style().icon() : null,
                            program.displayName(),
                            orgUnit.displayName(),
                            enrollment.followUp(),
                            program.uid()
                    );
                })
                .toList()
                .toObservable();
    }

    @NonNull
    @Override
    public Observable<List<EnrollmentViewModel>> otherEnrollments(String trackedEntityId) {
        return Observable.fromCallable(() -> d2.enrollmentModule().enrollments.byTrackedEntityInstance().eq(trackedEntityId).byStatus().neq(EnrollmentStatus.ACTIVE).withAllChildren().get())
                .flatMapIterable(enrollments -> enrollments)
                .map(enrollment -> {
                    Program program = d2.programModule().programs.byUid().eq(enrollment.program()).withStyle().one().get();
                    OrganisationUnit orgUnit = d2.organisationUnitModule().organisationUnits.byUid().eq(enrollment.organisationUnit()).one().get();
                    return EnrollmentViewModel.create(
                            enrollment.uid(),
                            DateUtils.getInstance().formatDate(enrollment.enrollmentDate()),
                            program.style() != null ? program.style().color() : null,
                            program.style() != null ? program.style().icon() : null,
                            program.displayName(),
                            orgUnit.displayName(),
                            enrollment.followUp(),
                            program.uid()
                    );
                })
                .toList()
                .toObservable();
    }

    @NonNull
    @Override
    public Observable<List<ProgramViewModel>> allPrograms(String trackedEntityId) {
        String trackedEntityType = d2.trackedEntityModule().trackedEntityInstances.byUid().eq(trackedEntityId).one().get().trackedEntityType();
        return Observable.just(d2.organisationUnitModule().organisationUnits.byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE).get())
                .map(captureOrgUnits -> {
                    Iterator<OrganisationUnit> it = captureOrgUnits.iterator();
                    List<String> captureOrgUnitUids = new ArrayList();
                    while (it.hasNext()) {
                        OrganisationUnit ou = it.next();
                        captureOrgUnitUids.add(ou.uid());
                    }
                    return captureOrgUnitUids;
                })
                .flatMap(orgUnits->Observable.fromCallable(() -> d2.programModule().programs
                        .byOrganisationUnitList(orgUnits)
                        .byTrackedEntityTypeUid().eq(trackedEntityType).withStyle().get()))
                .flatMapIterable(programs -> programs)
                .map(program -> ProgramViewModel.create(
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
                        program.access().data().write()
                ))
                .toList()
                .toObservable();
    }

    @NonNull
    @Override
    public Observable<List<Program>> alreadyEnrolledPrograms(String trackedEntityId) {
        return Observable.fromCallable(() -> d2.enrollmentModule().enrollments.byTrackedEntityInstance().eq(trackedEntityId).withAllChildren().get())
                .flatMapIterable(enrollments -> enrollments)
                .map(enrollment -> d2.programModule().programs.byUid().eq(enrollment.program()).one().get())
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
            dataValue.put(TrackedEntityInstanceModel.Columns.LAST_UPDATED,
                    BaseIdentifiableObject.DATE_FORMAT.format(currentDate));
            dataValue.put(TrackedEntityInstanceModel.Columns.STATE,
                    State.TO_POST.toString());

            if (briteDatabase.update(TrackedEntityInstanceModel.TABLE, dataValue,
                    TrackedEntityInstanceModel.Columns.UID + " = ? ", teiUid == null ? "" : teiUid) <= 0) {
                String message = String.format(Locale.US, "Failed to update tracked entity " +
                                "instance for uid=[%s]",
                        teiUid);
                return Observable.error(new SQLiteConstraintException(message));
            }

            EnrollmentModel enrollmentModel = EnrollmentModel.builder()
                    .uid(codeGenerator.generate())
                    .created(currentDate)
                    .lastUpdated(currentDate)
                    .enrollmentDate(enrollmentDate)
                    .program(programUid)
                    .organisationUnit(orgUnit)
                    .trackedEntityInstance(teiUid)
                    .enrollmentStatus(EnrollmentStatus.ACTIVE)
                    .followUp(false)
                    .state(State.TO_POST)
                    .build();

            if (briteDatabase.insert(EnrollmentModel.TABLE, enrollmentModel.toContentValues()) < 0) {
                String message = String.format(Locale.US, "Failed to insert new enrollment " +
                        "instance for organisationUnit=[%s] and program=[%s]", orgUnit, programUid);
                return Observable.error(new SQLiteConstraintException(message));
            }

            return Observable.just(enrollmentModel.uid());
        });
    }

    @Override
    public Observable<List<OrganisationUnit>> getOrgUnits(String programUid) {
        if (programUid != null) {
            return Observable.just(d2.organisationUnitModule().organisationUnits.byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE).withPrograms().get())
                    .flatMapIterable(organisationUnits -> organisationUnits)
                    .filter(organisationUnit -> {
                        boolean result = false;
                        for (Program program : organisationUnit.programs()) {
                            if (program.uid().equals(programUid))
                                result = true;
                        }
                        return result;
                    })
                    .toList()
                    .toObservable();
        } else
            return Observable.just(d2.organisationUnitModule().organisationUnits.byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE).get());
    }

    @Override
    public String getProgramColor(@NonNull String programUid) {
        Program program = d2.programModule().programs.byUid().eq(programUid).withStyle().one().get();
        return program.style() != null ? program.style().color() : null;
    }

    @Override
    public Program getProgram(String programUid) {
        Program program = d2.programModule().programs.byUid().eq(programUid).one().get();
        return program;
    }
}