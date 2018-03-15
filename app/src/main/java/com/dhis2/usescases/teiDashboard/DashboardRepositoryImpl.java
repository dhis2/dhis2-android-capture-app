package com.dhis2.usescases.teiDashboard;

import android.content.ContentValues;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;
import org.hisp.dhis.android.core.relationship.RelationshipModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;

/**
 * Created by ppajuelo on 30/11/2017.
 *
 */

public class DashboardRepositoryImpl implements DashboardRepository {

    private final String PROGRAM_QUERY = String.format("SELECT %s.* FROM %s WHERE %s.%s = ",
            ProgramModel.TABLE, ProgramModel.TABLE, ProgramModel.TABLE, ProgramModel.Columns.UID);

    private final String ATTRIBUTES_QUERY = String.format("SELECT %s.* FROM %s INNER JOIN %s ON %s.%s = %s.%s WHERE %s.%s = ",
            TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.TABLE,
            ProgramTrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.Columns.UID,
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.TRACKED_ENTITY_ATTRIBUTE,
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.PROGRAM);

    private final String ORG_UNIT_QUERY = String.format("SELECT * FROM %s WHERE %s.%s = ",
            OrganisationUnitModel.TABLE, OrganisationUnitModel.TABLE, OrganisationUnitModel.Columns.UID
    );

    private final String ENROLLMENT_QUERY = String.format("SELECT * FROM %s WHERE %s.%s = ? AND %s.%s = ?",
            EnrollmentModel.TABLE, EnrollmentModel.TABLE, EnrollmentModel.Columns.PROGRAM,
            EnrollmentModel.TABLE, EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE);

    private final String PROGRAM_STAGE_QUERY = String.format("SELECT * FROM %s WHERE %s.%s = ",
            ProgramStageModel.TABLE, ProgramStageModel.TABLE, ProgramStageModel.Columns.PROGRAM);

    private final String EVENTS_QUERY = String.format(
            "SELECT Event.* FROM %s JOIN %s " +
                    "ON %s.%s = %s.%s " +
                    "WHERE %s.%s = ? " +
                    "AND %s.%s = ?",
            EventModel.TABLE, EnrollmentModel.TABLE,
            EnrollmentModel.TABLE, EnrollmentModel.Columns.UID, EventModel.TABLE, EventModel.Columns.ENROLLMENT_UID,
            EnrollmentModel.TABLE, EnrollmentModel.Columns.PROGRAM,
            EnrollmentModel.TABLE, EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE);
    private static final Set<String> EVENTS_TABLE = new HashSet<>(Arrays.asList(EventModel.TABLE, EnrollmentModel.TABLE));

    private final String ATTRIBUTE_VALUES_QUERY = String.format(
            "SELECT TrackedEntityAttributeValue.* FROM %s JOIN %s " +
                    "ON %s.%s = %s.%s " +
                    "WHERE %s.%s = ? " +
                    "AND %s.%s = ?",
            TrackedEntityAttributeValueModel.TABLE, ProgramTrackedEntityAttributeModel.TABLE,
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.TRACKED_ENTITY_ATTRIBUTE, TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_ATTRIBUTE,
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.PROGRAM,
            TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_INSTANCE);
    private final String ATTRIBUTE_VALUES_NO_PROGRAM_QUERY = String.format(
            "SELECT %s.* FROM %s JOIN %s " +
                    "ON %s.%s = %s.%s " +
                    "WHERE %s.%s = ?",
            TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.TABLE, ProgramTrackedEntityAttributeModel.TABLE,
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.TRACKED_ENTITY_ATTRIBUTE, TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_ATTRIBUTE,
            TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_INSTANCE);
    private static final Set<String> ATTRIBUTE_VALUES_TABLE = new HashSet<>(Arrays.asList(TrackedEntityAttributeValueModel.TABLE, ProgramTrackedEntityAttributeModel.TABLE));

    private final String RELATIONSHIP_QUERY = String.format(
            "SELECT Relationship.* FROM %s JOIN %s " +
                    "ON %s.%s = %s.%s " +
                    "WHERE %s.%s = ? " +
                    "AND %s.%s = ?",
            RelationshipModel.TABLE, ProgramModel.TABLE,
            ProgramModel.TABLE, ProgramModel.Columns.RELATIONSHIP_TYPE, RelationshipModel.TABLE, RelationshipModel.Columns.RELATIONSHIP_TYPE,
            ProgramModel.TABLE, ProgramModel.Columns.UID,
            RelationshipModel.TABLE, RelationshipModel.Columns.TRACKED_ENTITY_INSTANCE_A);
    private static final Set<String> RELATIONSHIP_TABLE = new HashSet<>(Arrays.asList(RelationshipModel.TABLE, ProgramModel.TABLE));

    private static final String[] ATTRUBUTE_TABLES = new String[]{TrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.TABLE};
    private static final Set<String> ATTRIBUTE_TABLE_SET = new HashSet<>(Arrays.asList(ATTRUBUTE_TABLES));


    private final BriteDatabase briteDatabase;

    public DashboardRepositoryImpl(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }

    @Override
    public Observable<ProgramModel> getProgramData(String programUid) {
        return briteDatabase.createQuery(ProgramModel.TABLE, PROGRAM_QUERY + "'" + programUid + "'")
                .mapToOne(ProgramModel::create);
    }

    @Override
    public Observable<List<TrackedEntityAttributeModel>> getAttributes(String programId) {
        return briteDatabase.createQuery(ATTRIBUTE_TABLE_SET, ATTRIBUTES_QUERY + "'" + programId + "'")
                .mapToList(TrackedEntityAttributeModel::create);
    }

    @Override
    public Observable<OrganisationUnitModel> getOrgUnit(String orgUnitId) {
        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, ORG_UNIT_QUERY + "'" + orgUnitId + "'")
                .mapToOne(OrganisationUnitModel::create);
    }

    @Override
    public Observable<List<ProgramStageModel>> getProgramStages(String programUid) {
        return briteDatabase.createQuery(ProgramStageModel.TABLE, PROGRAM_STAGE_QUERY + "'" + programUid + "'")
                .mapToList(ProgramStageModel::create);
    }

    @Override
    public Observable<EnrollmentModel> getEnrollment(String programUid, String teiUid) {
        return briteDatabase.createQuery(EnrollmentModel.TABLE, ENROLLMENT_QUERY, programUid, teiUid)
                .mapToOne(EnrollmentModel::create);
    }

    @Override
    public Observable<List<EventModel>> getTEIEnrollmentEvents(String programUid, String teiUid) {
        return briteDatabase.createQuery(EVENTS_TABLE, EVENTS_QUERY, programUid, teiUid)
                .mapToList(EventModel::create);
    }

    @Override
    public Observable<List<TrackedEntityAttributeValueModel>> getTEIAttributeValues(String programUid, String teiUid) {
        if (programUid != null)
            return briteDatabase.createQuery(ATTRIBUTE_VALUES_TABLE, ATTRIBUTE_VALUES_QUERY, programUid, teiUid)
                    .mapToList(TrackedEntityAttributeValueModel::create);
        else
            return briteDatabase.createQuery(ATTRIBUTE_VALUES_TABLE, ATTRIBUTE_VALUES_NO_PROGRAM_QUERY, teiUid)
                    .mapToList(TrackedEntityAttributeValueModel::create);
    }

    @Override
    public Observable<List<RelationshipModel>> getRelationships(String programUid, String teiUid) {
        return briteDatabase.createQuery(RELATIONSHIP_TABLE, RELATIONSHIP_QUERY, programUid, teiUid)
                .mapToList(RelationshipModel::create);

    }

    @Override
    public int setFollowUp(String enrollmentUid, boolean followUp) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(EnrollmentModel.Columns.FOLLOW_UP, followUp ? "1" : "0");

        return briteDatabase.update(EnrollmentModel.TABLE, contentValues, EnrollmentModel.Columns.UID + " = ?", enrollmentUid);
    }
}
