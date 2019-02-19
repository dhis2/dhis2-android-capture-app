package org.dhis2.data.metadata;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Base64;

import com.squareup.sqlbrite2.BriteDatabase;
import com.squareup.sqlbrite2.SqlBrite.Query;

import org.dhis2.R;
import org.dhis2.data.tuples.Pair;
import org.dhis2.utils.DateUtils;
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
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.maintenance.D2ErrorTableInfo;
import org.hisp.dhis.android.core.option.OptionModel;
import org.hisp.dhis.android.core.option.OptionSetModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitProgramLinkModel;
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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;

import static android.text.TextUtils.isEmpty;
import static org.dhis2.data.database.SqlConstants.ALL;
import static org.dhis2.data.database.SqlConstants.AND;
import static org.dhis2.data.database.SqlConstants.ASC;
import static org.dhis2.data.database.SqlConstants.COMMA;
import static org.dhis2.data.database.SqlConstants.DESC;
import static org.dhis2.data.database.SqlConstants.EQUAL;
import static org.dhis2.data.database.SqlConstants.FROM;
import static org.dhis2.data.database.SqlConstants.GREAT_THAN;
import static org.dhis2.data.database.SqlConstants.GROUP_BY;
import static org.dhis2.data.database.SqlConstants.JOIN;
import static org.dhis2.data.database.SqlConstants.LESS_THAN;
import static org.dhis2.data.database.SqlConstants.LIMIT_1;
import static org.dhis2.data.database.SqlConstants.NOT_EQUAL;
import static org.dhis2.data.database.SqlConstants.ON;
import static org.dhis2.data.database.SqlConstants.ORDER_BY;
import static org.dhis2.data.database.SqlConstants.POINT;
import static org.dhis2.data.database.SqlConstants.QUESTION_MARK;
import static org.dhis2.data.database.SqlConstants.SELECT;
import static org.dhis2.data.database.SqlConstants.SELECT_DISTINCT;
import static org.dhis2.data.database.SqlConstants.TABLE_POINT_FIELD;
import static org.dhis2.data.database.SqlConstants.TABLE_POINT_FIELD_EQUALS;
import static org.dhis2.data.database.SqlConstants.TABLE_POINT_FIELD_NOT_EQUALS;
import static org.dhis2.data.database.SqlConstants.VARIABLE;
import static org.dhis2.data.database.SqlConstants.WHERE;


/**
 * QUADRAM. Created by ppajuelo on 04/12/2017.
 */

public class MetadataRepositoryImpl implements MetadataRepository {

    private static final String SELECT_PROGRMAS_TO_ENROLL = String.format(
            SELECT + ALL + FROM + VARIABLE + WHERE + TABLE_POINT_FIELD_EQUALS + QUESTION_MARK,
            ProgramModel.TABLE, ProgramModel.TABLE, ProgramModel.Columns.TRACKED_ENTITY_TYPE
    );

    private static final String SELECT_TEI_ENROLLMENTS = String.format(
            SELECT + ALL + FROM + VARIABLE + WHERE + TABLE_POINT_FIELD_EQUALS,
            EnrollmentModel.TABLE,
            EnrollmentModel.TABLE, EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE);

    private static final String SELECT_ENROLLMENT_EVENTS_QUERY = SELECT + ALL + FROM + VARIABLE +
            WHERE + TABLE_POINT_FIELD_NOT_EQUALS + VARIABLE +
            AND + TABLE_POINT_FIELD_EQUALS + QUESTION_MARK + LIMIT_1;

    private static final String SELECT_ENROLLMENT_EVENTS = String.format(
            SELECT_ENROLLMENT_EVENTS_QUERY,
            EventModel.TABLE,
            EventModel.TABLE,
            EventModel.Columns.STATE,
            State.TO_DELETE,
            EventModel.TABLE,
            EventModel.Columns.ENROLLMENT);

    private static final String SELECT_ENROLLMENT_LAST_EVENT = String.format(
            SELECT + VARIABLE + POINT + ALL + FROM + VARIABLE + JOIN + VARIABLE + ON + TABLE_POINT_FIELD_EQUALS + TABLE_POINT_FIELD
                    + WHERE + TABLE_POINT_FIELD_EQUALS + QUESTION_MARK
                    + ORDER_BY + TABLE_POINT_FIELD + AND + TABLE_POINT_FIELD_NOT_EQUALS + VARIABLE + DESC + LIMIT_1,
            EventModel.TABLE, EventModel.TABLE, EnrollmentModel.TABLE, EnrollmentModel.TABLE, EnrollmentModel.Columns.UID, EventModel.TABLE, EventModel.Columns.ENROLLMENT,
            EnrollmentModel.TABLE, EnrollmentModel.Columns.UID, EventModel.TABLE, EventModel.Columns.EVENT_DATE, EventModel.TABLE, EventModel.Columns.STATE, State.TO_DELETE
    );

    private static final Set<String> SELECT_ENROLLMENT_LAST_EVENT_TABLES = new HashSet<>(Arrays.asList(EventModel.TABLE, EnrollmentModel.TABLE));

    private static final String PROGRAM_LIST_QUERY = String.format(SELECT + ALL + FROM + VARIABLE + WHERE, ProgramModel.TABLE);

    private static final String ACTIVE_TEI_PROGRAMS = String.format(
            SELECT + VARIABLE + POINT + ALL + FROM + VARIABLE + JOIN + VARIABLE +
                    ON + TABLE_POINT_FIELD_EQUALS + TABLE_POINT_FIELD + WHERE + TABLE_POINT_FIELD_EQUALS + QUESTION_MARK,
            ProgramModel.TABLE,
            ProgramModel.TABLE,
            EnrollmentModel.TABLE, EnrollmentModel.TABLE, EnrollmentModel.Columns.PROGRAM, ProgramModel.TABLE, ProgramModel.Columns.UID,
            EnrollmentModel.TABLE, EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE);

    private static final Set<String> ACTIVE_TEI_PROGRAMS_TABLES = new HashSet<>(Arrays.asList(ProgramModel.TABLE, EnrollmentModel.TABLE));


    private static final String PROGRAM_LIST_ALL_QUERY = String.format(SELECT + ALL + FROM + VARIABLE + WHERE + TABLE_POINT_FIELD_EQUALS,
            ProgramModel.TABLE, ProgramModel.TABLE, ProgramModel.Columns.UID);

    private static final String TRACKED_ENTITY_QUERY = String.format(SELECT + ALL + FROM + VARIABLE + WHERE + TABLE_POINT_FIELD_EQUALS,
            TrackedEntityTypeModel.TABLE, TrackedEntityTypeModel.TABLE, TrackedEntityTypeModel.Columns.UID);

    private static final String TRACKED_ENTITY_INSTANCE_QUERY = String.format(SELECT + ALL + FROM + VARIABLE + WHERE + TABLE_POINT_FIELD_EQUALS,
            TrackedEntityInstanceModel.TABLE, TrackedEntityInstanceModel.TABLE, TrackedEntityInstanceModel.Columns.UID);

    private static final String ORG_UNIT_QUERY = String.format(SELECT + ALL + FROM + VARIABLE + WHERE + TABLE_POINT_FIELD_EQUALS,
            OrganisationUnitModel.TABLE, OrganisationUnitModel.TABLE, OrganisationUnitModel.Columns.UID);

    private static final String TEI_ORG_UNIT_QUERY = String.format(SELECT + ALL + FROM + VARIABLE +
                    JOIN + VARIABLE + ON + TABLE_POINT_FIELD_EQUALS + TABLE_POINT_FIELD +
                    WHERE + TABLE_POINT_FIELD_EQUALS + QUESTION_MARK + LIMIT_1,
            OrganisationUnitModel.TABLE,
            TrackedEntityInstanceModel.TABLE, TrackedEntityInstanceModel.TABLE, TrackedEntityInstanceModel.Columns.ORGANISATION_UNIT, OrganisationUnitModel.TABLE, OrganisationUnitModel.Columns.UID,
            TrackedEntityInstanceModel.TABLE, TrackedEntityInstanceModel.Columns.UID);

    private static final String ENROLLMENT_ORG_UNIT_QUERY =
            SELECT + OrganisationUnitModel.TABLE + POINT + ALL + FROM + OrganisationUnitModel.TABLE +
                    WHERE + OrganisationUnitModel.TABLE + POINT + OrganisationUnitModel.Columns.UID +
                    " IN (SELECT Enrollment.organisationUnit FROM Enrollment " +
                    "JOIN Program ON Program.uid = Enrollment.program WHERE Enrollment.trackedEntityInstance = ? AND Program.uid = ? LIMIT 1)";


    private static final Set<String> TEI_ORG_UNIT_TABLES = new HashSet<>(Arrays.asList(
            OrganisationUnitModel.TABLE, TrackedEntityInstanceModel.TABLE));

    private static final String ORG_UNIT_DATE_QUERY = String.format(
            SELECT + ALL + FROM + VARIABLE + WHERE + TABLE_POINT_FIELD + LESS_THAN + QUESTION_MARK +
                    AND + TABLE_POINT_FIELD + GREAT_THAN + QUESTION_MARK,
            OrganisationUnitModel.TABLE,
            OrganisationUnitModel.TABLE, OrganisationUnitModel.Columns.OPENING_DATE, OrganisationUnitModel.TABLE, OrganisationUnitModel.Columns.CLOSED_DATE
    );

    private static final String PROGRAM_TRACKED_ENTITY_ATTRIBUTES_QUERY = String.format(SELECT + ALL + FROM + VARIABLE +
                    WHERE + TABLE_POINT_FIELD_EQUALS,
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.PROGRAM);

    private static final String PROGRAM_TRACKED_ENTITY_ATTRIBUTES_NO_PROGRAM_QUERY = String.format(
            SELECT_DISTINCT + VARIABLE + POINT + ALL + FROM + VARIABLE +
                    JOIN + VARIABLE + ON + TABLE_POINT_FIELD_EQUALS + TABLE_POINT_FIELD +
                    WHERE + TABLE_POINT_FIELD_EQUALS + '1' + GROUP_BY + TABLE_POINT_FIELD,
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.TABLE,
            TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.Columns.UID, ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.TRACKED_ENTITY_ATTRIBUTE,
            TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.Columns.DISPLAY_IN_LIST_NO_PROGRAM, TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.Columns.UID);

    private static final Set<String> PROGRAM_TRACKED_ENTITY_ATTRIBUTES_NO_PROGRAM_TABLES = new HashSet<>(Arrays.asList(
            TrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.TABLE));

    private static final String PROGRAM_TRACKED_ENTITY_ATTRIBUTES_VALUES_QUERY = String.format(
            SELECT + VARIABLE + POINT + ALL + FROM + VARIABLE +
                    JOIN + VARIABLE + ON + TABLE_POINT_FIELD_EQUALS + TABLE_POINT_FIELD +
                    WHERE + TABLE_POINT_FIELD_EQUALS + QUESTION_MARK +
                    AND + TABLE_POINT_FIELD + NOT_EQUAL + '0' +
                    ORDER_BY + TABLE_POINT_FIELD + ASC,

            TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.TABLE,
            TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.Columns.UID,
            TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_ATTRIBUTE,
            TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_INSTANCE,
            TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.Columns.SORT_ORDER_IN_LIST_NO_PROGRAM,
            TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.Columns.SORT_ORDER_IN_LIST_NO_PROGRAM
    );

    private static final Set<String> ATTR_VALUE_TABLES = new HashSet<>(Arrays.asList(TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeModel.TABLE));

    private static final String PROGRAM_TRACKED_ENTITY_ATTRIBUTES_VALUES_PROGRAM_QUERY = String.format(
            SELECT + VARIABLE + POINT + ALL + FROM + VARIABLE +
                    JOIN + VARIABLE + ON + TABLE_POINT_FIELD_EQUALS + TABLE_POINT_FIELD +
                    WHERE + TABLE_POINT_FIELD_EQUALS + QUESTION_MARK +
                    AND + TABLE_POINT_FIELD_EQUALS + QUESTION_MARK +
                    AND + TABLE_POINT_FIELD_EQUALS + '1' +
                    ORDER_BY + TABLE_POINT_FIELD + ASC,
            TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.TABLE,
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.TRACKED_ENTITY_ATTRIBUTE, TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_ATTRIBUTE,
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.PROGRAM,
            TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_INSTANCE,
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.DISPLAY_IN_LIST,
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.SORT_ORDER);

    private static final Set<String> ATTR_PROGRAM_VALUE_TABLES = new HashSet<>(Arrays.asList(TrackedEntityAttributeValueModel.TABLE,
            ProgramTrackedEntityAttributeModel.TABLE));

    private static final String TE_ATTRIBUTE_QUERY = String.format(
            SELECT + ALL + FROM + VARIABLE + WHERE + TABLE_POINT_FIELD_EQUALS + QUESTION_MARK + LIMIT_1,
            TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.Columns.UID);

    private static final String RELATIONSHIP_TYPE_QUERY = String.format(
            SELECT + VARIABLE + POINT + ALL + FROM + VARIABLE +
                    JOIN + VARIABLE + ON + TABLE_POINT_FIELD_EQUALS + TABLE_POINT_FIELD +
                    WHERE + TABLE_POINT_FIELD_EQUALS,
            RelationshipTypeModel.TABLE, RelationshipTypeModel.TABLE,
            ProgramModel.TABLE, ProgramModel.TABLE, ProgramModel.Columns.RELATIONSHIP_TYPE, RelationshipTypeModel.TABLE, RelationshipTypeModel.Columns.UID,
            ProgramModel.TABLE, ProgramModel.Columns.UID);

    private static final Set<String> RELATIONSHIP_TYPE_TABLES = new HashSet<>(Arrays.asList(RelationshipTypeModel.TABLE, ProgramModel.TABLE));

    private static final String RELATIONSHIP_TYPE_LIST_QUERY = String.format(SELECT + ALL + FROM + VARIABLE,
            RelationshipTypeModel.TABLE);

    private static final String DATA_ELEMENT_QUERY = String.format(SELECT + ALL + FROM + VARIABLE
                    + WHERE + TABLE_POINT_FIELD_EQUALS,
            DataElementModel.TABLE, DataElementModel.TABLE, DataElementModel.Columns.UID);


    private static final String SELECT_PROGRAM_STAGE = String.format(SELECT + ALL + FROM + VARIABLE + WHERE + TABLE_POINT_FIELD_EQUALS,
            ProgramStageModel.TABLE, ProgramStageModel.TABLE, ProgramStageModel.Columns.UID);

    private static final String SELECT_CATEGORY_OPTION = String.format(SELECT + ALL + FROM + VARIABLE + WHERE + TABLE_POINT_FIELD_EQUALS,
            CategoryOptionModel.TABLE, CategoryOptionModel.TABLE, CategoryOptionModel.Columns.UID);

    private static final String SELECT_CATEGORY_OPTION_COMBO = String.format(SELECT + ALL + FROM + VARIABLE + WHERE + TABLE_POINT_FIELD_EQUALS,
            CategoryOptionComboModel.TABLE, CategoryOptionComboModel.TABLE, CategoryOptionComboModel.Columns.UID);

    private static final String SELECT_CATEGORY_OPTIONS_COMBO = String.format(SELECT + ALL + FROM + VARIABLE + WHERE + TABLE_POINT_FIELD_EQUALS,
            CategoryOptionComboModel.TABLE, CategoryOptionComboModel.TABLE, CategoryOptionComboModel.Columns.CATEGORY_COMBO);


    private static final String SELECT_CATEGORY_COMBO = String.format(SELECT + ALL + FROM + VARIABLE + WHERE + TABLE_POINT_FIELD_EQUALS,
            CategoryComboModel.TABLE, CategoryComboModel.TABLE, CategoryComboModel.Columns.UID);

    private static final String SELECT_DEFAULT_CAT_COMBO = String.format(SELECT + ALL + FROM + VARIABLE + WHERE + TABLE_POINT_FIELD_EQUALS + "1",
            CategoryComboModel.TABLE, CategoryComboModel.TABLE, CategoryComboModel.Columns.IS_DEFAULT);


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

    private static final String RESERVED_UIDS = SELECT_DISTINCT +
            ProgramTrackedEntityAttributeModel.TABLE + POINT + ProgramTrackedEntityAttributeModel.Columns.TRACKED_ENTITY_ATTRIBUTE + COMMA +
            OrganisationUnitProgramLinkModel.TABLE + POINT + OrganisationUnitProgramLinkModel.Columns.ORGANISATION_UNIT + COMMA +
            FROM + ProgramTrackedEntityAttributeModel.TABLE +
            JOIN + TrackedEntityAttributeModel.TABLE +
            ON + TrackedEntityAttributeModel.TABLE + POINT + TrackedEntityAttributeModel.Columns.UID +
            EQUAL + ProgramTrackedEntityAttributeModel.TABLE + POINT + ProgramTrackedEntityAttributeModel.Columns.TRACKED_ENTITY_ATTRIBUTE +
            JOIN + OrganisationUnitProgramLinkModel.TABLE +
            ON + OrganisationUnitProgramLinkModel.TABLE + POINT + OrganisationUnitProgramLinkModel.Columns.PROGRAM +
            EQUAL + ProgramTrackedEntityAttributeModel.TABLE + POINT + ProgramTrackedEntityAttributeModel.Columns.PROGRAM +
            WHERE + TrackedEntityAttributeModel.TABLE + POINT + TrackedEntityAttributeModel.Columns.GENERATED +
            EQUAL + QUESTION_MARK;

    private static final Set<String> PROGRAM_TRACKED_ENTITY_INSTANCE_TABLES = new HashSet<>(Arrays.asList(
            TrackedEntityInstanceModel.TABLE, TrackedEntityTypeModel.TABLE, ProgramModel.TABLE));

    private static final String SELECT_PROGRAM_STAGE_COUNT = SELECT + "COUNT(*)" + FROM + ProgramStageDataElementModel.TABLE +
            WHERE + ProgramStageDataElementModel.Columns.PROGRAM_STAGE + EQUAL + "'" + VARIABLE + "'" + LIMIT_1;

    private final BriteDatabase briteDatabase;

    MetadataRepositoryImpl(@NonNull BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }

    @Override
    public Observable<TrackedEntityTypeModel> getTrackedEntity(String trackedEntityUid) {
        String id = trackedEntityUid == null ? "" : trackedEntityUid;
        return briteDatabase
                .createQuery(TrackedEntityTypeModel.TABLE, TRACKED_ENTITY_QUERY + "'" + id + "'" + LIMIT_1)
                .mapToOne(TrackedEntityTypeModel::create);
    }

    @Override
    public Observable<CategoryComboModel> getCategoryComboWithId(String categoryComboId) {
        String id = categoryComboId == null ? "" : categoryComboId;
        return briteDatabase
                .createQuery(CategoryComboModel.TABLE, SELECT_CATEGORY_COMBO + "'" + id + "'" + LIMIT_1)
                .mapToOne(CategoryComboModel::create);
    }

    @Override
    public Observable<String> getDefaultCategoryOptionId() {
        return briteDatabase
                .createQuery(CategoryComboModel.TABLE, SELECT_DEFAULT_CAT_COMBO)
                .mapToOne(cursor -> cursor.getString(0));
    }

    public Observable<TrackedEntityInstanceModel> getTrackedEntityInstance(String teiUid) {
        String id = teiUid == null ? "" : teiUid;
        return briteDatabase
                .createQuery(TrackedEntityInstanceModel.TABLE, TRACKED_ENTITY_INSTANCE_QUERY + "'" + id + "'" + LIMIT_1)
                .mapToOne(TrackedEntityInstanceModel::create);
    }

    @Override
    public Observable<List<TrackedEntityInstanceModel>> getTrackedEntityInstances(String programUid) {
        String id = programUid == null ? "" : programUid;
        String programTrackedEntityInstanceQuery = SELECT + ALL + FROM + TrackedEntityInstanceModel.TABLE
                + JOIN + TrackedEntityTypeModel.TABLE +
                ON + TrackedEntityTypeModel.TABLE + POINT + TrackedEntityTypeModel.Columns.UID +
                EQUAL + TrackedEntityInstanceModel.TABLE + POINT + TrackedEntityInstanceModel.Columns.TRACKED_ENTITY_TYPE
                + JOIN + ProgramModel.TABLE +
                ON + TrackedEntityTypeModel.TABLE + POINT + TrackedEntityTypeModel.Columns.UID +
                EQUAL + ProgramModel.TABLE + POINT + ProgramModel.Columns.TRACKED_ENTITY_TYPE
                + WHERE + ProgramModel.TABLE + POINT + ProgramModel.Columns.UID + EQUAL + "'" + id + "'";

        return briteDatabase
                .createQuery(PROGRAM_TRACKED_ENTITY_INSTANCE_TABLES, programTrackedEntityInstanceQuery)
                .mapToList(TrackedEntityInstanceModel::create);
    }

    @Override
    public Observable<CategoryOptionModel> getCategoryOptionWithId(String categoryOptionId) {
        String id = categoryOptionId == null ? "" : categoryOptionId;
        return briteDatabase
                .createQuery(CategoryOptionModel.TABLE, SELECT_CATEGORY_OPTION + "'" + id + "'" + LIMIT_1)
                .mapToOne(CategoryOptionModel::create);
    }

    @Override
    public Observable<CategoryOptionComboModel> getCategoryOptionComboWithId(String categoryOptionComboId) {
        String id = categoryOptionComboId == null ? "" : categoryOptionComboId;
        return briteDatabase
                .createQuery(CategoryOptionModel.TABLE, SELECT_CATEGORY_OPTION_COMBO + "'" + id + "'" + LIMIT_1)
                .mapToOne(CategoryOptionComboModel::create);
    }


    @Override
    public Observable<List<CategoryOptionComboModel>> getCategoryComboOptions(String categoryComboId) {
        String id = categoryComboId == null ? "" : categoryComboId;
        return briteDatabase
                .createQuery(CategoryOptionModel.TABLE, SELECT_CATEGORY_OPTIONS_COMBO + "'" + id + "'")
                .mapToList(CategoryOptionComboModel::create);
    }

    @Override
    public void saveCatOption(String eventUid, CategoryOptionComboModel selectedOption) {
        ContentValues event = new ContentValues();
        event.put(EventModel.Columns.ATTRIBUTE_OPTION_COMBO, selectedOption.uid());
        briteDatabase.update(EventModel.TABLE, event, EventModel.Columns.UID + EQUAL + QUESTION_MARK, eventUid == null ? "" : eventUid);
    }

    @Override
    public Observable<OrganisationUnitModel> getOrganisationUnit(String orgUnitUid) {
        String id = orgUnitUid == null ? "" : orgUnitUid;
        return briteDatabase
                .createQuery(OrganisationUnitModel.TABLE, ORG_UNIT_QUERY + "'" + id + "'" + LIMIT_1)
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
                    .createQuery(TEI_ORG_UNIT_TABLES, ENROLLMENT_ORG_UNIT_QUERY, teiUid, programUid)
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
                .createQuery(RELATIONSHIP_TYPE_TABLES, RELATIONSHIP_TYPE_QUERY + "'" + id + "'" + LIMIT_1)
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
                .createQuery(ProgramStageModel.TABLE, SELECT_PROGRAM_STAGE + "'" + id + "'" + LIMIT_1)
                .mapToOne(ProgramStageModel::create);
    }

    @Override
    public Observable<DataElementModel> getDataElement(String dataElementUid) {
        String id = dataElementUid == null ? "" : dataElementUid;
        return briteDatabase
                .createQuery(DataElementModel.TABLE, DATA_ELEMENT_QUERY + "'" + id + "'" + LIMIT_1)
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
        String selectTrackedEntityCount = SELECT + "COUNT(*)" + FROM + TrackedEntityDataValueModel.TABLE +
                WHERE + TrackedEntityDataValueModel.Columns.EVENT + EQUAL + "'" + VARIABLE + "'" + LIMIT_1;
        String id = eventId == null ? "" : eventId;
        return briteDatabase
                .createQuery(TrackedEntityDataValueModel.TABLE, String.format(selectTrackedEntityCount, id))
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
        String selectOptionSet = SELECT + ALL + FROM + OptionModel.TABLE +
                WHERE + OptionModel.TABLE + POINT + OptionModel.Columns.OPTION_SET +
                EQUAL + QUESTION_MARK;
        Cursor cursor = briteDatabase.query(selectOptionSet, optionSetId == null ? "" : optionSetId);
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
    public int optionSetSize(String optionSetUid) {
        String selectOptionSet = SELECT + "COUNT(" + OptionModel.TABLE + POINT + OptionModel.Columns.UID + ")" +
                FROM + OptionModel.TABLE +
                WHERE + OptionModel.TABLE + POINT + OptionModel.Columns.OPTION_SET +
                EQUAL + QUESTION_MARK;
        Cursor cursor = briteDatabase.query(selectOptionSet, optionSetUid == null ? "" : optionSetUid);
        int numberOfOptions = 0;
        if (cursor != null && cursor.moveToFirst()) {
            numberOfOptions = cursor.getInt(0);
            cursor.close();
        }
        return numberOfOptions;
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
                .createQuery(SystemSettingModel.TABLE, SELECT + ALL + FROM + SystemSettingModel.TABLE)
                .mapToList(SystemSettingModel::create)
                .map(systemSettingModels -> {
                    String flag = "";
                    String style = "";
                    for (SystemSettingModel settingModel : systemSettingModels) {
                        if ("style".equals(settingModel.key()))
                            style = settingModel.value();
                        else
                            flag = settingModel.value() != null ? settingModel.value() : "";
                    }

                    return parseStyle(style, flag);
                });

    }

    private Pair<String, Integer> parseStyle(String style, String flag) {
        if (style != null && flag != null) {
            if (style.contains("green"))
                return Pair.create(flag, R.style.GreenTheme);
            if (style.contains("india"))
                return Pair.create(flag, R.style.OrangeTheme);
            if (style.contains("myanmar"))
                return Pair.create(flag, R.style.RedTheme);
        }

        if (flag == null)
            flag = "";

        return Pair.create(flag, R.style.AppTheme);
    }

    @Override
    public Observable<ObjectStyleModel> getObjectStyle(String uid) {
        return briteDatabase.createQuery(ObjectStyleModel.TABLE,
                SELECT + ALL + FROM + ObjectStyleModel.TABLE +
                        WHERE + ObjectStyleModel.Columns.UID + EQUAL + QUESTION_MARK + LIMIT_1,
                uid == null ? "" : uid)
                .mapToOneOrDefault((ObjectStyleModel::create), ObjectStyleModel.builder().build());
    }

    @Override
    public Observable<List<OrganisationUnitModel>> getOrganisationUnits() {
        return briteDatabase.createQuery(OrganisationUnitModel.TABLE,
                SELECT + ALL + FROM + OrganisationUnitModel.TABLE)
                .mapToList(OrganisationUnitModel::create);
    }

    @Override
    public Observable<List<OrganisationUnitModel>> getSearchOrganisationUnits() {
        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, SELECT + ALL + FROM + OrganisationUnitModel.TABLE +
                WHERE + OrganisationUnitModel.Columns.UID + "iN (SELECT UserOrganisationUnit.organisationUnit FROM UserOrganisationUnit " +
                "WHERE UserOrganisationUnit.organisationUnitScope = 'SCOPE_TEI_SEARCH')")
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
            return briteDatabase.createQuery(EventModel.TABLE, overdueQuery, teiUid, EventStatus.SKIPPED.name()).mapToList(EventModel::create).map(list -> !list.isEmpty());
        else
            return briteDatabase.createQuery(EventModel.TABLE, overdueQuery + overdueProgram, teiUid, EventStatus.SKIPPED.name(), programUid).mapToList(EventModel::create).map(list -> !list.isEmpty());

    }

    @Override
    public Observable<ProgramModel> getExpiryDateFromEvent(String eventUid) {
        return briteDatabase
                .createQuery(ProgramModel.TABLE, EXPIRY_DATE_PERIOD_QUERY, eventUid == null ? "" : eventUid)
                .mapToOne(ProgramModel::create);
    }

    @Override
    public Observable<Boolean> isCompletedEventExpired(String eventUid) {
        return Observable.zip(briteDatabase.createQuery(EventModel.TABLE, SELECT + ALL + FROM + EventModel.TABLE +
                        WHERE + EventModel.Columns.UID + EQUAL + QUESTION_MARK, eventUid)
                        .mapToOne(EventModel::create),
                getExpiryDateFromEvent(eventUid),
                ((eventModel, programModel) ->
                        DateUtils.getInstance().isEventExpired(null, eventModel.completedDate(), programModel.completeEventsExpiryDays())));
    }

    @NonNull
    @Override
    public Observable<List<ResourceModel>> syncState(ProgramModel program) {
        String syncState = SELECT + ALL + FROM + ResourceModel.TABLE;
        return briteDatabase
                .createQuery(ResourceModel.TABLE, syncState)
                .mapToList(ResourceModel::create);
    }

    @Override
    public Flowable<Pair<Integer, Integer>> getDownloadedData() {
        String teiCount = SELECT_DISTINCT + "COUNT (" + TrackedEntityInstanceModel.Columns.UID + ")" +
                FROM + TrackedEntityInstanceModel.TABLE +
                WHERE + TrackedEntityInstanceModel.TABLE + POINT + TrackedEntityInstanceModel.Columns.STATE +
                NOT_EQUAL + State.RELATIONSHIP;
        String eventCount = SELECT_DISTINCT + "COUNT (" + EventModel.Columns.UID + ")" +
                FROM + EventModel.TABLE +
                WHERE + EventModel.TABLE + POINT + EventModel.Columns.ENROLLMENT + " IS NULL";

        int currentTei = 0;
        int currentEvent = 0;

        Cursor teiCursor = briteDatabase.query(teiCount);
        if (teiCursor != null && teiCursor.moveToFirst()) {
            currentTei = teiCursor.getInt(0);
            teiCursor.close();
        }

        Cursor eventCursor = briteDatabase.query(eventCount);
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
                    byte[] bytes = String.format("%s:%s", username, password).getBytes(StandardCharsets.UTF_8);
                    String encodedCredentials = Base64.encodeToString(bytes, Base64.DEFAULT);

                    return currentServer.equals(serverUrl) && userCredentials.equals(encodedCredentials);

                }).toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public Observable<String> getServerUrl() {
        return briteDatabase.createQuery(AuthenticatedUserModel.TABLE, "SELECT SystemInfo.contextPath FROM SystemInfo LIMIT 1")
                .mapToOne(cursor -> cursor.getString(0));
    }

    @Override
    public Observable<Integer> getOrgUnitsForDataElementsCount() {
        String sqlQuery = "SELECT COUNT(*) FROM (SELECT DISTINCT t.uid, o.organisationUnit " +
                "FROM TrackedEntityAttribute t, OrganisationUnitProgramLink o, ProgramTrackedEntityAttribute p " +
                "WHERE t.generated = 1 AND p.trackedEntityAttribute = t.uid AND p.program = o.program)";
        return briteDatabase.createQuery(AuthenticatedUserModel.TABLE, sqlQuery)
                .mapToOne(cursor -> {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        return cursor.getInt(0);
                    } else
                        return 0;
                });
    }

    @Override
    public Observable<List<D2Error>> getSyncErrors() {
        return briteDatabase.createQuery(D2ErrorTableInfo.TABLE_INFO.name(), "SELECT * FROM D2Error ORDER BY created DESC")
                .mapToList(D2Error::create);
    }

    @Override
    public Observable<List<String>> searchOptions(String text, String idOptionSet, int page) {
        String pageQuery = String.format(Locale.US, " LIMIT %d,%d", page * 15, 15);

        String optionQuery = !isEmpty(text) ?
                "select Option.displayName from OptionSet " +
                        "JOIN Option ON Option.optionSet = OptionSet.uid " +
                        "where OptionSet.uid = ? and Option.displayName like '%" + text + "%' " + pageQuery :
                "select Option.displayName from OptionSet " +
                        "JOIN Option ON Option.optionSet = OptionSet.uid " +
                        "where OptionSet.uid = ? " + pageQuery;

        return briteDatabase.createQuery(OptionSetModel.TABLE, optionQuery, idOptionSet)
                .mapToList(cursor -> cursor.getString(0));

    }


}