package com.dhis2.usescases.programEventDetail;

import android.support.annotation.NonNull;

import com.dhis2.utils.DateUtils;
import com.dhis2.utils.Period;
import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

import java.util.Date;
import java.util.List;

import io.reactivex.Observable;

/**
 * Created by ppajuelo on 02/11/2017.
 *
 */

public class ProgramEventDetailRepositoryImpl implements ProgramEventDetailRepository {

    private final BriteDatabase briteDatabase;

    ProgramEventDetailRepositoryImpl(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }

    @NonNull
    @Override
    public Observable<List<EventModel>> programEvents(String programUid) {
        String SELECT_EVENT_WITH_PROGRAM_UID = "SELECT * FROM " + EventModel.TABLE + " WHERE " + EventModel.Columns.PROGRAM + "=";
        return briteDatabase.createQuery(EventModel.TABLE, SELECT_EVENT_WITH_PROGRAM_UID + "'" + programUid + "'")
                .mapToList(EventModel::create);
    }

    @NonNull
    @Override
    public Observable<List<EventModel>> programEvents(String programUid, String fromDate, String toDate) {
        String SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES = "SELECT * FROM " + EventModel.TABLE + " WHERE " + EventModel.Columns.PROGRAM + "='%s' AND " + EventModel.Columns.LAST_UPDATED + " BETWEEN '%s' and '%s'" ;
        return briteDatabase.createQuery(EventModel.TABLE, String.format(SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES, programUid, fromDate, toDate))
                .mapToList(EventModel::create);
    }

    @NonNull
    @Override
    public Observable<List<EventModel>> programEvents(String programUid, List<Date> dates, Period period) {
        String SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES = "SELECT * FROM " + EventModel.TABLE + " WHERE " + EventModel.Columns.PROGRAM + "='%s' AND (%s)";
        StringBuilder dateQuery = new StringBuilder();
        String queryFormat = "(%s BETWEEN '%s' AND '%s') ";
        for (int i = 0; i < dates.size(); i++) {
            Date[] datesToQuery = DateUtils.getInstance().getDateFromDateAndPeriod(dates.get(i), period);
            dateQuery.append(String.format(queryFormat, EventModel.Columns.LAST_UPDATED, DateUtils.getInstance().formatDate(datesToQuery[0]), DateUtils.getInstance().formatDate(datesToQuery[1])));
            if (i < dates.size() - 1)
                dateQuery.append("OR ");
        }

        return briteDatabase.createQuery(EventModel.TABLE, String.format(SELECT_EVENT_WITH_PROGRAM_UID_AND_DATES, programUid, dateQuery))
                .mapToList(EventModel::create);
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnitModel>> orgUnits() {
        String SELECT_ORG_UNITS = "SELECT * FROM " + OrganisationUnitModel.TABLE;
        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, SELECT_ORG_UNITS)
                .mapToList(OrganisationUnitModel::create);
    }
}