package com.dhis2.data.metadata;

import android.support.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;
import org.hisp.dhis.android.core.relationship.RelationshipTypeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityModel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;

/**
 * Created by ppajuelo on 04/12/2017.
 */

public class MetadataRepositoryImpl implements MetadataRepository {

    private final String PROGRAM_LIST_QUERY = String.format("SELECT * FROM %s WHERE ",
            ProgramModel.TABLE);

    private final String PROGRAM_LIST_ALL_QUERY = String.format("SELECT * FROM %s WHERE %s.%s = ",
            ProgramModel.TABLE, ProgramModel.TABLE, ProgramModel.Columns.UID);

    private final String TRACKED_ENTITY_QUERY = String.format("SELECT * FROM %s WHERE %s.%s = ",
            TrackedEntityModel.TABLE, TrackedEntityModel.TABLE, TrackedEntityModel.Columns.UID);

    private final String ORG_UNIT_QUERY = String.format("SELECT * FROM %s WHERE %s.%s = ",
            OrganisationUnitModel.TABLE, OrganisationUnitModel.TABLE, OrganisationUnitModel.Columns.UID);

    private final String PROGRAM_TRACKED_ENTITY_ATTRIBUTES_QUERY = String.format("SELECT * FROM %s WHERE %s.%s = ",
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.PROGRAM);

    private final String RELATIONSHIP_TYPE_QUERY = String.format("SELECT %s.* FROM %s " +
                    "INNER JOIN %s ON %s.%s = %s.%s  " +
                    "WHERE %s.%s = ",
            RelationshipTypeModel.TABLE, RelationshipTypeModel.TABLE,
            ProgramModel.TABLE, RelationshipTypeModel.TABLE, RelationshipTypeModel.Columns.UID, ProgramModel.TABLE, ProgramModel.Columns.RELATIONSHIP_TYPE,
            ProgramModel.TABLE, ProgramModel.Columns.UID);

    private final String RELATIONSHIP_TYPE_LIST_QUERY = String.format("SELECT * FROM %s ",
            RelationshipTypeModel.TABLE);

    private Set<String> RELATIONSHIP_TYPE_TABLES = new HashSet<>(Arrays.asList(RelationshipTypeModel.TABLE, ProgramModel.TABLE));

    private final BriteDatabase briteDatabase;

    public MetadataRepositoryImpl(@NonNull BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }

    @Override
    public Observable<TrackedEntityModel> getTrackedEntity(String trackedEntityUid) {
        return briteDatabase
                .createQuery(TrackedEntityModel.TABLE, TRACKED_ENTITY_QUERY + "'" + trackedEntityUid + "'")
                .mapToOne(TrackedEntityModel::create);
    }

    @Override
    public Observable<OrganisationUnitModel> getOrganisatuibUnit(String orgUnitUid) {
        return briteDatabase
                .createQuery(OrganisationUnitModel.TABLE, ORG_UNIT_QUERY + "'" + orgUnitUid + "'")
                .mapToOne(OrganisationUnitModel::create);
    }

    @Override
    public Observable<List<ProgramTrackedEntityAttributeModel>> getProgramTrackedEntityAttributes(String programUid) {
        return briteDatabase
                .createQuery(ProgramTrackedEntityAttributeModel.TABLE, PROGRAM_TRACKED_ENTITY_ATTRIBUTES_QUERY + "'" + programUid + "'")
                .mapToList(ProgramTrackedEntityAttributeModel::create);
    }

    @Override
    public Observable<RelationshipTypeModel> getRelationshipType(String relationshipTypeUid) {
        return briteDatabase
                .createQuery(RELATIONSHIP_TYPE_TABLES, RELATIONSHIP_TYPE_QUERY + "'" + relationshipTypeUid + "'")
                .mapToOneOrDefault(RelationshipTypeModel::create, RelationshipTypeModel.builder().build());

    }

    @Override
    public Observable<List<RelationshipTypeModel>> getRelationshipTypeList() {
        return briteDatabase
                .createQuery(RELATIONSHIP_TYPE_TABLES, RELATIONSHIP_TYPE_LIST_QUERY)
                .mapToList(RelationshipTypeModel::create);
    }

    @Override
    public Observable<List<ProgramModel>> getProgramModelFromEnrollmentList(List<Enrollment> enrollments) {
        String query = "";
        for (Enrollment enrollment : enrollments) {
            query = query.concat(ProgramModel.TABLE + "." + ProgramModel.Columns.UID + " = '" + enrollment.program() + "'");
            if (!enrollment.program().equals(enrollments.get(enrollments.size() - 1).program()))
                query = query.concat(" OR ");
        }

        return briteDatabase
                .createQuery(ProgramModel.TABLE, PROGRAM_LIST_QUERY + query)
                .mapToList(ProgramModel::create);

    }

    @Override
    public Observable<ProgramModel> getProgramWithId(String programUid) {
        return briteDatabase
                .createQuery(ProgramModel.TABLE, PROGRAM_LIST_ALL_QUERY + "'" + programUid + "'")
                .mapToOne(ProgramModel::create);
    }
}
