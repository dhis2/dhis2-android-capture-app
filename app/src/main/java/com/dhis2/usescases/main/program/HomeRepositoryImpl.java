package com.dhis2.usescases.main.program;

import android.support.annotation.NonNull;

import com.dhis2.utils.DateUtils;
import com.dhis2.utils.Period;
import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitProgramLinkModel;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;

import static android.text.TextUtils.isEmpty;

class HomeRepositoryImpl implements HomeRepository {

    private List<Date> dates;
    private Period period;

    private final static String PROGRAMS = "" +
            "SELECT Program.* FROM Program";

    private final static String PROGRAMS_EVENT_DATES_2 = "" +
            "SELECT *, Program.uid, Event.uid AS event_uid, Event.lastUpdated AS event_updated FROM Program " +
            "INNER JOIN Event ON Event.program = Program.uid " +
            "WHERE (%s) " +
            "AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "' " +
            "GROUP BY Program.uid ORDER BY Program.displayName";

    private final static String SELECT_PROGRAMS = "SELECT " +
            "Program.* FROM Program " +
            "JOIN Event ON Event.program = Program.uid " +
            "WHERE (%s) " +
            "AND Event.organisationUnit IN (%s) " +
            "AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "' " +
            "GROUP BY Program.uid ORDER BY Program.displayName";

    private final static String SELECT_EVENTS = "SELECT Event.* FROM Event " +
            "WHERE (%s) " +
            "AND Event.organisationUnit IN (%s) " +
            "AND Event.program = ? " +
            "AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "'";

    private final static String SELECT_EVENTS_NO_DATE = "SELECT Event.* FROM Event " +
            "WHERE Event.organisationUnit IN (%s) " +
            "AND Event.program = ? " +
            "AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "'";


    private final static String[] SELECT_TABLE_NAMES = new String[]{ProgramModel.TABLE, EventModel.TABLE, OrganisationUnitProgramLinkModel.TABLE};
    private final static String[] SELECT_TABLE_NAMES_2 = new String[]{ProgramModel.TABLE, EventModel.TABLE};
    private static final Set<String> SELECT_SET = new HashSet<>(Arrays.asList(SELECT_TABLE_NAMES));
    private static final Set<String> SELECT_SET_2 = new HashSet<>(Arrays.asList(SELECT_TABLE_NAMES_2));

    private final static String SELECT_ORG_UNITS =
            "SELECT * FROM " + OrganisationUnitModel.TABLE;

    private final BriteDatabase briteDatabase;
    private String orgUnits;

    HomeRepositoryImpl(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }


    @NonNull
    @Override
    public Observable<List<ProgramModel>> programs(String orgUnitsIdQuery) {
        this.dates = null;
        this.period = null;
        this.orgUnits = orgUnitsIdQuery;
        String finalQuery = PROGRAMS;
        if (!isEmpty(orgUnitsIdQuery)) {
            finalQuery += String.format(" JOIN Event ON Event.program = Program.uid WHERE Event.organisationUnit IN (%s) AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "'", orgUnitsIdQuery);
        }
        return briteDatabase.createQuery(SELECT_SET_2, finalQuery + " GROUP BY Program.uid")
                .mapToList(ProgramModel::create);
    }

    @NonNull
    @Override
    public Observable<List<ProgramModel>> programs(List<Date> dates, Period period) {
        this.dates = dates;
        this.period = period;

        StringBuilder dateQuery = new StringBuilder();
        String queryFormat = "(%s BETWEEN '%s' AND '%s') ";
        for (int i = 0; i < dates.size(); i++) {
            Date[] datesToQuery = DateUtils.getInstance().getDateFromDateAndPeriod(dates.get(i), period);
            dateQuery.append(String.format(queryFormat, "Event.eventDate", DateUtils.getInstance().formatDate(datesToQuery[0]), DateUtils.getInstance().formatDate(datesToQuery[1])));
            if (i < dates.size() - 1)
                dateQuery.append("OR ");
        }

        return briteDatabase.createQuery(SELECT_SET_2, String.format(PROGRAMS_EVENT_DATES_2, dateQuery))
                .mapToList(ProgramModel::create);
    }


    @NonNull
    @Override
    public Flowable<List<ProgramModel>> programs(List<Date> dates, Period period, String orgUnitsId) {
        this.dates = dates;
        this.period = period;
        this.orgUnits = orgUnitsId;

        StringBuilder dateQuery = new StringBuilder();
        String queryFormat = "(%s BETWEEN '%s' AND '%s') ";
        for (int i = 0; i < dates.size(); i++) {
            Date[] datesToQuery = DateUtils.getInstance().getDateFromDateAndPeriod(dates.get(i), period);
            dateQuery.append(String.format(queryFormat, "Event.eventDate", DateUtils.getInstance().formatDate(datesToQuery[0]), DateUtils.getInstance().formatDate(datesToQuery[1])));
            if (i < dates.size() - 1)
                dateQuery.append("OR ");
        }

        return briteDatabase.createQuery(SELECT_SET, String.format(SELECT_PROGRAMS, dateQuery, orgUnitsId))
                .mapToList(ProgramModel::create).toFlowable(BackpressureStrategy.LATEST);
    }

    @NonNull
    @Override
    public Observable<List<ProgramModel>> toDoPrograms(List<Date> dates, Period period) {
        StringBuilder dateQuery = new StringBuilder();
        String queryFormat = "(%s BETWEEN '%s' AND '%s') ";
        for (int i = 0; i < dates.size(); i++) {
            Date[] datesToQuery = DateUtils.getInstance().getDateFromDateAndPeriod(dates.get(i), period);
            dateQuery.append(String.format(queryFormat, "Event.dueDate", DateUtils.getInstance().formatDate(datesToQuery[0]), DateUtils.getInstance().formatDate(datesToQuery[1])));
            if (i < dates.size() - 1)
                dateQuery.append("OR ");
        }

        return briteDatabase.createQuery(SELECT_SET_2, String.format(PROGRAMS_EVENT_DATES_2, dateQuery))
                .mapToList(ProgramModel::create);
    }

    @NonNull
    @Override
    public Flowable<List<ProgramModel>> toDoPrograms(List<Date> dates, Period period, String orgUnitsId) {
        StringBuilder dateQuery = new StringBuilder();
        String queryFormat = "(%s BETWEEN '%s' AND '%s') ";
        for (int i = 0; i < dates.size(); i++) {
            Date[] datesToQuery = DateUtils.getInstance().getDateFromDateAndPeriod(dates.get(i), period);
            dateQuery.append(String.format(queryFormat, "Event.dueDate", DateUtils.getInstance().formatDate(datesToQuery[0]), DateUtils.getInstance().formatDate(datesToQuery[1])));
            if (i < dates.size() - 1)
                dateQuery.append("OR ");
        }

        return briteDatabase.createQuery(SELECT_SET, String.format(SELECT_PROGRAMS, dateQuery, orgUnitsId))
                .mapToList(ProgramModel::create).toFlowable(BackpressureStrategy.LATEST);
    }

    @NonNull
    @Override
    public Observable<List<EventModel>> eventModels(String programUid) {
        String queryFinal;
        if (dates != null) {
            StringBuilder dateQuery = new StringBuilder();
            String queryFormat = "(%s BETWEEN '%s' AND '%s') ";
            for (int i = 0; i < dates.size(); i++) {
                Date[] datesToQuery = DateUtils.getInstance().getDateFromDateAndPeriod(dates.get(i), period);
                dateQuery.append(String.format(queryFormat, "Event.eventDate", DateUtils.databaseDateFormat().format(datesToQuery[0]), DateUtils.databaseDateFormat().format(datesToQuery[1])));
                if (i < dates.size() - 1)
                    dateQuery.append("OR ");
            }
            queryFinal = String.format(SELECT_EVENTS, dateQuery, orgUnits);
        } else
            queryFinal = String.format(SELECT_EVENTS_NO_DATE, orgUnits);

        return briteDatabase.createQuery(EventModel.TABLE, queryFinal, programUid).mapToList(EventModel::create);
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnitModel>> orgUnits() {
        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, SELECT_ORG_UNITS)
                .mapToList(OrganisationUnitModel::create);
    }

}
