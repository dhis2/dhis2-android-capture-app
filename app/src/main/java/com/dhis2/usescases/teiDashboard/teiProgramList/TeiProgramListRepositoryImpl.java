package com.dhis2.usescases.teiDashboard.teiProgramList;

import android.content.ContentValues;
import android.database.sqlite.SQLiteConstraintException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.dhis2.utils.CodeGenerator;
import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

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
    public Observable<List<EnrollmentModel>> activeEnrollments(String trackedEntityId) {
        String SELECT_ACTIVE_ENROLLMENTS_WITH_TEI_ID = "SELECT * FROM " + EnrollmentModel.TABLE + " WHERE " + EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE + "='%s' AND " + EnrollmentModel.Columns.ENROLLMENT_STATUS + "='ACTIVE'";
        return briteDatabase.createQuery(EnrollmentModel.TABLE, String.format(SELECT_ACTIVE_ENROLLMENTS_WITH_TEI_ID, trackedEntityId))
                .mapToList(EnrollmentModel::create);
    }

    @NonNull
    @Override
    public Observable<List<EnrollmentModel>> otherEnrollments(String trackedEntityId) {
        String SELECT_ENROLLMENTS_WITH_TEI_ID = "SELECT * FROM " + EnrollmentModel.TABLE + " WHERE " + EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE + "='%s' AND " + EnrollmentModel.Columns.ENROLLMENT_STATUS + "!='ACTIVE'";
        return briteDatabase.createQuery(EnrollmentModel.TABLE, String.format(SELECT_ENROLLMENTS_WITH_TEI_ID, trackedEntityId))
                .mapToList(EnrollmentModel::create);
    }

    @NonNull
    @Override
    public Observable<List<ProgramModel>> allPrograms(String trackedEntityId) {
        String SELECT_PROGRAMS_WITH_TEI_ID = "SELECT * FROM " + ProgramModel.TABLE + " WHERE " + ProgramModel.Columns.TRACKED_ENTITY_TYPE + "='%s'";
        return briteDatabase.createQuery(EnrollmentModel.TABLE, String.format(SELECT_PROGRAMS_WITH_TEI_ID, trackedEntityId))
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
    public Observable<String> saveToEnroll(@NonNull String teiType, @NonNull String orgUnit, @NonNull String programUid, @Nullable String teiUid, @Nullable HashMap<String, String> queryData) {
        Date currentDate = Calendar.getInstance().getTime();
        return Observable.defer(() -> {
            TrackedEntityInstanceModel trackedEntityInstanceModel = null;
            if (teiUid == null) {
                String generatedUid = codeGenerator.generate();
                trackedEntityInstanceModel =
                        TrackedEntityInstanceModel.builder()
                                .uid(generatedUid)
                                .created(currentDate)
                                .lastUpdated(currentDate)
                                .organisationUnit(orgUnit)
                                .trackedEntityType(teiType)
                                .state(State.TO_POST)
                                .build();

                if (briteDatabase.insert(TrackedEntityInstanceModel.TABLE,
                        trackedEntityInstanceModel.toContentValues()) < 0) {
                    String message = String.format(Locale.US, "Failed to insert new tracked entity " +
                                    "instance for organisationUnit=[%s] and trackedEntity=[%s]",
                            orgUnit, teiType);
                    return Observable.error(new SQLiteConstraintException(message));
                }

                for (String key : queryData.keySet()) {
                    TrackedEntityAttributeValueModel attributeValueModel =
                            TrackedEntityAttributeValueModel.builder()
                                    .created(currentDate)
                                    .lastUpdated(currentDate)
                                    .value(queryData.get(key))
                                    .trackedEntityAttribute(key)
                                    .trackedEntityInstance(generatedUid)
                                    .build();
                    if (briteDatabase.insert(TrackedEntityAttributeValueModel.TABLE,
                            attributeValueModel.toContentValues()) < 0) {
                        String message = String.format(Locale.US, "Failed to insert new trackedEntityAttributeValue " +
                                        "instance for organisationUnit=[%s] and trackedEntity=[%s]",
                                orgUnit, teiType);
                        return Observable.error(new SQLiteConstraintException(message));
                    }
                }

            } else {
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
            }

            EnrollmentModel enrollmentModel = EnrollmentModel.builder()
                    .uid(codeGenerator.generate())
                    .created(currentDate)
                    .lastUpdated(currentDate)
                    .dateOfEnrollment(currentDate)
                    .program(programUid)
                    .organisationUnit(orgUnit)
                    .trackedEntityInstance(teiUid != null ? teiUid : trackedEntityInstanceModel.uid())
                    .enrollmentStatus(EnrollmentStatus.ACTIVE)
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
}