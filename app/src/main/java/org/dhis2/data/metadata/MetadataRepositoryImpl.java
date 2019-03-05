package org.dhis2.data.metadata;

import android.content.ContentValues;
import android.database.Cursor;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.R;
import org.dhis2.data.tuples.Pair;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionModel;
import org.hisp.dhis.android.core.common.ObjectStyleModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.maintenance.D2ErrorTableInfo;
import org.hisp.dhis.android.core.option.OptionModel;
import org.hisp.dhis.android.core.option.OptionSetModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute;
import org.hisp.dhis.android.core.resource.Resource;
import org.hisp.dhis.android.core.settings.SystemSetting;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityTypeModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;
import static org.dhis2.data.database.SqlConstants.ALL;
import static org.dhis2.data.database.SqlConstants.AUTH_USER_TABLE;
import static org.dhis2.data.database.SqlConstants.FROM;
import static org.dhis2.data.database.SqlConstants.JOIN;
import static org.dhis2.data.database.SqlConstants.LIMIT_1;
import static org.dhis2.data.database.SqlConstants.ON;
import static org.dhis2.data.database.SqlConstants.POINT;
import static org.dhis2.data.database.SqlConstants.PROGRAM_STAGE_TABLE;
import static org.dhis2.data.database.SqlConstants.PROGRAM_STAGE_UID;
import static org.dhis2.data.database.SqlConstants.PROGRAM_TE_ATTR_PROGRAM;
import static org.dhis2.data.database.SqlConstants.PROGRAM_TE_ATTR_TABLE;
import static org.dhis2.data.database.SqlConstants.PROGRAM_TE_ATTR_TRACKED_ENTITY_ATTRIBUTE;
import static org.dhis2.data.database.SqlConstants.QUESTION_MARK;
import static org.dhis2.data.database.SqlConstants.QUOTE;
import static org.dhis2.data.database.SqlConstants.RESOURCE_TABLE;
import static org.dhis2.data.database.SqlConstants.SELECT;
import static org.dhis2.data.database.SqlConstants.SYSTEM_SETTING_TABLE;
import static org.dhis2.data.database.SqlConstants.TABLE_POINT_FIELD;
import static org.dhis2.data.database.SqlConstants.TABLE_POINT_FIELD_EQUALS;
import static org.dhis2.data.database.SqlConstants.TE_ATTR_DISPLAY_IN_LIST_NO_PROGRAM;
import static org.dhis2.data.database.SqlConstants.TE_ATTR_TABLE;
import static org.dhis2.data.database.SqlConstants.TE_ATTR_UID;
import static org.dhis2.data.database.SqlConstants.VARIABLE;
import static org.dhis2.data.database.SqlConstants.WHERE;


/**
 * QUADRAM. Created by ppajuelo on 04/12/2017.
 */

public class MetadataRepositoryImpl implements MetadataRepository {

    private static final String SELECT_TEI_ENROLLMENTS = String.format(
            SELECT + ALL + FROM + VARIABLE + WHERE + TABLE_POINT_FIELD_EQUALS,
            EnrollmentModel.TABLE,
            EnrollmentModel.TABLE, EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE);

    private static final String ACTIVE_TEI_PROGRAMS = String.format(
            SELECT + VARIABLE + POINT + ALL + FROM + VARIABLE +
                    JOIN + VARIABLE + ON + TABLE_POINT_FIELD_EQUALS + TABLE_POINT_FIELD +
                    WHERE + TABLE_POINT_FIELD_EQUALS + QUESTION_MARK,
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

    private static final String TEI_ORG_UNIT_QUERY = String.format(
            SELECT + ALL + FROM + VARIABLE +
                    JOIN + VARIABLE + ON + TABLE_POINT_FIELD_EQUALS + TABLE_POINT_FIELD +
                    WHERE + TABLE_POINT_FIELD_EQUALS + QUESTION_MARK + LIMIT_1,
            OrganisationUnitModel.TABLE,
            TrackedEntityInstanceModel.TABLE, TrackedEntityInstanceModel.TABLE, TrackedEntityInstanceModel.Columns.ORGANISATION_UNIT, OrganisationUnitModel.TABLE, OrganisationUnitModel.Columns.UID,
            TrackedEntityInstanceModel.TABLE, TrackedEntityInstanceModel.Columns.UID);

    private static final String ENROLLMENT_ORG_UNIT_QUERY =
            "SELECT OrganisationUnit.* FROM OrganisationUnit " +
                    "WHERE OrganisationUnit.uid IN (" +
                    "SELECT Enrollment.organisationUnit FROM Enrollment " +
                    "JOIN Program ON Program.uid = Enrollment.program WHERE Enrollment.trackedEntityInstance = ? AND Program.uid = ?" + LIMIT_1 + ")";


    private static final Set<String> TEI_ORG_UNIT_TABLES = new HashSet<>(Arrays.asList(OrganisationUnitModel.TABLE, TrackedEntityInstanceModel.TABLE));

    private static final String PROGRAM_TRACKED_ENTITY_ATTRIBUTES_QUERY = String.format("SELECT * FROM %s WHERE %s.%s = ",
            PROGRAM_TE_ATTR_TABLE, PROGRAM_TE_ATTR_TABLE, PROGRAM_TE_ATTR_PROGRAM);

    private static final String PROGRAM_TRACKED_ENTITY_ATTRIBUTES_NO_PROGRAM_QUERY = String.format(
            "SELECT DISTINCT %s.* FROM %s " +
                    JOIN + VARIABLE + ON + TABLE_POINT_FIELD_EQUALS + TABLE_POINT_FIELD +
                    "WHERE %s.%s = '1' GROUP BY %s.%s",
            PROGRAM_TE_ATTR_TABLE, PROGRAM_TE_ATTR_TABLE,
            TE_ATTR_TABLE, TE_ATTR_TABLE, TE_ATTR_UID, PROGRAM_TE_ATTR_TABLE,
            PROGRAM_TE_ATTR_TRACKED_ENTITY_ATTRIBUTE,
            TE_ATTR_TABLE, TE_ATTR_DISPLAY_IN_LIST_NO_PROGRAM, TE_ATTR_TABLE, TE_ATTR_UID);

    private static final Set<String> PROGRAM_TRACKED_ENTITY_ATTRIBUTES_NO_PROGRAM_TABLES = new HashSet<>(Arrays.asList(TE_ATTR_TABLE,
            PROGRAM_TE_ATTR_TABLE));

    private static final String SELECT_PROGRAM_STAGE = String.format(SELECT + ALL + FROM + VARIABLE + WHERE + TABLE_POINT_FIELD_EQUALS,
            PROGRAM_STAGE_TABLE, PROGRAM_STAGE_TABLE, PROGRAM_STAGE_UID);

    private static final String SELECT_CATEGORY_OPTION_COMBO = String.format(SELECT + ALL + FROM + VARIABLE + WHERE + TABLE_POINT_FIELD_EQUALS,
            CategoryOptionComboModel.TABLE, CategoryOptionComboModel.TABLE, CategoryOptionComboModel.Columns.UID);

    private static final String SELECT_CATEGORY_OPTIONS_COMBO = String.format(SELECT + ALL + FROM + VARIABLE + WHERE + TABLE_POINT_FIELD_EQUALS,
            CategoryOptionComboModel.TABLE, CategoryOptionComboModel.TABLE, CategoryOptionComboModel.Columns.CATEGORY_COMBO);


    private static final String SELECT_CATEGORY_COMBO = String.format(SELECT + ALL + FROM + VARIABLE + WHERE + TABLE_POINT_FIELD_EQUALS,
            CategoryComboModel.TABLE, CategoryComboModel.TABLE, CategoryComboModel.Columns.UID);

    private static final String SELECT_DEFAULT_CAT_COMBO = String.format(SELECT + VARIABLE + FROM + VARIABLE + WHERE + TABLE_POINT_FIELD_EQUALS +
                    QUOTE + "1" + QUOTE + LIMIT_1,
            CategoryComboModel.Columns.UID, CategoryComboModel.TABLE, CategoryComboModel.TABLE, CategoryComboModel.Columns.IS_DEFAULT);

    private static final String EXPIRY_DATE_PERIOD_QUERY = String.format(
            SELECT + "program.* FROM %s " +
                    JOIN + VARIABLE + ON + TABLE_POINT_FIELD_EQUALS + TABLE_POINT_FIELD +
                    "WHERE %s.%s = ? " +
                    LIMIT_1,
            ProgramModel.TABLE,
            EventModel.TABLE, ProgramModel.TABLE, ProgramModel.Columns.UID, EventModel.TABLE, EventModel.Columns.PROGRAM,
            EventModel.TABLE, EventModel.Columns.UID);

    private BriteDatabase briteDatabase;

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
        briteDatabase.update(EventModel.TABLE, event, EventModel.Columns.UID + " = ?", eventUid == null ? "" : eventUid);
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
    public Observable<List<ProgramTrackedEntityAttribute>> getProgramTrackedEntityAttributes(String programUid) {
        if (programUid != null)
            return briteDatabase
                    .createQuery(PROGRAM_TE_ATTR_TABLE, PROGRAM_TRACKED_ENTITY_ATTRIBUTES_QUERY + "'" + programUid + "'")
                    .mapToList(ProgramTrackedEntityAttribute::create);
        else
            return briteDatabase
                    .createQuery(PROGRAM_TRACKED_ENTITY_ATTRIBUTES_NO_PROGRAM_TABLES, PROGRAM_TRACKED_ENTITY_ATTRIBUTES_NO_PROGRAM_QUERY)
                    .mapToList(ProgramTrackedEntityAttribute::create);
    }


    @NonNull
    @Override
    public Observable<ProgramStage> programStage(String programStageId) {
        String id = programStageId == null ? "" : programStageId;
        return briteDatabase
                .createQuery(PROGRAM_STAGE_TABLE, SELECT_PROGRAM_STAGE + "'" + id + "'" + LIMIT_1)
                .mapToOne(ProgramStage::create);
    }


    @Override
    public Observable<List<EnrollmentModel>> getTEIEnrollments(String teiUid) {
        String id = teiUid == null ? "" : teiUid;
        return briteDatabase
                .createQuery(EnrollmentModel.TABLE, SELECT_TEI_ENROLLMENTS + "'" + id + "'")
                .mapToList(EnrollmentModel::create);
    }


    @Override
    public List<OptionModel> optionSet(String optionSetId) {
        String selectOptionSet = SELECT + ALL + FROM + OptionModel.TABLE + WHERE + "Option.optionSet = ?";
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
    public Observable<Map<String, ObjectStyleModel>> getObjectStylesForPrograms(List<ProgramModel> enrollmentProgramModels) {
        Map<String, ObjectStyleModel> objectStyleMap = new HashMap<>();
        for (ProgramModel programModel : enrollmentProgramModels) {
            ObjectStyleModel objectStyle = ObjectStyleModel.builder().build();
            try (Cursor cursor = briteDatabase.query("SELECT * FROM ObjectStyle WHERE uid = ?" + LIMIT_1, programModel.uid())) {
                if (cursor.moveToFirst())
                    objectStyle = ObjectStyleModel.create(cursor);
            } catch (Exception e) {
                Timber.e(e);
            }
            objectStyleMap.put(programModel.uid(), objectStyle);
        }

        return Observable.just(objectStyleMap);
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
                .createQuery(ProgramModel.TABLE, PROGRAM_LIST_ALL_QUERY + "'" + id + "'" + LIMIT_1)
                .mapToOne(ProgramModel::create);
    }


    @Override
    public Observable<Pair<String, Integer>> getTheme() {
        return briteDatabase
                .createQuery(SYSTEM_SETTING_TABLE, "SELECT * FROM " + SYSTEM_SETTING_TABLE)
                .mapToList(SystemSetting::create)
                .map(systemSettingModels -> {
                    String flag = "";
                    String style = "";
                    for (SystemSetting settingModel : systemSettingModels)
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
        return briteDatabase.createQuery(ObjectStyleModel.TABLE, "SELECT * FROM ObjectStyle WHERE uid = ?" + LIMIT_1, uid == null ? "" : uid)
                .mapToOneOrDefault((ObjectStyleModel::create), ObjectStyleModel.builder().build());
    }

    @Override
    public Observable<List<OrganisationUnitModel>> getOrganisationUnits() {
        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, "SELECT * FROM OrganisationUnit")
                .mapToList(OrganisationUnitModel::create);
    }


    @Override
    public Observable<ProgramModel> getExpiryDateFromEvent(String eventUid) {
        return briteDatabase
                .createQuery(ProgramModel.TABLE, EXPIRY_DATE_PERIOD_QUERY, eventUid == null ? "" : eventUid)
                .mapToOne(ProgramModel::create);
    }

    @Override
    public Observable<Boolean> isCompletedEventExpired(String eventUid) {
        return Observable.zip(briteDatabase.createQuery(EventModel.TABLE, "SELECT * FROM Event WHERE uid = ?", eventUid)
                        .mapToOne(EventModel::create),
                getExpiryDateFromEvent(eventUid),
                ((eventModel, programModel) -> DateUtils.getInstance().isEventExpired(null, eventModel.completedDate(), programModel.completeEventsExpiryDays())));
    }

    @NonNull
    @Override
    public Observable<List<Resource>> syncState(ProgramModel program) {
        String syncState = "SELECT * FROM " + RESOURCE_TABLE;
        return briteDatabase
                .createQuery(RESOURCE_TABLE, syncState)
                .mapToList(Resource::create);
    }

    @Override
    public Flowable<Pair<Integer, Integer>> getDownloadedData() {
        String teiCount = "SELECT DISTINCT COUNT (uid) FROM TrackedEntityInstance WHERE TrackedEntityInstance.state != 'RELATIONSHIP'";
        String eventCount = "SELECT DISTINCT COUNT (uid) FROM Event WHERE Event.enrollment IS NULL";

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
    public Observable<String> getServerUrl() {
        return briteDatabase.createQuery(AUTH_USER_TABLE, "SELECT SystemInfo.contextPath FROM SystemInfo" + LIMIT_1)
                .mapToOne(cursor -> cursor.getString(0));
    }


    @Override
    public Observable<List<D2Error>> getSyncErrors() {
        return briteDatabase.createQuery(D2ErrorTableInfo.TABLE_INFO.name(), "SELECT * FROM D2Error ORDER BY created DESC")
                .mapToList(D2Error::create);
    }

    @Override
    public Observable<List<OptionModel>> searchOptions(String text, String idOptionSet, int page) {
        String pageQuery = String.format(Locale.US, " LIMIT %d,%d", page * 15, 15);

        String optionQuery = !isEmpty(text) ?
                "select Option.* from OptionSet " +
                        "JOIN Option ON Option.optionSet = OptionSet.uid " +
                        "where OptionSet.uid = ? and Option.displayName like '%" + text + "%' " + pageQuery :
                "select Option.* from OptionSet " +
                        "JOIN Option ON Option.optionSet = OptionSet.uid " +
                        "where OptionSet.uid = ? " + pageQuery;

        return briteDatabase.createQuery(OptionSetModel.TABLE, optionQuery, idOptionSet)
                .mapToList(OptionModel::create);

    }
}