package com.dhis2.usescases.teiDashboard.teiProgramList;

import android.support.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.enrollment.EnrollmentModel;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by ppajuelo on 02/11/2017.
 *
 */

public class TeiProgramListRepositoryImpl implements TeiProgramListRepository {

    private final BriteDatabase briteDatabase;

    TeiProgramListRepositoryImpl(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }

    @NonNull
    @Override
    public Observable<List<EnrollmentModel>> activeEnrollments(String trackedEntityId) {
        String SELECT_ACTIVE_ENROLLMENTS_WITH_TEI_ID = "SELECT * FROM " + EnrollmentModel.TABLE + " WHERE " + EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE + "='%s' AND " + EnrollmentModel.Columns.ENROLLMENT_STATUS + "='ACTIVE'" ;
        return briteDatabase.createQuery(EnrollmentModel.TABLE, String.format(SELECT_ACTIVE_ENROLLMENTS_WITH_TEI_ID, trackedEntityId))
                .mapToList(EnrollmentModel::create);
    }

    @NonNull
    @Override
    public Observable<List<EnrollmentModel>> otherEnrollments(String trackedEntityId) {
        String SELECT_ENROLLMENTS_WITH_TEI_ID = "SELECT * FROM " + EnrollmentModel.TABLE + " WHERE " + EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE + "='%s' AND " + EnrollmentModel.Columns.ENROLLMENT_STATUS + "!='ACTIVE'" ;
        return briteDatabase.createQuery(EnrollmentModel.TABLE, String.format(SELECT_ENROLLMENTS_WITH_TEI_ID, trackedEntityId))
                .mapToList(EnrollmentModel::create);
    }
}