package com.dhis2.data.metadata;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;

import com.dhis2.R;
import com.dhis2.data.tuples.Pair;
import com.squareup.sqlbrite2.BriteDatabase;
import com.squareup.sqlbrite2.SqlBrite.Query;

import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionModel;
import org.hisp.dhis.android.core.common.ObjectStyleModel;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.dataelement.DataElementModel;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.option.OptionModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStageDataElementModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;
import org.hisp.dhis.android.core.relationship.RelationshipTypeModel;
import org.hisp.dhis.android.core.resource.ResourceModel;
import org.hisp.dhis.android.core.settings.SystemSettingModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityTypeModel;
import org.hisp.dhis.android.core.user.AuthenticatedUserModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;


/**
 * Created by ppajuelo on 04/12/2017.
 */

public class MetadataRepositoryImpl implements MetadataRepository {

    private static final String SELECT_PROGRMAS_TO_ENROLL = String.format(
            "SELECT * FROM %s WHERE %s.%s = ?",
            ProgramModel.TABLE, ProgramModel.TABLE, ProgramModel.Columns.TRACKED_ENTITY_TYPE
    );

    private static final String SELECT_TEI_ENROLLMENTS = String.format(
            "SELECT * FROM %s WHERE %s.%s =",
            EnrollmentModel.TABLE,
            EnrollmentModel.TABLE, EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE);

    private static final String SELECT_ENROLLMENT_EVENTS = String.format(
            "SELECT * FROM %s WHERE %s.%s != %s AND %s.%s = ? LIMIT 1",
            EventModel.TABLE,
            EventModel.TABLE,
            EventModel.Columns.STATE,
            State.TO_DELETE,
            EventModel.TABLE,
            EventModel.Columns.ENROLLMENT);

    private static final String SELECT_ENROLLMENT_LAST_EVENT = String.format(
            "SELECT %s.* FROM %s JOIN %s ON %s.%s = %s.%s WHERE %s.%s = ? ORDER BY %s.%s AND %s.%s != %s DESC LIMIT 1",
            EventModel.TABLE, EventModel.TABLE, EnrollmentModel.TABLE, EnrollmentModel.TABLE, EnrollmentModel.Columns.UID, EventModel.TABLE, EventModel.Columns.ENROLLMENT,
            EnrollmentModel.TABLE, EnrollmentModel.Columns.UID, EventModel.TABLE, EventModel.Columns.EVENT_DATE, EventModel.TABLE, EventModel.Columns.STATE, State.TO_DELETE
    );

    private Set<String> SELECT_ENROLLMENT_LAST_EVENT_TABLES = new HashSet<>(Arrays.asList(EventModel.TABLE, EnrollmentModel.TABLE));

    private final String PROGRAM_LIST_QUERY = String.format("SELECT * FROM %s WHERE ",
            ProgramModel.TABLE);

    private final String ACTIVE_TEI_PROGRAMS = String.format(
            " SELECT %s.* FROM %s " +
                    "JOIN %s ON %s.%s = %s.%s " +
                    "WHERE %s.%s = ?",
            ProgramModel.TABLE,
            ProgramModel.TABLE,
            EnrollmentModel.TABLE, EnrollmentModel.TABLE, EnrollmentModel.Columns.PROGRAM, ProgramModel.TABLE, ProgramModel.Columns.UID,
            EnrollmentModel.TABLE, EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE);

    private Set<String> ACTIVE_TEI_PROGRAMS_TABLES = new HashSet<>(Arrays.asList(ProgramModel.TABLE, EnrollmentModel.TABLE));


    private final String PROGRAM_LIST_ALL_QUERY = String.format("SELECT * FROM %s WHERE %s.%s = ",
            ProgramModel.TABLE, ProgramModel.TABLE, ProgramModel.Columns.UID);

    private final String TRACKED_ENTITY_QUERY = String.format("SELECT * FROM %s WHERE %s.%s = ",
            TrackedEntityTypeModel.TABLE, TrackedEntityTypeModel.TABLE, TrackedEntityTypeModel.Columns.UID);

    private final String TRACKED_ENTITY_INSTANCE_QUERY = String.format("SELECT * FROM %s WHERE %s.%s = ",
            TrackedEntityInstanceModel.TABLE, TrackedEntityInstanceModel.TABLE, TrackedEntityInstanceModel.Columns.UID);

    private final String ORG_UNIT_QUERY = String.format("SELECT * FROM %s WHERE %s.%s = ",
            OrganisationUnitModel.TABLE, OrganisationUnitModel.TABLE, OrganisationUnitModel.Columns.UID);

    private final String TEI_ORG_UNIT_QUERY = String.format(
            "SELECT * FROM %s " +
                    "JOIN %s ON %s.%s = %s.%s " +
                    "WHERE  %s.%s = ? LIMIT 1",
            OrganisationUnitModel.TABLE,
            TrackedEntityInstanceModel.TABLE, TrackedEntityInstanceModel.TABLE, TrackedEntityInstanceModel.Columns.ORGANISATION_UNIT, OrganisationUnitModel.TABLE, OrganisationUnitModel.Columns.UID,
            TrackedEntityInstanceModel.TABLE, TrackedEntityInstanceModel.Columns.UID);

    private final String ENROLLMENT_ORG_UNIT_QUERY =
            "SELECT OrganisationUnit.* FROM OrganisationUnit " +
                    "WHERE OrganisationUnit.uid IN (" +
                    "SELECT Enrollment.organisationUnit FROM Enrollment " +
                    "JOIN Program ON Program.uid = Enrollment.program WHERE Enrollment.trackedEntityInstance = ? AND Program.uid = ? LIMIT 1)";


    private Set<String> TEI_ORG_UNIT_TABLES = new HashSet<>(Arrays.asList(OrganisationUnitModel.TABLE, TrackedEntityInstanceModel.TABLE));

    private final String ORG_UNIT_DATE_QUERY = String.format(
            "SELECT * FROM %s " +
                    "WHERE %s.%s < ? AND %s.%s > ?",
            OrganisationUnitModel.TABLE,
            OrganisationUnitModel.TABLE, OrganisationUnitModel.Columns.OPENING_DATE, OrganisationUnitModel.TABLE, OrganisationUnitModel.Columns.CLOSED_DATE
    );

    private final String PROGRAM_TRACKED_ENTITY_ATTRIBUTES_QUERY = String.format("SELECT * FROM %s WHERE %s.%s = ",
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.PROGRAM);

    private final String PROGRAM_TRACKED_ENTITY_ATTRIBUTES_NO_PROGRAM_QUERY = String.format(
            "SELECT %s.* FROM %s " +
                    "JOIN %s ON %s.%s = %s.%s " +
                    "WHERE %s.%s = '1' GROUP BY %s.%s",
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.TABLE,
            TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.Columns.UID, ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.TRACKED_ENTITY_ATTRIBUTE,
            TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.Columns.DISPLAY_IN_LIST_NO_PROGRAM, TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.Columns.UID);
    private Set<String> PROGRAM_TRACKED_ENTITY_ATTRIBUTES_NO_PROGRAM_TABLES = new HashSet<>(Arrays.asList(TrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.TABLE));

    private final String PROGRAM_TRACKED_ENTITY_ATTRIBUTES_VALUES_QUERY = String.format(
            "SELECT %s.* FROM %s " +
                    "JOIN %s ON %s.%s = %s.%s " +
                    "WHERE %s.%s = ? AND %s.%s <> '0' ORDER BY %s.%s ASC",
            TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.TABLE,
            TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.Columns.UID, TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_ATTRIBUTE,
            TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_INSTANCE,
            TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.Columns.SORT_ORDER_IN_LIST_NO_PROGRAM,
            TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.Columns.SORT_ORDER_IN_LIST_NO_PROGRAM
    );
    private final Set<String> ATTR_VALUE_TABLES = new HashSet<>(Arrays.asList(TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeModel.TABLE));

    private final String PROGRAM_TRACKED_ENTITY_ATTRIBUTES_VALUES_PROGRAM_QUERY = String.format(
            "SELECT %s.* FROM %s " +
                    "JOIN %s ON %s.%s = %s.%s " +
                    "WHERE %s.%s = ? AND %s.%s = ? AND " +
                    "%s.%s = 1 " +
                    "ORDER BY %s.%s ASC",
            TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.TABLE,
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.TRACKED_ENTITY_ATTRIBUTE, TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_ATTRIBUTE,
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.PROGRAM,
            TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_INSTANCE,
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.DISPLAY_IN_LIST,
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.SORT_ORDER);
    private final Set<String> ATTR_PROGRAM_VALUE_TABLES = new HashSet<>(Arrays.asList(TrackedEntityAttributeValueModel.TABLE, ProgramTrackedEntityAttributeModel.TABLE));

    private final String TE_ATTRIBUTE_QUERY = String.format(
            "SELECT * FROM %s WHERE %s.%s = ? LIMIT 1",
            TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.Columns.UID);

    private final String RELATIONSHIP_TYPE_QUERY = String.format("SELECT %s.* FROM %s " +
                    "JOIN %s ON %s.%s = %s.%s  " +
                    "WHERE %s.%s = ",
            RelationshipTypeModel.TABLE, RelationshipTypeModel.TABLE,
            ProgramModel.TABLE, ProgramModel.TABLE, ProgramModel.Columns.RELATIONSHIP_TYPE, RelationshipTypeModel.TABLE, RelationshipTypeModel.Columns.UID,
            ProgramModel.TABLE, ProgramModel.Columns.UID);

    private Set<String> RELATIONSHIP_TYPE_TABLES = new HashSet<>(Arrays.asList(RelationshipTypeModel.TABLE, ProgramModel.TABLE));

    private final String RELATIONSHIP_TYPE_LIST_QUERY = String.format("SELECT * FROM %s ",
            RelationshipTypeModel.TABLE);

    private final String DATA_ELEMENT_QUERY = String.format("SELECT * FROM %s WHERE %s.%s = ",
            DataElementModel.TABLE, DataElementModel.TABLE, DataElementModel.Columns.UID);


    private final String SELECT_PROGRAM_STAGE = String.format("SELECT * FROM %s WHERE %s.%s = ",
            ProgramStageModel.TABLE, ProgramStageModel.TABLE, ProgramStageModel.Columns.UID);

    private final String SELECT_CATEGORY_OPTION = String.format("SELECT * FROM %s WHERE %s.%s = ",
            CategoryOptionModel.TABLE, CategoryOptionModel.TABLE, CategoryOptionModel.Columns.UID);

    private final String SELECT_CATEGORY_OPTION_COMBO = String.format("SELECT * FROM %s WHERE %s.%s = ",
            CategoryOptionComboModel.TABLE, CategoryOptionComboModel.TABLE, CategoryOptionComboModel.Columns.UID);

    private final String SELECT_CATEGORY_COMBO = String.format("SELECT * FROM %s WHERE %s.%s = ",
            CategoryComboModel.TABLE, CategoryComboModel.TABLE, CategoryComboModel.Columns.UID);


    private static final String RESOURCES_QUERY = String.format("SELECT * FROM %s WHERE %s.%s = ? LIMIT 1",
            ResourceModel.TABLE, ResourceModel.TABLE, ResourceModel.Columns.RESOURCE_TYPE);

    private static final String EXPIRY_DATE_PERIOD_QUERY = String.format(
            "SELECT program.* FROM %s " +
                    "JOIN %s ON %s.%s = %s.%s " +
                    "WHERE %s.%s = ? " +
                    "LIMIT 1",
            ProgramModel.TABLE,
            EventModel.TABLE, ProgramModel.TABLE, ProgramModel.Columns.UID, EventModel.TABLE, EventModel.Columns.PROGRAM,
            EventModel.TABLE, EventModel.Columns.UID);

    private final String RESERVED_UIDS = "SELECT DISTINCT\n" +
            "ProgramTrackedEntityAttribute.trackedEntityAttribute,\n" +
            "OrganisationUnitProgramLink.organisationUnit\n" +
            "FROM ProgramTrackedEntityAttribute\n" +
            "JOIN TrackedEntityAttribute ON TrackedEntityAttribute.uid = ProgramTrackedEntityAttribute.trackedEntityAttribute\n" +
            "JOIN OrganisationUnitProgramLink ON OrganisationUnitProgramLink.program = ProgramTrackedEntityAttribute.program\n" +
            "WHERE TrackedEntityAttribute.generated = ?";

    private final BriteDatabase briteDatabase;

    MetadataRepositoryImpl(@NonNull BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }

    @Override
    public Observable<TrackedEntityTypeModel> getTrackedEntity(String trackedEntityUid) {
        String id = trackedEntityUid == null ? "" : trackedEntityUid;
        return briteDatabase
                .createQuery(TrackedEntityTypeModel.TABLE, TRACKED_ENTITY_QUERY + "'" + id + "' LIMIT 1")
                .mapToOne(TrackedEntityTypeModel::create);
    }

    @Override
    public Observable<CategoryComboModel> getCategoryComboWithId(String categoryComboId) {
        String id = categoryComboId == null ? "" : categoryComboId;
        return briteDatabase
                .createQuery(CategoryComboModel.TABLE, SELECT_CATEGORY_COMBO + "'" + id + "' LIMIT 1")
                .mapToOne(CategoryComboModel::create);
    }

    public Observable<TrackedEntityInstanceModel> getTrackedEntityInstance(String teiUid) {
        String id = teiUid == null ? "" : teiUid;
        return briteDatabase
                .createQuery(TrackedEntityInstanceModel.TABLE, TRACKED_ENTITY_INSTANCE_QUERY + "'" + id + "' LIMIT 1")
                .mapToOne(TrackedEntityInstanceModel::create);
    }

    @Override
    public Observable<List<TrackedEntityInstanceModel>> getTrackedEntityInstances(String programUid) {
        String id = programUid == null ? "" : programUid;
        String PROGRAM_TRACKED_ENTITY_INSTANCE_QUERY = "SELECT * FROM " + TrackedEntityInstanceModel.TABLE
                + " JOIN " + TrackedEntityTypeModel.TABLE + " ON " + TrackedEntityTypeModel.TABLE + "." + TrackedEntityTypeModel.Columns.UID + " = " + TrackedEntityInstanceModel.TABLE + "." + TrackedEntityInstanceModel.Columns.TRACKED_ENTITY_TYPE
                + " JOIN " + ProgramModel.TABLE + " ON " + TrackedEntityTypeModel.TABLE + "." + TrackedEntityTypeModel.Columns.UID + " = " + ProgramModel.TABLE + "." + ProgramModel.Columns.TRACKED_ENTITY_TYPE
                + " WHERE " + ProgramModel.TABLE + "." + ProgramModel.Columns.UID + " = '" + id + "'";

        final Set<String> PROGRAM_TRACKED_ENTITY_INSTANCE_TABLES = new HashSet<>(Arrays.asList(TrackedEntityInstanceModel.TABLE, TrackedEntityTypeModel.TABLE, ProgramModel.TABLE));

        return briteDatabase
                .createQuery(PROGRAM_TRACKED_ENTITY_INSTANCE_TABLES, PROGRAM_TRACKED_ENTITY_INSTANCE_QUERY)
                .mapToList(TrackedEntityInstanceModel::create);
    }

    @Override
    public Observable<CategoryOptionModel> getCategoryOptionWithId(String categoryOptionId) {
        String id = categoryOptionId == null ? "" : categoryOptionId;
        return briteDatabase
                .createQuery(CategoryOptionModel.TABLE, SELECT_CATEGORY_OPTION + "'" + id + "' LIMIT 1")
                .mapToOne(CategoryOptionModel::create);
    }

    @Override
    public Observable<CategoryOptionComboModel> getCategoryOptionComboWithId(String categoryOptionComboId) {
        String id = categoryOptionComboId == null ? "" : categoryOptionComboId;
        return briteDatabase
                .createQuery(CategoryOptionModel.TABLE, SELECT_CATEGORY_OPTION_COMBO + "'" + id + "' LIMIT 1")
                .mapToOne(CategoryOptionComboModel::create);
    }

    @Override
    public Observable<OrganisationUnitModel> getOrganisationUnit(String orgUnitUid) {
        String id = orgUnitUid == null ? "" : orgUnitUid;
        return briteDatabase
                .createQuery(OrganisationUnitModel.TABLE, ORG_UNIT_QUERY + "'" + id + "' LIMIT 1")
                .mapToOne(OrganisationUnitModel::create);
    }

    @Override
    public Observable<OrganisationUnitModel> getTeiOrgUnit(String teiUid) {
        return briteDatabase
                .createQuery(TEI_ORG_UNIT_TABLES, TEI_ORG_UNIT_QUERY, teiUid == null ? "" : teiUid)
                .mapToOne(OrganisationUnitModel::create);
    }

    @Override
    public Observable<OrganisationUnitModel> getTeiOrgUnit(@NonNull String teiUid, @Nullable String programUid) {
        if (programUid == null)
            return getTeiOrgUnit(teiUid);
        else
            return briteDatabase
                    .createQuery(TEI_ORG_UNIT_TABLES, ENROLLMENT_ORG_UNIT_QUERY, teiUid == null ? "" : teiUid, programUid)
                    .mapToOne(OrganisationUnitModel::create);
    }

    @Override
    public Observable<List<OrganisationUnitModel>> getOrgUnitForOpenAndClosedDate(String currentDate) {
        return briteDatabase
                .createQuery(OrganisationUnitModel.TABLE, ORG_UNIT_DATE_QUERY, currentDate == null ? "" : currentDate)
                .mapToList(OrganisationUnitModel::create);
    }

    @Override
    public Observable<List<ProgramTrackedEntityAttributeModel>> getProgramTrackedEntityAttributes(String programUid) {
        if (programUid != null)
            return briteDatabase
                    .createQuery(ProgramTrackedEntityAttributeModel.TABLE, PROGRAM_TRACKED_ENTITY_ATTRIBUTES_QUERY + "'" + programUid + "'")
                    .mapToList(ProgramTrackedEntityAttributeModel::create);
        else
            return briteDatabase
                    .createQuery(PROGRAM_TRACKED_ENTITY_ATTRIBUTES_NO_PROGRAM_TABLES, PROGRAM_TRACKED_ENTITY_ATTRIBUTES_NO_PROGRAM_QUERY)
                    .mapToList(ProgramTrackedEntityAttributeModel::create);
    }

    @Override
    public Observable<List<TrackedEntityAttributeValueModel>> getTEIAttributeValues(String uid) {
        return briteDatabase
                .createQuery(ATTR_VALUE_TABLES, PROGRAM_TRACKED_ENTITY_ATTRIBUTES_VALUES_QUERY, uid == null ? "" : uid)
                .mapToList(TrackedEntityAttributeValueModel::create);
    }

    @Override
    public Observable<List<TrackedEntityAttributeValueModel>> getTEIAttributeValues(String programUid, String teiUid) {
        return briteDatabase
                .createQuery(ATTR_PROGRAM_VALUE_TABLES, PROGRAM_TRACKED_ENTITY_ATTRIBUTES_VALUES_PROGRAM_QUERY, programUid == null ? "" : programUid, teiUid == null ? "" : teiUid)
                .mapToList(TrackedEntityAttributeValueModel::create);
    }

    @Override
    public Observable<TrackedEntityAttributeModel> getTrackedEntityAttribute(String teAttribute) {
        return briteDatabase
                .createQuery(TrackedEntityAttributeModel.TABLE, TE_ATTRIBUTE_QUERY, teAttribute == null ? "" : teAttribute)
                .mapToOne(TrackedEntityAttributeModel::create);
    }

    @Override
    public Observable<RelationshipTypeModel> getRelationshipType(String programID) {
        RelationshipTypeModel defaultRelationshipType = RelationshipTypeModel.builder()
                .aIsToB("...")
                .bIsToA("...")
                .build();
        String id = programID == null ? "" : programID;
        return briteDatabase
                .createQuery(RELATIONSHIP_TYPE_TABLES, RELATIONSHIP_TYPE_QUERY + "'" + id + "' LIMIT 1")
                .lift(Query.mapToOneOrDefault(RelationshipTypeModel::create, defaultRelationshipType));
    }

    @Override
    public Observable<List<RelationshipTypeModel>> getRelationshipTypeList() {
        return briteDatabase
                .createQuery(RELATIONSHIP_TYPE_TABLES, RELATIONSHIP_TYPE_LIST_QUERY)
                .mapToList(RelationshipTypeModel::create);
    }

    @NonNull
    @Override
    public Observable<ProgramStageModel> programStage(String programStageId) {
        String id = programStageId == null ? "" : programStageId;
        return briteDatabase
                .createQuery(ProgramStageModel.TABLE, SELECT_PROGRAM_STAGE + "'" + id + "' LIMIT 1")
                .mapToOne(ProgramStageModel::create);
    }

    @Override
    public Observable<DataElementModel> getDataElement(String dataElementUid) {
        String id = dataElementUid == null ? "" : dataElementUid;
        return briteDatabase
                .createQuery(DataElementModel.TABLE, DATA_ELEMENT_QUERY + "'" + id + "' LIMIT 1")
                .mapToOne(DataElementModel::create);
    }

    @Override
    public Observable<List<EnrollmentModel>> getTEIEnrollments(String teiUid) {
        String id = teiUid == null ? "" : teiUid;
        return briteDatabase
                .createQuery(EnrollmentModel.TABLE, SELECT_TEI_ENROLLMENTS + "'" + id + "'")
                .mapToList(EnrollmentModel::create);
    }

    @Override
    public Observable<List<ProgramModel>> getTEIProgramsToEnroll(String teiUid) {
        return briteDatabase
                .createQuery(ProgramModel.TABLE, SELECT_PROGRMAS_TO_ENROLL, teiUid == null ? "" : teiUid)
                .mapToList(ProgramModel::create);
    }

    @Override
    public Observable<EventModel> getEnrollmentLastEvent(String enrollmentUid) {
        return briteDatabase
                .createQuery(SELECT_ENROLLMENT_LAST_EVENT_TABLES, SELECT_ENROLLMENT_EVENTS, enrollmentUid == null ? "" : enrollmentUid)
                .mapToOne(EventModel::create);
    }

    @Override
    public Observable<List<EventModel>> getEnrollmentEvents(String enrollmentUid) {
        return briteDatabase
                .createQuery(SELECT_ENROLLMENT_LAST_EVENT_TABLES, SELECT_ENROLLMENT_LAST_EVENT, enrollmentUid == null ? "" : enrollmentUid)
                .mapToList(EventModel::create);
    }

    @Override
    public Observable<Integer> getProgramStageDataElementCount(String programStageId) {
        String SELECT_PROGRAM_STAGE_COUNT = "SELECT COUNT(*) FROM " + ProgramStageDataElementModel.TABLE +
                " WHERE " + ProgramStageDataElementModel.Columns.PROGRAM_STAGE + " = '%s' LIMIT 1";
        String id = programStageId == null ? "" : programStageId;
        return briteDatabase
                .createQuery(ProgramStageDataElementModel.TABLE, String.format(SELECT_PROGRAM_STAGE_COUNT, id))
                .mapToOne(cursor -> {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        return cursor.getInt(0);
                    } else
                        return 0;
                });
    }

    @Override
    public Observable<Integer> getTrackEntityDataValueCount(String eventId) {
        String SELECT_TRACKED_ENTITY_COUNT = "SELECT COUNT(*) FROM " + TrackedEntityDataValueModel.TABLE +
                " WHERE " + TrackedEntityDataValueModel.Columns.EVENT + " = '%s' LIMIT 1";
        String id = eventId == null ? "" : eventId;
        return briteDatabase
                .createQuery(TrackedEntityDataValueModel.TABLE, String.format(SELECT_TRACKED_ENTITY_COUNT, id))
                .mapToOne(cursor -> {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        return cursor.getInt(0);
                    } else
                        return 0;
                });
    }

    @Override
    public List<OptionModel> optionSet(String optionSetId) {
        String SELECT_OPTION_SET = "SELECT * FROM " + OptionModel.TABLE + " WHERE Option.optionSet = ?";
        Cursor cursor = briteDatabase.query(SELECT_OPTION_SET, optionSetId == null ? "" : optionSetId);
        List<OptionModel> options = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            for (int i = 0; i < cursor.getCount(); i++) {
                options.add(OptionModel.create(cursor));
                cursor.moveToNext();
            }
            cursor.close();
        }
        return options;
    }

    @Override
    public Observable<List<ProgramModel>> getProgramModelFromEnrollmentList(List<Enrollment> enrollments) {
        String query = "";
        for (Enrollment enrollment : enrollments) {
            String id = enrollment.program() == null ? "" : enrollment.program();
            query = query.concat(ProgramModel.TABLE + "." + ProgramModel.Columns.UID + " = '" + id + "'");
            if (!enrollment.program().equals(enrollments.get(enrollments.size() - 1).program()))
                query = query.concat(" OR ");
        }

        return briteDatabase
                .createQuery(ProgramModel.TABLE, PROGRAM_LIST_QUERY + query)
                .mapToList(ProgramModel::create);

    }

    @Override
    public Observable<List<ProgramModel>> getTeiActivePrograms(String teiUid) {
        return briteDatabase.createQuery(ACTIVE_TEI_PROGRAMS_TABLES, ACTIVE_TEI_PROGRAMS, teiUid == null ? "" : teiUid)
                .mapToList(ProgramModel::create);
    }

    @Override
    public Observable<ProgramModel> getProgramWithId(String programUid) {
        String id = programUid == null ? "" : programUid;
        return briteDatabase
                .createQuery(ProgramModel.TABLE, PROGRAM_LIST_ALL_QUERY + "'" + id + "' LIMIT 1")
                .mapToOne(ProgramModel::create);
    }

    @Override
    public Observable<ResourceModel> getLastSync(ResourceModel.Type resourceType) {
        String id = resourceType.name() == null ? "" : resourceType.name();
        return briteDatabase
                .createQuery(ResourceModel.TABLE, RESOURCES_QUERY, id)
                .mapToOne(ResourceModel::create);
    }

    @Override
    public Observable<Pair<String, Integer>> getTheme() {
        return briteDatabase
                .createQuery(SystemSettingModel.TABLE, "SELECT * FROM " + SystemSettingModel.TABLE)
                .mapToList(SystemSettingModel::create)
                .map(systemSettingModels -> {
                    String flag = "";
                    String style = "";
                    for (SystemSettingModel settingModel : systemSettingModels)
                        if (settingModel.key().equals("style"))
                            style = settingModel.value();
                        else
                            flag = settingModel.value();

                    if (style.contains("green"))
                        return Pair.create(flag, R.style.GreenTheme);
                    if (style.contains("india"))
                        return Pair.create(flag, R.style.OrangeTheme);
                    if (style.contains("myanmar"))
                        return Pair.create(flag, R.style.RedTheme);
                    else
                        return Pair.create(flag, R.style.AppTheme);
                });

    }

    @Override
    public Observable<ObjectStyleModel> getObjectStyle(String uid) {
        return briteDatabase.createQuery(ObjectStyleModel.TABLE, "SELECT * FROM ObjectStyle WHERE uid = ? LIMIT 1", uid == null ? "" : uid)
                .mapToOneOrDefault((ObjectStyleModel::create), ObjectStyleModel.builder().build());
    }

    @Override
    public Observable<List<OrganisationUnitModel>> getOrganisationUnits() {
        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, "SELECT * FROM OrganisationUnit")
                .mapToList(OrganisationUnitModel::create);
    }

    @Override
    public Observable<List<Pair<String, String>>> getReserveUids() {
        Cursor cursor = briteDatabase.query(RESERVED_UIDS, "1");
        List<Pair<String, String>> pairs = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            for (int i = 0; i < cursor.getCount(); i++) {
                pairs.add(Pair.create(cursor.getString(0), cursor.getString(1)));
                cursor.moveToNext();
            }
            cursor.close();
        }

        return Observable.just(pairs);
    }

    @Override
    public Observable<Boolean> hasOverdue(@Nullable String programUid, @NonNull String teiUid) {

        String overdueQuery = "SELECT * FROM EVENT JOIN Enrollment ON Enrollment.uid = Event.enrollment " +
                "JOIN TrackedEntityInstance ON TrackedEntityInstance.uid = Enrollment.trackedEntityInstance " +
                "WHERE TrackedEntityInstance.uid = ? AND Event.status = ?";

        String overdueProgram = " AND Enrollment.program = ?";

        if (programUid == null)
            return briteDatabase.createQuery(EventModel.TABLE, overdueQuery, teiUid == null ? "" : teiUid, EventStatus.SKIPPED.name()).mapToList(EventModel::create).map(list -> !list.isEmpty());
        else
            return briteDatabase.createQuery(EventModel.TABLE, overdueQuery + overdueProgram, teiUid == null ? "" : teiUid, EventStatus.SKIPPED.name(), programUid).mapToList(EventModel::create).map(list -> !list.isEmpty());

    }

    @Override
    public Observable<ProgramModel> getExpiryDateFromEvent(String eventUid) {
        return briteDatabase
                .createQuery(ProgramModel.TABLE, EXPIRY_DATE_PERIOD_QUERY, eventUid == null ? "" : eventUid)
                .mapToOne(ProgramModel::create);
    }

    @NonNull
    @Override
    public Observable<List<ResourceModel>> syncState(ProgramModel program) {
        String SYNC_STATE = "SELECT * FROM " + ResourceModel.TABLE;
        return briteDatabase
                .createQuery(ResourceModel.TABLE, SYNC_STATE)
                .mapToList(ResourceModel::create);
    }

    @Override
    public Flowable<Pair<Integer, Integer>> getDownloadedData() {
        String TEI_COUNT = "SELECT DISTINCT COUNT (uid) FROM TrackedEntityInstance WHERE TrackedEntityInstance.state != 'RELATIONSHIP'";
        String EVENT_COUNT = "SELECT DISTINCT COUNT (uid) FROM Event WHERE Event.enrollment IS NULL";

        int currentTei = 0;
        int currentEvent = 0;

        Cursor teiCursor = briteDatabase.query(TEI_COUNT);
        if (teiCursor != null && teiCursor.moveToFirst()) {
            currentTei = teiCursor.getInt(0);
            teiCursor.close();
        }

        Cursor eventCursor = briteDatabase.query(EVENT_COUNT);
        if (eventCursor != null && eventCursor.moveToFirst()) {
            currentEvent = eventCursor.getInt(0);
            eventCursor.close();
        }

        return Flowable.just(Pair.create(currentEvent, currentTei));

    }

    @Override
    public Flowable<Boolean> validateCredentials(String serverUrl, String username, String password) {
        return briteDatabase.createQuery(AuthenticatedUserModel.TABLE, "SELECT AuthenticatedUser.credentials, SystemInfo.contextPath FROM AuthenticatedUser JOIN SystemInfo LIMIT 1")
                .mapToOne(cursor -> {
                    String userCredentials = cursor.getString(0);
                    String currentServer = cursor.getString(1);
                    byte[] bytes = String.format("%s:%s", username, password).getBytes("UTF-8");
                    String encodedCredentials = Base64.encodeToString(bytes, Base64.DEFAULT);

                    return currentServer.equals(serverUrl) && userCredentials.equals(encodedCredentials);

                }).toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public Observable<String> getServerUrl() {
        return briteDatabase.createQuery(AuthenticatedUserModel.TABLE, "SELECT SystemInfo.contextPath FROM SystemInfo LIMIT 1")
                .mapToOne(cursor -> cursor.getString(0));
    }
}