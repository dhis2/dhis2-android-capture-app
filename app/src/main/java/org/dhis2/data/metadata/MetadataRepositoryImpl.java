package org.dhis2.data.metadata;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.R;
import org.dhis2.data.tuples.Pair;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.common.ObjectStyleModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.imports.TrackerImportConflict;
import org.hisp.dhis.android.core.option.OptionGroup;
import org.hisp.dhis.android.core.option.OptionModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.program.ProgramStageSectionModel;
import org.hisp.dhis.android.core.settings.SystemSettingModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityTypeModel;
import org.hisp.dhis.android.core.user.AuthenticatedUserModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;
import static android.text.TextUtils.join;


/**
 * QUADRAM. Created by ppajuelo on 04/12/2017.
 */

public class MetadataRepositoryImpl implements MetadataRepository {

    private static final String SELECT_TEI_ENROLLMENTS = String.format(
            "SELECT * FROM %s WHERE %s.%s =",
            EnrollmentModel.TABLE,
            EnrollmentModel.TABLE, EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE);



    private final String PROGRAM_LIST_ALL_QUERY = String.format("SELECT * FROM %s WHERE %s.%s = ",
            ProgramModel.TABLE, ProgramModel.TABLE, ProgramModel.Columns.UID);

    private final String TRACKED_ENTITY_QUERY = String.format("SELECT * FROM %s WHERE %s.%s = ",
            TrackedEntityTypeModel.TABLE, TrackedEntityTypeModel.TABLE, TrackedEntityTypeModel.Columns.UID);

    private final String ORG_UNIT_QUERY = String.format("SELECT * FROM %s WHERE %s.%s = ",
            OrganisationUnitModel.TABLE, OrganisationUnitModel.TABLE, OrganisationUnitModel.Columns.UID);



    private final String SELECT_DEFAULT_CAT_COMBO = String.format("SELECT %s FROM %s WHERE %s.%s = '1' LIMIT 1",
            CategoryComboModel.Columns.UID, CategoryComboModel.TABLE, CategoryComboModel.TABLE, CategoryComboModel.Columns.IS_DEFAULT);

    private final String SELECT_DEFAULT_CAT_OPTION_COMBO = String.format("SELECT %s FROM %s WHERE %s.%s = 'default'",
            CategoryOptionComboModel.Columns.UID, CategoryOptionComboModel.TABLE, CategoryOptionComboModel.TABLE, CategoryOptionComboModel.Columns.CODE);


    private static final String EXPIRY_DATE_PERIOD_QUERY = String.format(
            "SELECT program.* FROM %s " +
                    "JOIN %s ON %s.%s = %s.%s " +
                    "WHERE %s.%s = ? " +
                    "LIMIT 1",
            ProgramModel.TABLE,
            EventModel.TABLE, ProgramModel.TABLE, ProgramModel.Columns.UID, EventModel.TABLE, EventModel.Columns.PROGRAM,
            EventModel.TABLE, EventModel.Columns.UID);

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
    public Observable<String> getDefaultCategoryOptionId() {
        return briteDatabase
                .createQuery(CategoryComboModel.TABLE, SELECT_DEFAULT_CAT_COMBO)
                .mapToOne(cursor -> cursor.getString(0));
    }
    @Override
    public Observable<String> getDefaultCategoryOptionComboId() {
        return briteDatabase
                .createQuery(CategoryOptionComboModel.TABLE, SELECT_DEFAULT_CAT_OPTION_COMBO)
                .mapToOne(cursor -> cursor.getString(0));
    }

    @Override
    public void saveCatOption(String eventUid, String catOptComboUid) {
        ContentValues event = new ContentValues();
        event.put(EventModel.Columns.ATTRIBUTE_OPTION_COMBO, catOptComboUid);
        briteDatabase.update(EventModel.TABLE, event, EventModel.Columns.UID + " = ?", eventUid == null ? "" : eventUid);
    }

    @Override
    public Observable<OrganisationUnitModel> getOrganisationUnit(String orgUnitUid) {
        String id = orgUnitUid == null ? "" : orgUnitUid;
        return briteDatabase
                .createQuery(OrganisationUnitModel.TABLE, ORG_UNIT_QUERY + "'" + id + "' LIMIT 1")
                .mapToOne(OrganisationUnitModel::create);
    }


    @Override
    public Observable<List<EnrollmentModel>> getTEIEnrollments(String teiUid) {
        String id = teiUid == null ? "" : teiUid;
        return briteDatabase
                .createQuery(EnrollmentModel.TABLE, SELECT_TEI_ENROLLMENTS + "'" + id + "'")
                .mapToList(EnrollmentModel::create);
    }


    @Override
    public Flowable<ProgramStageModel> programStageForEvent(String eventId) {
        return briteDatabase.createQuery(ProgramStageSectionModel.TABLE, "SELECT ProgramStage.* FROM ProgramStage JOIN Event ON Event.programStage = ProgramStage.uid WHERE Event.uid = ? LIMIT 1", eventId)
                .mapToOne(ProgramStageModel::create).toFlowable(BackpressureStrategy.LATEST);
    }



    @Override
    public Observable<ProgramModel> getProgramWithId(String programUid) {
        String id = programUid == null ? "" : programUid;
        return briteDatabase
                .createQuery(ProgramModel.TABLE, PROGRAM_LIST_ALL_QUERY + "'" + id + "' LIMIT 1")
                .mapToOne(ProgramModel::create);
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


    @Override
    public Flowable<Pair<Integer, Integer>> getDownloadedData() {
        String TEI_COUNT = "SELECT DISTINCT COUNT (uid) FROM TrackedEntityInstance WHERE TrackedEntityInstance.state != 'RELATIONSHIP'";
        String EVENT_COUNT = "SELECT DISTINCT COUNT (uid) FROM Event WHERE Event.enrollment IS NULL";

        int currentTei = 0;
        int currentEvent = 0;

        try (Cursor teiCursor = briteDatabase.query(TEI_COUNT)) {
            if (teiCursor != null && teiCursor.moveToFirst()) {
                currentTei = teiCursor.getInt(0);
            }
        }

        try (Cursor eventCursor = briteDatabase.query(EVENT_COUNT)) {
            if (eventCursor != null && eventCursor.moveToFirst()) {
                currentEvent = eventCursor.getInt(0);
            }
        }
        return Flowable.just(Pair.create(currentEvent, currentTei));
    }


    @Override
    public Observable<String> getServerUrl() {
        return briteDatabase.createQuery(AuthenticatedUserModel.TABLE, "SELECT SystemInfo.contextPath FROM SystemInfo LIMIT 1")
                .mapToOne(cursor -> cursor.getString(0));
    }


    @Override
    public List<TrackerImportConflict> getSyncErrors() {
        List<TrackerImportConflict> conflicts = new ArrayList<>();
        try (Cursor cursor = briteDatabase.query("SELECT * FROM TrackerImportConflict ORDER BY created DESC")) {
            if (cursor != null && cursor.moveToFirst()) {
                for (int i = 0; i < cursor.getCount(); i++) {
                    TrackerImportConflict conflict = TrackerImportConflict.create(cursor);
                    conflicts.add(conflict);
                    cursor.moveToNext();
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        return conflicts;
    }

    @Override
    public Observable<List<OptionModel>> searchOptions(String text, String idOptionSet, int page, List<String> optionsToHide, List<String> optionsGroupsToHide) {
        String pageQuery = String.format(Locale.US, "GROUP BY Option.uid ORDER BY sortOrder LIMIT %d,%d", page * 15, 15);
        String formattedOptionsToHide = "'" + join("','", optionsToHide) + "'";

        String optionQuery = "SELECT Option.* FROM Option WHERE Option.optionSet = ? " +
                (!optionsToHide.isEmpty() ? "AND Option.uid NOT IN (" + formattedOptionsToHide + ") " : " ") +
                (!isEmpty(text) ? "AND Option.displayName LIKE '%" + text + "%' " : " ") +
                pageQuery;

        return briteDatabase.createQuery(OptionModel.TABLE, optionQuery, idOptionSet)
                .mapToList(OptionModel::create)
                .map(optionList -> {
                    Iterator<OptionModel> iterator = optionList.iterator();
                    while (iterator.hasNext()) {
                        OptionModel option = iterator.next();
                        List<String> optionGroupUids = new ArrayList<>();
                        try (Cursor optionGroupCursor = briteDatabase.query("SELECT OptionGroup.* FROM OptionGroup " +
                                "LEFT JOIN OptionGroupOptionLink ON OptionGroupOptionLink.optionGroup = OptionGroup.uid WHERE OptionGroupOptionLink.option = ?", option.uid())) {
                            if (optionGroupCursor.moveToFirst()) {
                                for (int i = 0; i < optionGroupCursor.getCount(); i++) {
                                    optionGroupUids.add(OptionGroup.create(optionGroupCursor).uid());
                                    optionGroupCursor.moveToNext();
                                }
                            }
                        }
                        boolean remove = false;
                        for (String group : optionGroupUids)
                            if (optionsGroupsToHide.contains(group))
                                remove = true;

                        if (remove)
                            iterator.remove();

                    }
                    return optionList;
                });
    }
}