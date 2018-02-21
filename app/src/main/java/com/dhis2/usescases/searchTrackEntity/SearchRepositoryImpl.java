package com.dhis2.usescases.searchTrackEntity;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.option.OptionModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;

/**
 * Created by ppajuelo on 02/11/2017.
 */

public class SearchRepositoryImpl implements SearchRepository {

    private final BriteDatabase briteDatabase;

    private final String SELECT_PROGRAM_WITH_REGISTRATION = "SELECT * FROM " + ProgramModel.TABLE + " WHERE Program.programType='WITH_REGISTRATION' AND Program.trackedEntity = ";
    private final String SELECT_PROGRAM_ATTRIBUTES = "SELECT TrackedEntityAttribute.* FROM " + TrackedEntityAttributeModel.TABLE +
            " INNER JOIN " + ProgramTrackedEntityAttributeModel.TABLE +
            " ON " + TrackedEntityAttributeModel.TABLE + "." + TrackedEntityAttributeModel.Columns.UID + " = " + ProgramTrackedEntityAttributeModel.TABLE + "." + ProgramTrackedEntityAttributeModel.Columns.TRACKED_ENTITY_ATTRIBUTE +
            " WHERE " + ProgramTrackedEntityAttributeModel.TABLE + "." + ProgramTrackedEntityAttributeModel.Columns.PROGRAM + " = ";
    private final String SELECT_ATTRIBUTES = "SELECT * FROM " + TrackedEntityAttributeModel.TABLE;
    private final String SELECT_OPTION_SET = "SELECT * FROM " + OptionModel.TABLE + " WHERE Option.optionSet = ";

    private final String GET_TRACKED_ENTITY_INSTANCES =
            "SELECT " +
                    TrackedEntityInstanceModel.TABLE+"."+TrackedEntityInstanceModel.Columns.UID +", "+
                    TrackedEntityInstanceModel.TABLE+"."+TrackedEntityInstanceModel.Columns.CREATED_AT_CLIENT +", "+
                    TrackedEntityInstanceModel.TABLE+"."+TrackedEntityInstanceModel.Columns.LAST_UPDATED_AT_CLIENT +", "+
                    TrackedEntityInstanceModel.TABLE+"."+TrackedEntityInstanceModel.Columns.ORGANISATION_UNIT +", "+
                    TrackedEntityInstanceModel.TABLE+"."+TrackedEntityInstanceModel.Columns.TRACKED_ENTITY +", "+
                    TrackedEntityInstanceModel.TABLE+"."+TrackedEntityInstanceModel.Columns.CREATED +", "+
                    TrackedEntityInstanceModel.TABLE+"."+TrackedEntityInstanceModel.Columns.LAST_UPDATED +", "+
                    TrackedEntityInstanceModel.TABLE+"."+TrackedEntityInstanceModel.Columns.STATE +", "+
                    TrackedEntityInstanceModel.TABLE+"."+TrackedEntityInstanceModel.Columns.ID +", "+
                    EnrollmentModel.TABLE+"."+EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE+" AS enroll" +", "+
                    TrackedEntityAttributeValueModel.TABLE+"."+TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_INSTANCE+" AS attr" +
                    " FROM ((" + TrackedEntityInstanceModel.TABLE +
                    " JOIN " + EnrollmentModel.TABLE + " ON enroll = " + TrackedEntityInstanceModel.TABLE + "." + TrackedEntityInstanceModel.Columns.UID + ")" +
                    " JOIN " + TrackedEntityAttributeValueModel.TABLE + " ON attr = " + TrackedEntityInstanceModel.TABLE + "." + TrackedEntityInstanceModel.Columns.UID + ")" +
                    " WHERE ";

    private static final String[] TABLE_NAMES = new String[]{TrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.TABLE};
    private static final Set<String> TABLE_SET = new HashSet<>(Arrays.asList(TABLE_NAMES));
    private static final String[] TEI_TABLE_NAMES = new String[]{TrackedEntityInstanceModel.TABLE,
            EnrollmentModel.TABLE, TrackedEntityAttributeValueModel.TABLE};
    private static final Set<String> TEI_TABLE_SET = new HashSet<>(Arrays.asList(TEI_TABLE_NAMES));


    SearchRepositoryImpl(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }


    @NonNull
    @Override
    public Observable<List<TrackedEntityAttributeModel>> programAttributes(String programId) {
        return briteDatabase.createQuery(TABLE_SET, SELECT_PROGRAM_ATTRIBUTES + "'" + programId + "'")
                .mapToList(TrackedEntityAttributeModel::create);
    }

    @Override
    public Observable<List<TrackedEntityAttributeModel>> programAttributes() {
        return briteDatabase.createQuery(TrackedEntityAttributeModel.TABLE, SELECT_ATTRIBUTES)
                .mapToList(TrackedEntityAttributeModel::create);
    }

    @Override
    public Observable<List<OptionModel>> optionSet(String optionSetId) {
        return briteDatabase.createQuery(OptionModel.TABLE, SELECT_OPTION_SET + "'" + optionSetId + "'")
                .mapToList(OptionModel::create);
    }

    @Override
    public Observable<List<ProgramModel>> programsWithRegistration(String programTypeId) {
        return briteDatabase.createQuery(ProgramModel.TABLE, SELECT_PROGRAM_WITH_REGISTRATION + "'" + programTypeId + "'")
                .mapToList(ProgramModel::create);
    }

    @Override
    public Observable<List<TrackedEntityInstanceModel>> trackedEntityInstances(@NonNull String teType,
                                                                               @Nullable String programUid,
                                                                               @Nullable String enrollmentDate,
                                                                               @Nullable String incidentDate,
                                                                               @Nullable List<TrackedEntityAttributeValueModel> queryData) {

        String teiTypeWHERE = "TrackedEntityInstance.trackedEntity = '" + teType + "'";
        String TEI_FINAL_QUERY = GET_TRACKED_ENTITY_INSTANCES + teiTypeWHERE;
        if (programUid != null && !programUid.isEmpty()) {
            String programWHERE = "Enrollment.program = '" + programUid + "'";
            TEI_FINAL_QUERY += " OR " + programWHERE;
        }

        if (enrollmentDate != null && !enrollmentDate.isEmpty()) {
            String enrollmentDateWHERE = "Enrollment.enrollmentDate = '" + enrollmentDate + "'";
            TEI_FINAL_QUERY += " OR " + enrollmentDateWHERE;
        }
        if (incidentDate != null && !incidentDate.isEmpty()) {
            String incidentDateWHERE = "Enrollment.incidentData = '" + incidentDate + "'";
            TEI_FINAL_QUERY += " OR " + incidentDateWHERE;
        }


        if (queryData != null && !queryData.isEmpty()) { //TODO: IMPROVE DATA QUERY
            StringBuilder teiAttributeWHERE = new StringBuilder("");
            teiAttributeWHERE.append("TrakedEntityAttributeValue.value IN (");
            for (int i = 0; i < queryData.size(); i++) {
                teiAttributeWHERE.append("'").append(queryData.get(i).value()).append("'");
                if (i < queryData.size() - 1)
                    teiAttributeWHERE.append(",");
            }
            teiAttributeWHERE.append(")");

            TEI_FINAL_QUERY += " OR " + teiTypeWHERE+ " GROUP BY "+TrackedEntityInstanceModel.TABLE+"."+TrackedEntityInstanceModel.Columns.UID;
        }


        return briteDatabase.createQuery(TEI_TABLE_SET, TEI_FINAL_QUERY)
                .mapToList(TrackedEntityInstanceModel::create);
    }
}
