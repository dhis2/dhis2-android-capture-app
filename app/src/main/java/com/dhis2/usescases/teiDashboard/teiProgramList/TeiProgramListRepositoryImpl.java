package com.dhis2.usescases.teiDashboard.teiProgramList;

import android.content.ContentValues;
import android.database.sqlite.SQLiteConstraintException;
import android.support.annotation.NonNull;

import com.dhis2.data.tuples.Trio;
import com.dhis2.utils.CodeGenerator;
import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.ObjectStyleModel;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitProgramLinkModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import io.reactivex.Observable;

/**
 * Created by ppajuelo on 02/11/2017.
 */

public class TeiProgramListRepositoryImpl implements TeiProgramListRepository {

    private final BriteDatabase briteDatabase;
    private final CodeGenerator codeGenerator;

    TeiProgramListRepositoryImpl(CodeGenerator codeGenerator, BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
        this.codeGenerator = codeGenerator;
    }

    @NonNull
    @Override
    public Observable<List<EnrollmentViewModel>> activeEnrollments(String trackedEntityId) {
        String SELECT_ACTIVE_ENROLLMENTS_WITH_TEI_ID = "SELECT * FROM " + EnrollmentModel.TABLE +
                " WHERE " + EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE + "='%s' AND " + EnrollmentModel.Columns.ENROLLMENT_STATUS + "='ACTIVE'";
        String SELECT_ACTIVE_ENROLLMENT_WITH_TEI_ID = "SELECT " +
                "Enrollment.uid," +
                "Enrollment.enrollmentDate," +
                "Enrollment.followup," +
                "ObjectStyle.icon," +
                "ObjectStyle.color," +
                "Program.displayName AS programName," +
                "Program.uid AS programUid," +
                "OrganisationUnit.displayName AS OrgUnitName FROM Enrollment " +
                "LEFT JOIN ObjectStyle ON ObjectStyle.uid = Enrollment.program " +
                "JOIN Program ON Program.uid = Enrollment.program " +
                "JOIN OrganisationUnit ON OrganisationUnit.uid = Enrollment.organisationUnit " +
                "WHERE Enrollment.trackedEntityInstance = ? AND Enrollment.status = 'ACTIVE'";
       String[] TABLE_NAMES = new String[]{ProgramModel.TABLE, ObjectStyleModel.TABLE, OrganisationUnitProgramLinkModel.TABLE};
       Set<String> TABLE_SET = new HashSet<>(Arrays.asList(TABLE_NAMES));
       return briteDatabase.createQuery(TABLE_SET,SELECT_ACTIVE_ENROLLMENT_WITH_TEI_ID,trackedEntityId)
                .mapToList(EnrollmentViewModel::fromCursor);
       /* return briteDatabase.createQuery(EnrollmentModel.TABLE, String.format(SELECT_ACTIVE_ENROLLMENTS_WITH_TEI_ID, trackedEntityId))
                .mapToList(EnrollmentModel::create)
                .flatMapIterable(data -> data)
                .flatMap(enrollmentModel -> briteDatabase.createQuery(ObjectStyleModel.TABLE, "SELECT * FROM ObjectStyle WHERE uid = ?", enrollmentModel.program())
                        .mapToOneOrDefault(ObjectStyleModel::create, ObjectStyleModel.builder().color("").icon("").build())
                        .map(objectStyle -> Trio.create(enrollmentModel, objectStyle.icon(), objectStyle.color())))
                .toList()
                .flatMapObservable(Observable::just);*/
    }

    @NonNull
    @Override
    public Observable<List<EnrollmentViewModel>> otherEnrollments(String trackedEntityId) {
        String SELECT_ENROLLMENTS_WITH_TEI_ID = "SELECT * FROM " + EnrollmentModel.TABLE + " WHERE " + EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE + "='%s' AND " + EnrollmentModel.Columns.ENROLLMENT_STATUS + "!='ACTIVE'";
        String SELECT_ACTIVE_ENROLLMENT_WITH_TEI_ID = "SELECT " +
                "Enrollment.uid," +
                "Enrollment.enrollmentDate," +
                "Enrollment.followup," +
                "ObjectStyle.icon," +
                "ObjectStyle.color," +
                "Program.displayName AS programName," +
                "Program.uid AS programUid," +
                "OrganisationUnit.displayName AS OrgUnitName FROM Enrollment " +
                "LEFT JOIN ObjectStyle ON ObjectStyle.uid = Enrollment.program " +
                "JOIN Program ON Program.uid = Enrollment.program " +
                "JOIN OrganisationUnit ON OrganisationUnit.uid = Enrollment.organisationUnit " +
                "WHERE Enrollment.trackedEntityInstance = ? AND Enrollment.status != 'ACTIVE'";
        String[] TABLE_NAMES = new String[]{ProgramModel.TABLE, ObjectStyleModel.TABLE, OrganisationUnitProgramLinkModel.TABLE};
        Set<String> TABLE_SET = new HashSet<>(Arrays.asList(TABLE_NAMES));
        return briteDatabase.createQuery(TABLE_SET,SELECT_ACTIVE_ENROLLMENT_WITH_TEI_ID,trackedEntityId)
                .mapToList(EnrollmentViewModel::fromCursor);
       /* return briteDatabase.createQuery(EnrollmentModel.TABLE, String.format(SELECT_ENROLLMENTS_WITH_TEI_ID, trackedEntityId))
                .mapToList(EnrollmentModel::create);*/
    }

    @NonNull
    @Override
    public Observable<List<ProgramModel>> allPrograms(String trackedEntityId) {
        String SELECT_PROGRAMS_WITH_TEI_ID = "SELECT " + ProgramModel.TABLE + ".* FROM " + ProgramModel.TABLE + " JOIN " + TrackedEntityInstanceModel.TABLE + " WHERE " + ProgramModel.TABLE + "." + ProgramModel.Columns.UID +
                " NOT IN (SELECT " + EnrollmentModel.TABLE + "." + EnrollmentModel.Columns.PROGRAM + " FROM " + EnrollmentModel.TABLE + " WHERE " + EnrollmentModel.TABLE + "." + EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE + "='%s')" +
                " AND " + ProgramModel.TABLE + "." + ProgramModel.Columns.TRACKED_ENTITY_TYPE + "=" + TrackedEntityInstanceModel.TABLE + "." + TrackedEntityInstanceModel.Columns.TRACKED_ENTITY_TYPE +
                " AND " + TrackedEntityInstanceModel.TABLE + "." + TrackedEntityInstanceModel.Columns.UID + "='%s'";

        String SELECT_PROGRAMS_FOR_TEI = "SELECT Program.* FROM Program " +
                "JOIN TrackedEntityInstance ON TrackedEntityInstance.trackedEntityType = Program.trackedEntityType " +
                "WHERE TrackedEntityInstance.uid = ? GROUP BY Program.uid";
        return briteDatabase.createQuery(EnrollmentModel.TABLE, SELECT_PROGRAMS_FOR_TEI, trackedEntityId)
                .mapToList(ProgramModel::create);
    }

    @NonNull
    @Override
    public Observable<List<ProgramModel>> alreadyEnrolledPrograms(String trackedEntityId) {
        String SELECT_ENROLLED_PROGRAMS_WITH_TEI_ID = "SELECT * FROM " + ProgramModel.TABLE + " JOIN " + EnrollmentModel.TABLE +
                " ON " + EnrollmentModel.TABLE + "." + EnrollmentModel.Columns.PROGRAM + "=" + ProgramModel.TABLE + "." + ProgramModel.Columns.UID +
                " WHERE " + EnrollmentModel.TABLE + "." + EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE + "='%s' GROUP BY " + ProgramModel.TABLE + "." + ProgramModel.Columns.UID;
        return briteDatabase.createQuery(EnrollmentModel.TABLE, String.format(SELECT_ENROLLED_PROGRAMS_WITH_TEI_ID, trackedEntityId))
                .mapToList(ProgramModel::create);
    }

    @NonNull
    @Override
    public Observable<String> saveToEnroll(@NonNull String orgUnit, @NonNull String programUid, @NonNull String teiUid) {
        Date currentDate = Calendar.getInstance().getTime();
        return Observable.defer(() -> {

            ContentValues dataValue = new ContentValues();

            // renderSearchResults time stamp
            dataValue.put(TrackedEntityInstanceModel.Columns.LAST_UPDATED,
                    BaseIdentifiableObject.DATE_FORMAT.format(currentDate));
            dataValue.put(TrackedEntityInstanceModel.Columns.STATE,
                    State.TO_POST.toString());

            if (briteDatabase.update(TrackedEntityInstanceModel.TABLE, dataValue,
                    TrackedEntityInstanceModel.Columns.UID + " = ? ", teiUid) <= 0) {
                String message = String.format(Locale.US, "Failed to update tracked entity " +
                                "instance for uid=[%s]",
                        teiUid);
                return Observable.error(new SQLiteConstraintException(message));
            }

            EnrollmentModel enrollmentModel = EnrollmentModel.builder()
                    .uid(codeGenerator.generate())
                    .created(currentDate)
                    .lastUpdated(currentDate)
                    .enrollmentDate(currentDate)
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

            updateProgramTable(currentDate, programUid);

            return Observable.just(enrollmentModel.uid());
        });
    }

    @Override
    public Observable<List<OrganisationUnitModel>> getOrgUnits() {
        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, "SELECT * FROM " + OrganisationUnitModel.TABLE)
                .mapToList(OrganisationUnitModel::create);
    }

    private void updateProgramTable(Date lastUpdated, String programUid) {
        /*ContentValues program = new ContentValues();TODO: Crash if active
        program.put(EnrollmentModel.Columns.LAST_UPDATED, BaseIdentifiableObject.DATE_FORMAT.format(lastUpdated));
        briteDatabase.update(ProgramModel.TABLE, program, ProgramModel.Columns.UID + " = ?", programUid);*/
    }
}