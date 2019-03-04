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

import static org.dhis2.data.database.SqlConstants.ALL;
import static org.dhis2.data.database.SqlConstants.AND;
import static org.dhis2.data.database.SqlConstants.AS;
import static org.dhis2.data.database.SqlConstants.COMMA;
import static org.dhis2.data.database.SqlConstants.EQUAL;
import static org.dhis2.data.database.SqlConstants.FROM;
import static org.dhis2.data.database.SqlConstants.GROUP_BY;
import static org.dhis2.data.database.SqlConstants.JOIN;
import static org.dhis2.data.database.SqlConstants.ON;
import static org.dhis2.data.database.SqlConstants.POINT;
import static org.dhis2.data.database.SqlConstants.QUESTION_MARK;
import static org.dhis2.data.database.SqlConstants.QUOTE;
import static org.dhis2.data.database.SqlConstants.SELECT;
import static org.dhis2.data.database.SqlConstants.VARIABLE;
import static org.dhis2.data.database.SqlConstants.WHERE;

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

    private static final String PROGRAM_COLOR_QUERY = String.format(
            SELECT + VARIABLE + FROM + VARIABLE +
                    WHERE + VARIABLE + EQUAL + QUOTE + "Program" + QUOTE + AND + VARIABLE + EQUAL + QUESTION_MARK,
            ObjectStyleModel.Columns.COLOR, ObjectStyleModel.TABLE,
            ObjectStyleModel.Columns.OBJECT_TABLE,
            ObjectStyleModel.Columns.UID
    );

    @NonNull
    @Override
    public Observable<List<EnrollmentViewModel>> activeEnrollments(String trackedEntityId) {
        String selectActiveEnrollmentWithTeiId = SELECT +
                EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.UID + COMMA +
                EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.ENROLLMENT_DATE + COMMA +
                EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.FOLLOW_UP + COMMA +
                ObjectStyleModel.TABLE + POINT + ObjectStyleModel.Columns.ICON + COMMA +
                ObjectStyleModel.TABLE + POINT + ObjectStyleModel.Columns.COLOR + COMMA +
                ProgramModel.TABLE + POINT + ProgramModel.Columns.DISPLAY_NAME + AS + "programName" + COMMA +
                ProgramModel.TABLE + POINT + ProgramModel.Columns.UID + AS + "programUid" + COMMA +
                OrganisationUnitModel.TABLE + POINT + ProgramModel.Columns.DISPLAY_NAME + AS + "OrgUnitName" +
                FROM + EnrollmentModel.TABLE +
                " LEFT JOIN ObjectStyle ON ObjectStyle.uid = Enrollment.program " +
                "JOIN Program ON Program.uid = Enrollment.program " +
                "JOIN OrganisationUnit ON OrganisationUnit.uid = Enrollment.organisationUnit " +
                "WHERE Enrollment.trackedEntityInstance = ? AND Enrollment.status = 'ACTIVE'";
        String[] tableNames = new String[]{ProgramModel.TABLE, ObjectStyleModel.TABLE, OrganisationUnitProgramLinkModel.TABLE};
        Set<String> tableSet = new HashSet<>(Arrays.asList(tableNames));
        return briteDatabase.createQuery(tableSet, selectActiveEnrollmentWithTeiId, trackedEntityId == null ? "" : trackedEntityId)
                .mapToList(EnrollmentViewModel::fromCursor);
    }

    @NonNull
    @Override
    public Observable<List<EnrollmentViewModel>> otherEnrollments(String trackedEntityId) {
        String selectActiveEnrollmentWithTeiId = SELECT +
                EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.UID + COMMA +
                EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.ENROLLMENT_DATE + COMMA +
                EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.FOLLOW_UP + COMMA +
                ObjectStyleModel.TABLE + POINT + ObjectStyleModel.Columns.ICON + COMMA +
                ObjectStyleModel.TABLE + POINT + ObjectStyleModel.Columns.COLOR + COMMA +
                ProgramModel.TABLE + POINT + ProgramModel.Columns.DISPLAY_NAME + AS + "programName" + COMMA +
                ProgramModel.TABLE + POINT + ProgramModel.Columns.UID + AS + "programUid" + COMMA +
                OrganisationUnitModel.TABLE + POINT + OrganisationUnitModel.Columns.DISPLAY_NAME + AS + "OrgUnitName" +
                FROM + EnrollmentModel.TABLE +
                " LEFT JOIN ObjectStyle ON ObjectStyle.uid = Enrollment.program " +
                "JOIN Program ON Program.uid = Enrollment.program " +
                "JOIN OrganisationUnit ON OrganisationUnit.uid = Enrollment.organisationUnit " +
                "WHERE Enrollment.trackedEntityInstance = ? AND Enrollment.status != 'ACTIVE'";
        String[] tableNames = new String[]{ProgramModel.TABLE, ObjectStyleModel.TABLE, OrganisationUnitProgramLinkModel.TABLE};
        Set<String> tableSet = new HashSet<>(Arrays.asList(tableNames));
        return briteDatabase.createQuery(tableSet, selectActiveEnrollmentWithTeiId, trackedEntityId == null ? "" : trackedEntityId)
                .mapToList(EnrollmentViewModel::fromCursor);
    }


    private static final String PROGRAM_MODELS_FOR_TEI = SELECT +
            ProgramModel.TABLE + POINT + ProgramModel.Columns.UID + COMMA +
            ProgramModel.TABLE + POINT + ProgramModel.Columns.DISPLAY_NAME + COMMA +
            ObjectStyleModel.TABLE + POINT + ObjectStyleModel.Columns.COLOR + COMMA +
            ObjectStyleModel.TABLE + POINT + ObjectStyleModel.Columns.ICON + COMMA +
            ProgramModel.TABLE + POINT + ProgramModel.Columns.PROGRAM_TYPE + COMMA +
            ProgramModel.TABLE + POINT + ProgramModel.Columns.TRACKED_ENTITY_TYPE + COMMA +
            ProgramModel.TABLE + POINT + ProgramModel.Columns.DESCRIPTION + COMMA +
            ProgramModel.TABLE + POINT + ProgramModel.Columns.ONLY_ENROLL_ONCE + COMMA +
            ProgramModel.TABLE + POINT + ProgramModel.Columns.ACCESS_DATA_WRITE +
            FROM + ProgramModel.TABLE +
            " LEFT JOIN ObjectStyle ON ObjectStyle.uid = Program.uid " +
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
        String selectEnrolledProgramsWithTeiId = SELECT + ALL + FROM + ProgramModel.TABLE + JOIN + EnrollmentModel.TABLE +
                ON + EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.PROGRAM + EQUAL + ProgramModel.TABLE + POINT + ProgramModel.Columns.UID +
                WHERE + EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE +
                EQUAL + QUOTE + VARIABLE + QUOTE + GROUP_BY + ProgramModel.TABLE + POINT + ProgramModel.Columns.UID;
        return briteDatabase.createQuery(EnrollmentModel.TABLE, String.format(selectEnrolledProgramsWithTeiId, trackedEntityId == null ? "" : trackedEntityId))
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
                    TrackedEntityInstanceModel.Columns.UID + EQUAL + QUESTION_MARK, teiUid) <= 0) {
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
        Cursor programCursor = briteDatabase.query("SELECT * FROM Program WHERE uid = ? LIMIT 1", programUid);
        if (programCursor != null) {
            if (programCursor.moveToFirst())
                programModel = ProgramModel.create(programCursor);
            programCursor.close();
        }
        return programModel;
    }

}