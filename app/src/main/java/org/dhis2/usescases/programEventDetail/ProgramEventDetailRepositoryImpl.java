package org.dhis2.usescases.programEventDetail;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.utils.DateUtils;
import org.dhis2.utils.Period;
import org.dhis2.utils.ValueUtils;
import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Observable;

/**
 * QUADRAM. Created by ppajuelo on 02/11/2017.
 */

public class ProgramEventDetailRepositoryImpl implements ProgramEventDetailRepository {

    private final String EVENT_DATA_VALUES_NEW = "SELECT " +
            "TrackedEntityDataValue.*, " +
            "DataElement.valueType, " +
            "DataElement.optionSet " +
            "FROM TrackedEntityDataValue " +
            "JOIN DataElement ON DataElement.uid = TrackedEntityDataValue.dataElement WHERE TrackedEntityDataValue.event = ?\n" +
            "AND TrackedEntityDataValue.dataElement IN\n" +
            "(SELECT ProgramStageDataElement.dataElement FROM ProgramStageDataElement\n" +
            "WHERE ProgramStageDataElement.displayInReports = '1'\n" +
            "ORDER BY ProgramStageDataElement.sortOrder ASC\n" +
            ")";

    private final BriteDatabase briteDatabase;

    ProgramEventDetailRepositoryImpl(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }

    @NonNull
    private Observable<List<EventModel>> programEvents(String programUid, String fromDate, String toDate) {
        String SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES = "SELECT * FROM " + EventModel.TABLE + " WHERE " + EventModel.Columns.PROGRAM + "='%s' AND " + EventModel.Columns.EVENT_DATE + " BETWEEN '%s' and '%s' " +
                "AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "' " +
                "ORDER BY " + EventModel.TABLE + "." + EventModel.Columns.EVENT_DATE + " DESC";
        return briteDatabase.createQuery(EventModel.TABLE,
                String.format(SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES,
                        programUid == null ? "" : programUid,
                        fromDate == null ? "" : fromDate,
                        toDate == null ? "" : toDate))
                .mapToList(cursor -> {
                    EventModel eventModel = EventModel.create(cursor);

                    Cursor program = briteDatabase.query("SELECT * FROM Program WHERE uid = ?", programUid);
                    if (program != null && program.moveToFirst()) {
                        ProgramModel programModel = ProgramModel.create(program);
                        if (DateUtils.getInstance().hasExpired(eventModel, programModel.expiryDays(), programModel.completeEventsExpiryDays(), programModel.expiryPeriodType())) {
                            ContentValues contentValues = eventModel.toContentValues();
                            contentValues.put(EventModel.Columns.STATUS, EventStatus.SKIPPED.toString());
                            briteDatabase.update(EventModel.TABLE, contentValues, "uid = ?", eventModel.uid());
                        }
                        program.close();
                    }
                    return eventModel;
                });
    }

    @NonNull
    private Observable<List<EventModel>> programEvents(String programUid, List<Date> dates, Period period, String orgUnitQuery) {
        if (dates != null) {
            String SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES = "SELECT * FROM " + EventModel.TABLE + " WHERE " + EventModel.Columns.PROGRAM + "='%s' AND (%s) " +
                    "AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "'";

            StringBuilder dateQuery = new StringBuilder();
            String queryFormat = "(%s BETWEEN '%s' AND '%s') ";
            for (int i = 0; i < dates.size(); i++) {
                Date[] datesToQuery = DateUtils.getInstance().getDateFromDateAndPeriod(dates.get(i), period);
                dateQuery.append(String.format(queryFormat, EventModel.Columns.EVENT_DATE, DateUtils.databaseDateFormat().format(datesToQuery[0]), DateUtils.databaseDateFormat().format(datesToQuery[1])));
                if (i < dates.size() - 1)
                    dateQuery.append("OR ");
            }

            if (orgUnitQuery != null && !orgUnitQuery.isEmpty())
                SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES += " AND " + EventModel.TABLE + "." + EventModel.Columns.ORGANISATION_UNIT + " IN (" + orgUnitQuery + ")";

            SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES += " ORDER BY " + EventModel.TABLE + "." + EventModel.Columns.EVENT_DATE + " DESC";

            return briteDatabase.createQuery(EventModel.TABLE, String.format(SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES,
                    programUid == null ? "" : programUid,
                    dateQuery == null ? "" : dateQuery))
                    .mapToList(EventModel::create);
        } else {
            String SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES = "SELECT * FROM " + EventModel.TABLE + " WHERE " + EventModel.Columns.PROGRAM + "='%s' " +
                    "AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "'";

            if (orgUnitQuery != null && !orgUnitQuery.isEmpty())
                SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES += " AND " + EventModel.TABLE + "." + EventModel.Columns.ORGANISATION_UNIT + " IN (" + orgUnitQuery + ")";

            SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES += " ORDER BY " + EventModel.TABLE + "." + EventModel.Columns.EVENT_DATE + " DESC";

            return briteDatabase.createQuery(EventModel.TABLE, String.format(SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES, programUid == null ? "" : programUid))
                    .mapToList(cursor -> {
                        EventModel eventModel = EventModel.create(cursor);

                        Cursor program = briteDatabase.query("SELECT * FROM Program WHERE uid = ?", programUid);
                        if (program != null && program.moveToFirst()) {
                            ProgramModel programModel = ProgramModel.create(program);
                            if (DateUtils.getInstance().hasExpired(eventModel, programModel.expiryDays(), programModel.completeEventsExpiryDays(), programModel.expiryPeriodType())) {
                                ContentValues contentValues = eventModel.toContentValues();
                                contentValues.put(EventModel.Columns.STATUS, EventStatus.SKIPPED.toString());
                                briteDatabase.update(EventModel.TABLE, contentValues, "uid = ?", eventModel.uid());
                            }
                            program.close();
                        }
                        return eventModel;
                    });
        }
    }

    @NonNull
    @Override
    public Observable<List<EventModel>> filteredProgramEvents(String programUid, String fromDate, String toDate, CategoryOptionComboModel categoryOptionComboModel, String orgUnitQuery) {
        if (categoryOptionComboModel == null) {
            return programEvents(programUid, fromDate, toDate);
        }
        String SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES_AND_CAT_COMBO = "SELECT * FROM " + EventModel.TABLE + " WHERE " + EventModel.Columns.PROGRAM + "='%s' AND " + EventModel.Columns.EVENT_DATE + " BETWEEN '%s' and '%s'"
                + " AND " + EventModel.Columns.ATTRIBUTE_OPTION_COMBO + "='%s'"
                + " AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "'";
        if (orgUnitQuery != null && !orgUnitQuery.isEmpty())
            SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES_AND_CAT_COMBO += " AND " + EventModel.TABLE + "." + EventModel.Columns.ORGANISATION_UNIT + " IN (" + orgUnitQuery + ")";

        String id = categoryOptionComboModel == null || categoryOptionComboModel.uid() == null ? "" : categoryOptionComboModel.uid();
        return briteDatabase.createQuery(EventModel.TABLE, String.format(SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES_AND_CAT_COMBO,
                programUid == null ? "" : programUid,
                fromDate == null ? "" : fromDate,
                toDate == null ? "" : toDate,
                id))
                .mapToList(cursor -> {
                    EventModel eventModel = EventModel.create(cursor);

                    Cursor program = briteDatabase.query("SELECT * FROM Program WHERE uid = ?", programUid);
                    if (program != null && program.moveToFirst()) {
                        ProgramModel programModel = ProgramModel.create(program);
                        if (DateUtils.getInstance().hasExpired(eventModel, programModel.expiryDays(), programModel.completeEventsExpiryDays(), programModel.expiryPeriodType())) {
                            ContentValues contentValues = eventModel.toContentValues();
                            contentValues.put(EventModel.Columns.STATUS, EventStatus.SKIPPED.toString());
                            briteDatabase.update(EventModel.TABLE, contentValues, "uid = ?", eventModel.uid());
                        }
                        program.close();
                    }
                    return eventModel;
                });
    }

    @NonNull
    @Override
    public Observable<List<EventModel>> filteredProgramEvents(String programUid, List<Date> dates, Period period, CategoryOptionComboModel categoryOptionComboModel, String orgUnitQuery) {
        if (categoryOptionComboModel == null) {
            return programEvents(programUid, dates, period, orgUnitQuery);
        }
        if (dates != null) {
            String SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES_AND_CAT_COMBO = "SELECT * FROM " + EventModel.TABLE + " WHERE " + EventModel.Columns.PROGRAM + "='%s' AND " + EventModel.Columns.ATTRIBUTE_OPTION_COMBO + "='%s' AND (%s) " +
                    "AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "'";
            StringBuilder dateQuery = new StringBuilder();
            String queryFormat = "(%s BETWEEN '%s' AND '%s') ";
            for (int i = 0; i < dates.size(); i++) {
                Date[] datesToQuery = DateUtils.getInstance().getDateFromDateAndPeriod(dates.get(i), period);
                dateQuery.append(String.format(queryFormat, EventModel.Columns.EVENT_DATE, DateUtils.getInstance().formatDate(datesToQuery[0]), DateUtils.getInstance().formatDate(datesToQuery[1])));
                if (i < dates.size() - 1)
                    dateQuery.append("OR ");
            }

            if (orgUnitQuery != null && !orgUnitQuery.isEmpty())
                SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES_AND_CAT_COMBO += " AND " + EventModel.TABLE + "." + EventModel.Columns.ORGANISATION_UNIT + " IN (" + orgUnitQuery + ")";

            String id = categoryOptionComboModel == null || categoryOptionComboModel.uid() == null ? "" : categoryOptionComboModel.uid();
            return briteDatabase.createQuery(EventModel.TABLE, String.format(SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES_AND_CAT_COMBO,
                    programUid == null ? "" : programUid,
                    id,
                    dateQuery))
                    .mapToList(EventModel::create);
        } else {
            String SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES_AND_CAT_COMBO = "SELECT * FROM " + EventModel.TABLE + " WHERE " + EventModel.Columns.PROGRAM + "='%s' AND " + EventModel.Columns.ATTRIBUTE_OPTION_COMBO + "='%s' " +
                    "AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "'";

            if (orgUnitQuery != null && !orgUnitQuery.isEmpty())
                SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES_AND_CAT_COMBO += " AND " + EventModel.TABLE + "." + EventModel.Columns.ORGANISATION_UNIT + " IN (" + orgUnitQuery + ")";

            String id = categoryOptionComboModel == null || categoryOptionComboModel.uid() == null ? "" : categoryOptionComboModel.uid();
            return briteDatabase.createQuery(EventModel.TABLE, String.format(SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES_AND_CAT_COMBO,
                    programUid == null ? "" : programUid,
                    id))
                    .mapToList(cursor -> {
                        EventModel eventModel = EventModel.create(cursor);

                        Cursor program = briteDatabase.query("SELECT * FROM Program WHERE uid = ?", programUid);
                        if (program != null && program.moveToFirst()) {
                            ProgramModel programModel = ProgramModel.create(program);
                            if (DateUtils.getInstance().hasExpired(eventModel, programModel.expiryDays(), programModel.completeEventsExpiryDays(), programModel.expiryPeriodType())) {
                                ContentValues contentValues = eventModel.toContentValues();
                                contentValues.put(EventModel.Columns.STATUS, EventStatus.SKIPPED.toString());
                                briteDatabase.update(EventModel.TABLE, contentValues, "uid = ?", eventModel.uid());
                            }
                            program.close();
                        }
                        return eventModel;
                    });
        }
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnitModel>> orgUnits() {
        String SELECT_ORG_UNITS = "SELECT * FROM " + OrganisationUnitModel.TABLE;
        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, SELECT_ORG_UNITS)
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
        Cursor cursor = briteDatabase.query(EVENT_DATA_VALUES_NEW, id);
        if (cursor != null && cursor.moveToFirst()) {
            for (int i = 0; i < cursor.getCount(); i++) {
                String value = ValueUtils.transform(briteDatabase, cursor).value();
                values.add(value);
                cursor.moveToNext();
            }
            cursor.close();
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