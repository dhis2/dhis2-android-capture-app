package org.dhis2.usescases.programEventDetail;

import android.content.ContentValues;
import android.database.Cursor;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.utils.DateUtils;
import org.dhis2.utils.Period;
import org.dhis2.utils.ValueUtils;
import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
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

import static org.dhis2.data.database.SqlConstants.ALL;
import static org.dhis2.data.database.SqlConstants.AND;
import static org.dhis2.data.database.SqlConstants.COMMA;
import static org.dhis2.data.database.SqlConstants.DESC;
import static org.dhis2.data.database.SqlConstants.EQUAL;
import static org.dhis2.data.database.SqlConstants.FROM;
import static org.dhis2.data.database.SqlConstants.IN;
import static org.dhis2.data.database.SqlConstants.INNER_JOIN;
import static org.dhis2.data.database.SqlConstants.LIMIT_1;
import static org.dhis2.data.database.SqlConstants.NOT_EQUAL;
import static org.dhis2.data.database.SqlConstants.ON;
import static org.dhis2.data.database.SqlConstants.ORDER_BY;
import static org.dhis2.data.database.SqlConstants.POINT;
import static org.dhis2.data.database.SqlConstants.QUESTION_MARK;
import static org.dhis2.data.database.SqlConstants.QUOTE;
import static org.dhis2.data.database.SqlConstants.SELECT;
import static org.dhis2.data.database.SqlConstants.VARIABLE;
import static org.dhis2.data.database.SqlConstants.WHERE;

/**
 * QUADRAM. Created by ppajuelo on 02/11/2017.
 */

public class ProgramEventDetailRepositoryImpl implements ProgramEventDetailRepository {

    private static final String EVENT_DATA_VALUES = SELECT +
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

    ProgramEventDetailRepositoryImpl(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }

    private StringBuilder getDateQuery(Period period, List<Date> dates) {
        StringBuilder dateQuery = new StringBuilder();
        String queryFormat = "(%s BETWEEN '%s' AND '%s') ";
        for (int i = 0; i < dates.size(); i++) {
            Date[] datesToQuery = DateUtils.getInstance().getDateFromDateAndPeriod(dates.get(i), period);
            dateQuery.append(String.format(queryFormat, EventModel.Columns.EVENT_DATE, DateUtils.databaseDateFormat().format(datesToQuery[0]), DateUtils.databaseDateFormat().format(datesToQuery[1])));
            if (i < dates.size() - 1)
                dateQuery.append("OR ");
        }
        return dateQuery;
    }

    private String getEventWithProgramUidAndDatesQuery(String orgUnitQuery, String pageQuery) {
        String selectEventWithProgramUidAndDates = SELECT + ALL + FROM + EventModel.TABLE +
                WHERE + EventModel.Columns.PROGRAM + EQUAL + QUOTE + VARIABLE + QUOTE + AND + "(%s)" +
                AND + EventModel.TABLE + POINT + EventModel.Columns.STATE + NOT_EQUAL + QUOTE + State.TO_DELETE + QUOTE +
                AND + EventModel.TABLE + POINT + EventModel.Columns.ORGANISATION_UNIT + IN + "(" + orgUnitQuery + ")" +
                ORDER_BY + EventModel.TABLE + POINT + EventModel.Columns.EVENT_DATE + DESC + COMMA +
                EventModel.TABLE + POINT + EventModel.Columns.LAST_UPDATED + DESC;

        if (!orgUnitQuery.isEmpty())
            selectEventWithProgramUidAndDates += AND + EventModel.TABLE + POINT + EventModel.Columns.ORGANISATION_UNIT +
                    IN + "(" + orgUnitQuery + ")";

        selectEventWithProgramUidAndDates += ORDER_BY + EventModel.TABLE + POINT + EventModel.Columns.EVENT_DATE + DESC + pageQuery;
        return selectEventWithProgramUidAndDates;
    }

    @NonNull
    private Flowable<List<EventModel>> programEvents(String programUid, List<Date> dates, Period period, String orgUnitQuery, int page) {
        String pageQuery = String.format(Locale.US, " LIMIT %d,%d", page * 20, 20);
        if (orgUnitQuery == null)
            orgUnitQuery = "";

        if (dates != null) {
            return briteDatabase.createQuery(EventModel.TABLE, String.format(getEventWithProgramUidAndDatesQuery(orgUnitQuery, pageQuery),
                    programUid == null ? "" : programUid,
                    getDateQuery(period, dates)))
                    .mapToList(EventModel::create).toFlowable(BackpressureStrategy.LATEST);
        } else {
            String selectEventWithProgramUidAndDates = SELECT + ALL + FROM + EventModel.TABLE +
                    WHERE + EventModel.Columns.PROGRAM + EQUAL + QUOTE + VARIABLE + QUOTE +
                    AND + EventModel.TABLE + POINT + EventModel.Columns.STATE + NOT_EQUAL + QUOTE + State.TO_DELETE + QUOTE +
                    AND + EventModel.TABLE + POINT + EventModel.Columns.ORGANISATION_UNIT + IN + "(" + orgUnitQuery + ")" +
                    ORDER_BY + EventModel.TABLE + POINT + EventModel.Columns.EVENT_DATE + DESC + COMMA +
                    EventModel.TABLE + POINT + EventModel.Columns.LAST_UPDATED + DESC + pageQuery;

            return briteDatabase.createQuery(EventModel.TABLE, String.format(selectEventWithProgramUidAndDates, programUid == null ? "" : programUid))
                    .mapToList(cursor -> {
                        EventModel eventModel = EventModel.create(cursor);

                        Cursor program = briteDatabase.query(SELECT + ALL + FROM + ProgramModel.TABLE +
                                WHERE + ProgramModel.Columns.UID + EQUAL + QUESTION_MARK, programUid);
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
                    }).toFlowable(BackpressureStrategy.LATEST);
        }
    }

    private StringBuilder getFilteredDatesQuery(List<Date> dates, Period period) {
        StringBuilder dateQuery = new StringBuilder();
        String queryFormat = "(%s BETWEEN '%s' AND '%s') ";
        for (int i = 0; i < dates.size(); i++) {
            Date[] datesToQuery = DateUtils.getInstance().getDateFromDateAndPeriod(dates.get(i), period);
            dateQuery.append(String.format(queryFormat, EventModel.Columns.EVENT_DATE, DateUtils.getInstance().formatDate(datesToQuery[0]), DateUtils.getInstance().formatDate(datesToQuery[1])));
            if (i < dates.size() - 1)
                dateQuery.append("OR ");
        }
        return dateQuery;
    }

    @NonNull
    @Override
    public Flowable<List<EventModel>> filteredProgramEvents(String programUid, List<Date> dates,
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
            String id = categoryOptionComboModel.uid() == null ? "" : categoryOptionComboModel.uid();
            String selectEventWithProgramUidAndDatesAndCatCombo = SELECT + ALL + FROM + EventModel.TABLE +
                    WHERE + EventModel.Columns.PROGRAM + EQUAL + QUOTE + VARIABLE + QUOTE + AND
                    + EventModel.Columns.ATTRIBUTE_OPTION_COMBO + EQUAL + QUOTE + VARIABLE + QUOTE + AND + "(%s)" +
                    AND + EventModel.TABLE + POINT + EventModel.Columns.STATE + NOT_EQUAL + QUOTE + State.TO_DELETE + QUOTE +
                    AND + EventModel.TABLE + POINT + EventModel.Columns.ORGANISATION_UNIT + IN + "(" + orgUnitQuery + ")";

            selectEventWithProgramUidAndDatesAndCatCombo = selectEventWithProgramUidAndDatesAndCatCombo + pageQuery;
            return briteDatabase.createQuery(EventModel.TABLE, String.format(selectEventWithProgramUidAndDatesAndCatCombo,
                    programUid == null ? "" : programUid,
                    id,
                    getFilteredDatesQuery(dates, period)))
                    .mapToList(EventModel::create).toFlowable(BackpressureStrategy.LATEST);
        } else {
            String selectEventWithProgramUidAndDatesAndCatCombo = SELECT + ALL + FROM + EventModel.TABLE +
                    WHERE + EventModel.Columns.PROGRAM + EQUAL + QUOTE + VARIABLE + QUOTE + AND +
                    EventModel.Columns.ATTRIBUTE_OPTION_COMBO + EQUAL + QUOTE + VARIABLE + QUOTE +
                    AND + EventModel.TABLE + POINT + EventModel.Columns.STATE + NOT_EQUAL + QUOTE + State.TO_DELETE + QUOTE +
                    AND + EventModel.TABLE + POINT + EventModel.Columns.ORGANISATION_UNIT + IN + "(" + orgUnitQuery + ")";

            String id = categoryOptionComboModel.uid() == null ? "" : categoryOptionComboModel.uid();
            selectEventWithProgramUidAndDatesAndCatCombo = selectEventWithProgramUidAndDatesAndCatCombo + pageQuery;
            return briteDatabase.createQuery(EventModel.TABLE, String.format(selectEventWithProgramUidAndDatesAndCatCombo,
                    programUid == null ? "" : programUid,
                    id))
                    .mapToList(cursor -> getEventFromCursor(cursor, programUid))
                    .toFlowable(BackpressureStrategy.LATEST);
        }
    }

    private EventModel getEventFromCursor(Cursor cursor, String programUid){
        EventModel eventModel = EventModel.create(cursor);

        Cursor program = briteDatabase.query(SELECT + ALL + FROM + ProgramModel.TABLE +
                WHERE + ProgramModel.Columns.UID + EQUAL + QUESTION_MARK, programUid);
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
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnitModel>> orgUnits() {
        String selectOrgUnits = SELECT + ALL + FROM + OrganisationUnitModel.TABLE +
                WHERE + OrganisationUnitModel.Columns.UID + IN + "(SELECT UserOrganisationUnit.organisationUnit FROM UserOrganisationUnit " +
                "WHERE UserOrganisationUnit.organisationUnitScope = 'SCOPE_DATA_CAPTURE')";
        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, selectOrgUnits)
                .mapToList(OrganisationUnitModel::create);
    }

    @NonNull
    @Override
    public Observable<List<CategoryOptionComboModel>> catCombo(String categoryComboUid) {
        String id = categoryComboUid == null ? "" : categoryComboUid;
        String selectCategoryCombo = SELECT + CategoryOptionComboModel.TABLE + POINT + ALL + FROM + CategoryOptionComboModel.TABLE +
                INNER_JOIN + CategoryComboModel.TABLE +
                ON + CategoryOptionComboModel.TABLE + POINT + CategoryOptionComboModel.Columns.CATEGORY_COMBO + EQUAL
                + CategoryComboModel.TABLE + POINT + CategoryComboModel.Columns.UID
                + WHERE + CategoryComboModel.TABLE + POINT + CategoryComboModel.Columns.UID + EQUAL + QUOTE + id + QUOTE;
        return briteDatabase.createQuery(CategoryOptionComboModel.TABLE, selectCategoryCombo)
                .mapToList(CategoryOptionComboModel::create);
    }

    @Override
    public Observable<List<String>> eventDataValuesNew(EventModel eventModel) {
        List<String> values = new ArrayList<>();
        String id = eventModel == null ? "" : eventModel.uid();
        Cursor cursor = briteDatabase.query(EVENT_DATA_VALUES, id, id);
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
            cursor.close();
        }

        return Observable.just(values);
    }

    @Override
    public Observable<Boolean> writePermission(String programId) {
        String writePermission = SELECT + ProgramStageModel.TABLE + POINT + ProgramStageModel.Columns.ACCESS_DATA_WRITE +
                FROM + ProgramStageModel.TABLE + WHERE + ProgramStageModel.TABLE + POINT + ProgramStageModel.Columns.PROGRAM +
                EQUAL + QUESTION_MARK + LIMIT_1;
        String programWritePermission = SELECT + ProgramModel.TABLE + POINT + ProgramModel.Columns.ACCESS_DATA_WRITE +
                FROM + ProgramModel.TABLE + WHERE + ProgramModel.TABLE + POINT + ProgramModel.Columns.UID +
                EQUAL + QUESTION_MARK + LIMIT_1;
        return briteDatabase.createQuery(ProgramStageModel.TABLE, writePermission, programId == null ? "" : programId)
                .mapToOne(cursor -> cursor.getInt(0) == 1)
                .flatMap(programStageAccessDataWrite ->
                        briteDatabase.createQuery(ProgramModel.TABLE, programWritePermission, programId == null ? "" : programId)
                                .mapToOne(cursor -> (cursor.getInt(0) == 1) && programStageAccessDataWrite));
    }
}