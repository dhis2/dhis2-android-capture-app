package org.dhis2.usescases.programEventDetail;

import android.database.Cursor;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.tuples.Pair;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.Period;
import org.dhis2.utils.ValueUtils;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by ppajuelo on 02/11/2017.
 */

public class ProgramEventDetailRepositoryImpl implements ProgramEventDetailRepository {

    private final String EVENT_DATA_VALUES = "SELECT \n" +
            " DataElement.uid, \n" +
            " DataElement.displayName, \n" +
            " DataElement.valueType,\n" +
            " DataElement.optionSet, \n" +
            " ProgramStageDataElement.displayInReports, \n" +
            " TrackedEntityDataValue.value \n" +
            " FROM TrackedEntityDataValue \n" +
            " JOIN ProgramStageDataElement ON ProgramStageDataElement.dataElement = TrackedEntityDataValue.dataElement \n" +
            " JOIN Event ON Event.programStage = ProgramStageDataElement.programStage \n" +
            " JOIN DataElement ON DataElement.uid = TrackedEntityDataValue.dataElement \n" +
            " WHERE TrackedEntityDataValue.event = ? AND Event.uid = ? AND ProgramStageDataElement.displayInReports = 1 ORDER BY sortOrder";


    private final BriteDatabase briteDatabase;
    D2 d2;

    ProgramEventDetailRepositoryImpl(BriteDatabase briteDatabase, D2 d2) {
        this.briteDatabase = briteDatabase;
        this.d2 = d2;
    }

    @NonNull
    private Flowable<List<ProgramEventViewModel>> programEvents(String programUid, List<Date> dates, Period period, String orgUnitQuery, int page) {
        String pageQuery = String.format(Locale.US, " LIMIT %d,%d", page * 20, 20);

        String orgQuery = "";
        if (!isEmpty(orgUnitQuery))
            orgQuery = String.format(" AND Event.organisationUnit IN (%s) ", orgUnitQuery);

        if (dates != null) {
            String SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES = "SELECT * FROM " + EventModel.TABLE + " WHERE " + EventModel.Columns.PROGRAM + "='%s' AND (%s) " +
                    "AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "'" +
                    orgQuery +
                    " ORDER BY " + EventModel.TABLE + "." + EventModel.Columns.EVENT_DATE + " DESC, Event.lastUpdated DESC";

            StringBuilder dateQuery = new StringBuilder();
            String queryFormat = "(%s BETWEEN '%s' AND '%s') ";
            for (int i = 0; i < dates.size(); i++) {
                Date[] datesToQuery = DateUtils.getInstance().getDateFromDateAndPeriod(dates.get(i), period);
                dateQuery.append(String.format(queryFormat, EventModel.Columns.EVENT_DATE, DateUtils.databaseDateFormat().format(datesToQuery[0]), DateUtils.databaseDateFormat().format(datesToQuery[1])));
                if (i < dates.size() - 1)
                    dateQuery.append("OR ");
            }

            return briteDatabase.createQuery(EventModel.TABLE, String.format(SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES + pageQuery,
                    programUid == null ? "" : programUid,
                    dateQuery))
                    .mapToList(cursor -> transformIntoEventViewModel(EventModel.create(cursor))).toFlowable(BackpressureStrategy.LATEST);
        } else {


            String SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES = "SELECT * FROM " + EventModel.TABLE + " WHERE " + EventModel.Columns.PROGRAM + "='%s' " +
                    "AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "'" +
                    orgQuery +
                    " ORDER BY " + EventModel.TABLE + "." + EventModel.Columns.EVENT_DATE + " DESC, Event.lastUpdated DESC";

            return briteDatabase.createQuery(EventModel.TABLE, String.format(SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES + pageQuery, programUid == null ? "" : programUid))
                    .mapToList(cursor -> {
                        EventModel eventModel = EventModel.create(cursor);
                        return transformIntoEventViewModel(eventModel);
                    }).toFlowable(BackpressureStrategy.LATEST);
        }
    }

    @NonNull
    @Override
    public Flowable<List<ProgramEventViewModel>> filteredProgramEvents(String programUid, List<Date> dates,
                                                                       Period period,
                                                                       CategoryOptionComboModel categoryOptionComboModel,
                                                                       String orgUnitQuery,
                                                                       int page) {
        if (orgUnitQuery == null)
            orgUnitQuery = "";
        String pageQuery = String.format(Locale.US, " LIMIT %d,%d", page * 20, 20);

        if (categoryOptionComboModel == null) {
            return programEvents(programUid, dates, period, orgUnitQuery, page);
        }
        if (dates != null) {
            String SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES_AND_CAT_COMBO = "SELECT * FROM " + EventModel.TABLE + " WHERE " + EventModel.Columns.PROGRAM + "='%s' AND " + EventModel.Columns.ATTRIBUTE_OPTION_COMBO + "='%s' AND (%s) " +
                    "AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "'" +
                    " AND " + EventModel.TABLE + "." + EventModel.Columns.ORGANISATION_UNIT + " IN (" + orgUnitQuery + ")";

            StringBuilder dateQuery = new StringBuilder();
            String queryFormat = "(%s BETWEEN '%s' AND '%s') ";
            for (int i = 0; i < dates.size(); i++) {
                Date[] datesToQuery = DateUtils.getInstance().getDateFromDateAndPeriod(dates.get(i), period);
                dateQuery.append(String.format(queryFormat, EventModel.Columns.EVENT_DATE, DateUtils.getInstance().formatDate(datesToQuery[0]), DateUtils.getInstance().formatDate(datesToQuery[1])));
                if (i < dates.size() - 1)
                    dateQuery.append("OR ");
            }

            String id = categoryOptionComboModel.uid() == null ? "" : categoryOptionComboModel.uid();
            return briteDatabase.createQuery(EventModel.TABLE, String.format(SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES_AND_CAT_COMBO + pageQuery,
                    programUid == null ? "" : programUid,
                    id,
                    dateQuery))
                    .mapToList(cursor -> transformIntoEventViewModel(EventModel.create(cursor))).toFlowable(BackpressureStrategy.LATEST);
        } else {
            String SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES_AND_CAT_COMBO = "SELECT * FROM " + EventModel.TABLE + " WHERE " + EventModel.Columns.PROGRAM + "='%s' AND " + EventModel.Columns.ATTRIBUTE_OPTION_COMBO + "='%s' " +
                    "AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "'" +
                    " AND " + EventModel.TABLE + "." + EventModel.Columns.ORGANISATION_UNIT + " IN (" + orgUnitQuery + ")";

            String id = categoryOptionComboModel.uid() == null ? "" : categoryOptionComboModel.uid();
            return briteDatabase.createQuery(EventModel.TABLE, String.format(SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES_AND_CAT_COMBO + pageQuery,
                    programUid == null ? "" : programUid,
                    id))
                    .mapToList(cursor -> {
                        EventModel eventModel = EventModel.create(cursor);
                        return transformIntoEventViewModel(eventModel);
                    }).toFlowable(BackpressureStrategy.LATEST);
        }
    }

    private ProgramEventViewModel transformIntoEventViewModel(EventModel eventModel) {

        String orgUnitName = getOrgUnitName(eventModel.organisationUnit());
        List<Pair<String, String>> data = getData(eventModel.uid());
        boolean hasExpired = isExpired(eventModel);
        String attributeOptionCombo = getAttributeOptionCombo(eventModel.attributeOptionCombo());

        return ProgramEventViewModel.create(
                eventModel.uid(),
                eventModel.organisationUnit(),
                orgUnitName,
                eventModel.eventDate(),
                eventModel.state(),
                data,
                eventModel.status(),
                hasExpired,
                attributeOptionCombo);
    }

    private String getAttributeOptionCombo(String categoryOptionComboId) {
        String catOptionCombName = "";
        CategoryOptionCombo categoryOptionCombo;
        if (!isEmpty(categoryOptionComboId)) {
            categoryOptionCombo = d2.categoryModule().categoryOptionCombos.uid(categoryOptionComboId).withAllChildren().get();
            if (!d2.categoryModule().categoryCombos.uid(categoryOptionCombo.categoryCombo().uid()).get().isDefault())
                catOptionCombName = categoryOptionCombo.displayName();
        }
        return catOptionCombName;
    }

    private boolean isExpired(EventModel eventModel) {
        Program program = d2.programModule().programs.uid(eventModel.program()).get();
        return DateUtils.getInstance().isEventExpired(eventModel.eventDate(),
                eventModel.completedDate(),
                eventModel.status(),
                program.completeEventsExpiryDays(),
                program.expiryPeriodType(),
                program.expiryDays());

      /*  boolean hasExpired = false;
        Cursor programCursor = briteDatabase.query("SELECT * FROM Program WHERE uid = ?", eventModel.program());
        if (programCursor != null) {
            if (programCursor.moveToFirst()) {
                ProgramModel program = ProgramModel.create(programCursor);
                if (eventModel.status() == EventStatus.ACTIVE)
                    hasExpired = DateUtils.getInstance().hasExpired(eventModel, program.expiryDays(), program.completeEventsExpiryDays(), program.expiryPeriodType());
                if (eventModel.status() == EventStatus.COMPLETED)
                    hasExpired = DateUtils.getInstance().isEventExpired(null, eventModel.completedDate(), program.completeEventsExpiryDays());
            }
        }
        return hasExpired;*/
    }

    private String getOrgUnitName(String orgUnitUid) {
        String orgUrgUnitName = "";
        try (Cursor orgUnitCursor = briteDatabase.query("SELECT displayName FROM OrganisationUnit WHERE uid = ?", orgUnitUid)) {
            if (orgUnitCursor != null && orgUnitCursor.moveToFirst())
                orgUrgUnitName = orgUnitCursor.getString(0);
        }
        return orgUrgUnitName;
    }

    private List<Pair<String, String>> getData(String eventUid) {
        List<Pair<String, String>> data = new ArrayList<>();
        try (Cursor cursor = briteDatabase.query(EVENT_DATA_VALUES, eventUid, eventUid)) {
            if (cursor != null && cursor.moveToFirst()) {
                for (int i = 0; i < cursor.getCount(); i++) {
                    String displayName = cursor.getString(cursor.getColumnIndex("displayName"));
                    String value = cursor.getString(cursor.getColumnIndex("value"));
                    if (cursor.getString(cursor.getColumnIndex("optionSet")) != null)
                        value = ValueUtils.optionSetCodeToDisplayName(briteDatabase, cursor.getString(cursor.getColumnIndex("optionSet")), value);
                    else if (cursor.getString(cursor.getColumnIndex("valueType")).equals(ValueType.ORGANISATION_UNIT.name()))
                        value = ValueUtils.orgUnitUidToDisplayName(briteDatabase, value);

                    //TODO: Would be good to check other value types to render value (coordinates)

                    data.add(Pair.create(displayName, value));
                    cursor.moveToNext();
                }
            }
        }
        return data;
    }


    @NonNull
    @Override
    public Observable<List<OrganisationUnitModel>> orgUnits() {
        String SELECT_ORG_UNITS = "SELECT * FROM " + OrganisationUnitModel.TABLE + " " +
                "WHERE uid IN (SELECT UserOrganisationUnit.organisationUnit FROM UserOrganisationUnit " +
                "WHERE UserOrganisationUnit.organisationUnitScope = 'SCOPE_DATA_CAPTURE')";
        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, SELECT_ORG_UNITS)
                .mapToList(OrganisationUnitModel::create);
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnitModel>> orgUnits(String parentUid) {
        String SELECT_ORG_UNITS_BY_PARENT = "SELECT OrganisationUnit.* FROM OrganisationUnit " +
                "JOIN UserOrganisationUnit ON UserOrganisationUnit.organisationUnit = OrganisationUnit.uid " +
                "WHERE OrganisationUnit.parent = ? AND UserOrganisationUnit.organisationUnitScope = 'SCOPE_DATA_CAPTURE' " +
                "ORDER BY OrganisationUnit.displayName ASC";

        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, SELECT_ORG_UNITS_BY_PARENT, parentUid)
                .mapToList(OrganisationUnitModel::create);
    }

    @NonNull
    @Override
    public Observable<List<CategoryOptionComboModel>> catCombo(String categoryComboUid) {
        String id = categoryComboUid == null ? "" : categoryComboUid;
        String SELECT_CATEGORY_COMBO = "SELECT " + CategoryOptionComboModel.TABLE + ".* FROM " + CategoryOptionComboModel.TABLE + " INNER JOIN " + CategoryComboModel.TABLE +
                " ON " + CategoryOptionComboModel.TABLE + "." + CategoryOptionComboModel.Columns.CATEGORY_COMBO + " = " + CategoryComboModel.TABLE + "." + CategoryComboModel.Columns.UID
                + " WHERE " + CategoryComboModel.TABLE + "." + CategoryComboModel.Columns.UID + " = '" + id + "'";
        return briteDatabase.createQuery(CategoryOptionComboModel.TABLE, SELECT_CATEGORY_COMBO)
                .mapToList(CategoryOptionComboModel::create);
    }

    @Override
    public Observable<List<String>> eventDataValuesNew(EventModel eventModel) {
        List<String> values = new ArrayList<>();
        String id = eventModel == null || eventModel.uid() == null ? "" : eventModel.uid();
        try (Cursor cursor = briteDatabase.query(EVENT_DATA_VALUES, id, id)) {
            if (cursor != null && cursor.moveToFirst()) {
                for (int i = 0; i < cursor.getCount(); i++) {
                    String value = cursor.getString(cursor.getColumnIndex("value"));
                    if (cursor.getString(cursor.getColumnIndex("optionSet")) != null)
                        value = ValueUtils.optionSetCodeToDisplayName(briteDatabase, cursor.getString(cursor.getColumnIndex("optionSet")), value);
                    else if (cursor.getString(cursor.getColumnIndex("valueType")).equals(ValueType.ORGANISATION_UNIT.name()))
                        value = ValueUtils.orgUnitUidToDisplayName(briteDatabase, value);

                    values.add(value);
                    cursor.moveToNext();
                }
            }
        }
        return Observable.just(values);
    }

    @Override
    public Observable<Boolean> writePermission(String programId) {
        String WRITE_PERMISSION = "SELECT ProgramStage.accessDataWrite FROM ProgramStage WHERE ProgramStage.program = ? LIMIT 1";
        String PROGRAM_WRITE_PERMISSION = "SELECT Program.accessDataWrite FROM Program WHERE Program.uid = ? LIMIT 1";
        return briteDatabase.createQuery(ProgramStageModel.TABLE, WRITE_PERMISSION, programId == null ? "" : programId)
                .mapToOne(cursor -> cursor.getInt(0) == 1)
                .flatMap(programStageAccessDataWrite ->
                        briteDatabase.createQuery(ProgramModel.TABLE, PROGRAM_WRITE_PERMISSION, programId == null ? "" : programId)
                                .mapToOne(cursor -> (cursor.getInt(0) == 1) && programStageAccessDataWrite));
    }
}