package com.dhis2.usescases.programEventDetail;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.dhis2.utils.DateUtils;
import com.dhis2.utils.Period;
import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Observable;

/**
 * Created by ppajuelo on 02/11/2017.
 */

public class ProgramEventDetailRepositoryImpl implements ProgramEventDetailRepository {

    private final String EVENT_DATA_VALUES = "SELECT TrackedEntityDataValue.* FROM TrackedEntityDataValue WHERE TrackedEntityDataValue.event = ?\n" +
            "AND TrackedEntityDataValue.dataElement IN\n" +
            "(SELECT ProgramStageDataElement.dataElement FROM ProgramStageDataElement\n" +
            "WHERE ProgramStageDataElement.displayInReports = 1\n" +
            "ORDER BY ProgramStageDataElement.sortOrder ASC\n" +
            ")";

    private final String EVENT_DATA_VALUES_NEW = "SELECT TrackedEntityDataValue.value, DataElement.optionSet FROM TrackedEntityDataValue " +
            "JOIN DataElement ON DataElement.uid = TrackedEntityDataValue.dataElement WHERE TrackedEntityDataValue.event = ?\n" +
            "AND TrackedEntityDataValue.dataElement IN\n" +
            "(SELECT ProgramStageDataElement.dataElement FROM ProgramStageDataElement\n" +
            "WHERE ProgramStageDataElement.displayInReports = '1'\n" +
            "ORDER BY ProgramStageDataElement.sortOrder ASC\n" +
            ")";

    private final String OPTION = "SELECT Option.displayName FROM Option WHERE Option.uid = ?";

    private final BriteDatabase briteDatabase;

    ProgramEventDetailRepositoryImpl(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }

    @NonNull
    private Observable<List<EventModel>> programEvents(String programUid, String fromDate, String toDate) {
        String SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES = "SELECT * FROM " + EventModel.TABLE + " WHERE " + EventModel.Columns.PROGRAM + "='%s' AND " + EventModel.Columns.EVENT_DATE + " BETWEEN '%s' and '%s' " +
                "AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "' " +
                "ORDER BY " + EventModel.TABLE + "." + EventModel.Columns.EVENT_DATE + " DESC";
        return briteDatabase.createQuery(EventModel.TABLE, String.format(SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES, programUid, fromDate, toDate))
                .mapToList(EventModel::create);
    }

    @NonNull
    private Observable<List<EventModel>> programEvents(String programUid, List<Date> dates, Period period) {
        if (dates != null) {
            String SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES = "SELECT * FROM " + EventModel.TABLE + " WHERE " + EventModel.Columns.PROGRAM + "='%s' AND (%s) " +
                    "AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "' " +
                    "ORDER BY " + EventModel.TABLE + "." + EventModel.Columns.EVENT_DATE + " DESC";
            StringBuilder dateQuery = new StringBuilder();
            String queryFormat = "(%s BETWEEN '%s' AND '%s') ";
            for (int i = 0; i < dates.size(); i++) {
                Date[] datesToQuery = DateUtils.getInstance().getDateFromDateAndPeriod(dates.get(i), period);
                dateQuery.append(String.format(queryFormat, EventModel.Columns.EVENT_DATE, DateUtils.databaseDateFormat().format(datesToQuery[0]), DateUtils.databaseDateFormat().format(datesToQuery[1])));
                if (i < dates.size() - 1)
                    dateQuery.append("OR ");
            }

            return briteDatabase.createQuery(EventModel.TABLE, String.format(SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES, programUid, dateQuery))
                    .mapToList(EventModel::create);
        } else {
            String SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES = "SELECT * FROM " + EventModel.TABLE + " WHERE " + EventModel.Columns.PROGRAM + "='%s' " +
                    "AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "' " +
                    "ORDER BY " + EventModel.TABLE + "." + EventModel.Columns.EVENT_DATE + " DESC";

            return briteDatabase.createQuery(EventModel.TABLE, String.format(SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES, programUid))
                    .mapToList(EventModel::create);
        }
    }

    @NonNull
    @Override
    public Observable<List<EventModel>> filteredProgramEvents(String programUid, String fromDate, String toDate, CategoryOptionComboModel categoryOptionComboModel) {
        if (categoryOptionComboModel == null) {
            return programEvents(programUid, fromDate, toDate);
        }
        String SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES_AND_CAT_COMBO = "SELECT * FROM " + EventModel.TABLE + " WHERE " + EventModel.Columns.PROGRAM + "='%s' AND " + EventModel.Columns.EVENT_DATE + " BETWEEN '%s' and '%s'"
                + " AND " + EventModel.Columns.ATTRIBUTE_OPTION_COMBO + "='%s'"
                + " AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "'";
        return briteDatabase.createQuery(EventModel.TABLE, String.format(SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES_AND_CAT_COMBO, programUid, fromDate, toDate, categoryOptionComboModel.uid()))
                .mapToList(EventModel::create);
    }

    @NonNull
    @Override
    public Observable<List<EventModel>> filteredProgramEvents(String programUid, List<Date> dates, Period period, CategoryOptionComboModel categoryOptionComboModel) {
        if (categoryOptionComboModel == null) {
            return programEvents(programUid, dates, period);
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

            return briteDatabase.createQuery(EventModel.TABLE, String.format(SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES_AND_CAT_COMBO, programUid, categoryOptionComboModel.uid(), dateQuery))
                    .mapToList(EventModel::create);
        } else {
            String SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES_AND_CAT_COMBO = "SELECT * FROM " + EventModel.TABLE + " WHERE " + EventModel.Columns.PROGRAM + "='%s' AND " + EventModel.Columns.ATTRIBUTE_OPTION_COMBO + "='%s' " +
                    "AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "'";

            return briteDatabase.createQuery(EventModel.TABLE, String.format(SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES_AND_CAT_COMBO, programUid, categoryOptionComboModel.uid()))
                    .mapToList(EventModel::create);
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
        String SELECT_CATEGORY_COMBO = "SELECT " + CategoryOptionComboModel.TABLE + ".* FROM " + CategoryOptionComboModel.TABLE + " INNER JOIN " + CategoryComboModel.TABLE +
                " ON " + CategoryOptionComboModel.TABLE + "." + CategoryOptionComboModel.Columns.CATEGORY_COMBO + " = " + CategoryComboModel.TABLE + "." + CategoryComboModel.Columns.UID
                + " WHERE " + CategoryComboModel.TABLE + "." + CategoryComboModel.Columns.UID + " = '" + categoryComboUid + "'";
        return briteDatabase.createQuery(CategoryOptionComboModel.TABLE, SELECT_CATEGORY_COMBO)
                .mapToList(CategoryOptionComboModel::create);
    }

    @NonNull
    @Override
    public Observable<List<TrackedEntityDataValueModel>> eventDataValues(EventModel eventModel) {

        return briteDatabase.createQuery(TrackedEntityDataValueModel.TABLE, EVENT_DATA_VALUES, eventModel.uid())
                .mapToList(TrackedEntityDataValueModel::create);
    }

    public Observable<List<String>> eventDataValuesNew(EventModel eventModel) {
        List<String> values = new ArrayList<>();
        Cursor cursor = briteDatabase.query(EVENT_DATA_VALUES_NEW, eventModel.uid());
        if (cursor != null && cursor.moveToFirst()) {
            for (int i = 0; i < cursor.getCount(); i++) {
                String value = cursor.getString(0);

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
        return briteDatabase.createQuery(ProgramStageModel.TABLE, WRITE_PERMISSION, programId)
                .mapToOne(cursor -> cursor.getInt(0) == 1);
    }
}