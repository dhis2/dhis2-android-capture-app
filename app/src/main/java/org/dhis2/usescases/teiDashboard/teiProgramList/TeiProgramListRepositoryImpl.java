package org.dhis2.usescases.teiDashboard.teiProgramList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.usescases.main.program.ProgramViewModel;
import org.dhis2.utils.CodeGenerator;
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

import androidx.annotation.NonNull;
import io.reactivex.Observable;

/**
 * QUADRAM. Created by ppajuelo on 02/11/2017.
 */

public class TeiProgramListRepositoryImpl implements TeiProgramListRepository {

    private final BriteDatabase briteDatabase;
    private final CodeGenerator codeGenerator;

    TeiProgramListRepositoryImpl(CodeGenerator codeGenerator, BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
        this.codeGenerator = codeGenerator;
    }

    public final String PROGRAM_COLOR_QUERY = String.format(
            "SELECT %s FROM %S " +
                    "WHERE %s = 'Program' AND %s = ?",
            ObjectStyleModel.Columns.COLOR, ObjectStyleModel.TABLE,
            ObjectStyleModel.Columns.OBJECT_TABLE,
            ObjectStyleModel.Columns.UID
    );

    @NonNull
    @Override
    public Observable<List<EnrollmentViewModel>> activeEnrollments(String trackedEntityId) {
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
        return briteDatabase.createQuery(TABLE_SET, SELECT_ACTIVE_ENROLLMENT_WITH_TEI_ID, trackedEntityId == null ? "" : trackedEntityId)
                .mapToList(EnrollmentViewModel::fromCursor);
    }

    @NonNull
    @Override
    public Observable<List<EnrollmentViewModel>> otherEnrollments(String trackedEntityId) {
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
        return briteDatabase.createQuery(TABLE_SET, SELECT_ACTIVE_ENROLLMENT_WITH_TEI_ID, trackedEntityId == null ? "" : trackedEntityId)
                .mapToList(EnrollmentViewModel::fromCursor);
    }


    private final static String PROGRAM_MODELS_FOR_TEI = "SELECT " +
            "Program.uid, " +
            "Program.displayName, " +
            "ObjectStyle.color, " +
            "ObjectStyle.icon," +
            "Program.programType," +
            "Program.trackedEntityType," +
            "Program.description, " +
            "Program.onlyEnrollOnce, " +
            "Program.accessDataWrite " +
            "FROM Program LEFT JOIN ObjectStyle ON ObjectStyle.uid = Program.uid " +
            "JOIN OrganisationUnitProgramLink ON OrganisationUnitProgramLink.program = Program.uid " +
            "JOIN TrackedEntityInstance ON TrackedEntityInstance.trackedEntityType = Program.trackedEntityType " +
            "WHERE TrackedEntityInstance.uid = ? GROUP BY Program.uid";
    private static final String[] TABLE_NAMES = new String[]{ProgramModel.TABLE, ObjectStyleModel.TABLE, OrganisationUnitProgramLinkModel.TABLE};
    private static final Set<String> TABLE_SET = new HashSet<>(Arrays.asList(TABLE_NAMES));

    @NonNull
    @Override
    public Observable<List<ProgramViewModel>> allPrograms(String trackedEntityId) {
        return briteDatabase.createQuery(TABLE_SET, PROGRAM_MODELS_FOR_TEI, trackedEntityId == null ? "" : trackedEntityId)
                .mapToList(cursor -> {
                    String uid = cursor.getString(0);
                    String displayName = cursor.getString(1);
                    String color = cursor.getString(2);
                    String icon = cursor.getString(3);
                    String programType = cursor.getString(4);
                    String teiType = cursor.getString(5);
                    String description = cursor.getString(6);
                    Boolean onlyEnrollOnce = cursor.getInt(7) == 1;
                    Boolean accessDataWrite = cursor.getInt(8) == 1;

                    return ProgramViewModel.create(uid, displayName, color, icon, 0, teiType, "", programType, description, onlyEnrollOnce, accessDataWrite);
                });
    }

    @NonNull
    @Override
    public Observable<List<ProgramModel>> alreadyEnrolledPrograms(String trackedEntityId) {
        String SELECT_ENROLLED_PROGRAMS_WITH_TEI_ID = "SELECT * FROM " + ProgramModel.TABLE + " JOIN " + EnrollmentModel.TABLE +
                " ON " + EnrollmentModel.TABLE + "." + EnrollmentModel.Columns.PROGRAM + "=" + ProgramModel.TABLE + "." + ProgramModel.Columns.UID +
                " WHERE " + EnrollmentModel.TABLE + "." + EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE + "='%s' GROUP BY " + ProgramModel.TABLE + "." + ProgramModel.Columns.UID;
        return briteDatabase.createQuery(EnrollmentModel.TABLE, String.format(SELECT_ENROLLED_PROGRAMS_WITH_TEI_ID, trackedEntityId == null ? "" : trackedEntityId))
                .mapToList(ProgramModel::create);
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
    public Observable<List<OrganisationUnitModel>> getOrgUnits(String programUid) {

        if (programUid != null) {
            String orgUnitQuery = "SELECT * FROM OrganisationUnit " +
                    "JOIN OrganisationUnitProgramLink ON OrganisationUnitProgramLink.organisationUnit = OrganisationUnit.uid " +
                    "WHERE OrganisationUnitProgramLink.program = ?";
            return briteDatabase.createQuery(OrganisationUnitModel.TABLE, orgUnitQuery, programUid)
                    .mapToList(OrganisationUnitModel::create);
        } else
            return briteDatabase.createQuery(OrganisationUnitModel.TABLE, " SELECT * FROM OrganisationUnit")
                    .mapToList(OrganisationUnitModel::create);
    }

    @Override
    public String getProgramColor(@NonNull String programUid) {
        Cursor cursor = briteDatabase.query(PROGRAM_COLOR_QUERY, programUid);
        if (cursor.moveToFirst()) {
            return cursor.getString(0);
        }
        return null;
    }

    @Override
    public ProgramModel getProgram(String programUid) {
        ProgramModel programModel = null;
        try (Cursor programCursor = briteDatabase.query("SELECT * FROM Program WHERE uid = ? LIMIT 1", programUid)) {
            if (programCursor != null && programCursor.moveToFirst())
                programModel = ProgramModel.create(programCursor);
        }
        return programModel;
    }
}