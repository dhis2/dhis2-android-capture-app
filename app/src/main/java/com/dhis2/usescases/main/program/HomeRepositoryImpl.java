package com.dhis2.usescases.main.program;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.dhis2.data.tuples.Pair;
import com.dhis2.utils.DateUtils;
import com.dhis2.utils.Period;
import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.common.ObjectStyleModel;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitProgramLinkModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramType;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityTypeModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;

class HomeRepositoryImpl implements HomeRepository {


    private final static String SELECT_PROGRAMS = "SELECT " +
            "Program.* FROM Program " +
            "JOIN Event ON Event.program = Program.uid " +
            "WHERE (%s) " +
            "AND Event.organisationUnit IN (%s) " +
            "AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "' " +
            "GROUP BY Program.uid ORDER BY Program.displayName";

    private final static String SELECT_EVENTS = "SELECT Event.* FROM Event " +
            "WHERE %s " +
            "Event.program = ? " +
            "AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "'";

    private final static String SELECT_TEIS = "SELECT Event.* FROM Event " +
            "JOIN Enrollment ON Enrollment.uid = Event.enrollment " +
            "WHERE %s " +
            "Event.program = ? " +
            "AND Event.state != 'TO_DELETE' " +
            "GROUP BY Enrollment.trackedEntityInstance";

    private final static String SELECT_EVENTS_NO_DATE = "SELECT Event.* FROM Event " +
            "WHERE Event.organisationUnit IN (%s) " +
            "AND Event.program = ? " +
            "AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "'";

    private final static String TRACKED_ENTITY_TYPE_NAME = "SELECT TrackedEntityType.displayName FROM TrackedEntityType " +
            "WHERE TrackedEntityType.uid = ? LIMIT 1";

    private final static String PROGRAM_MODELS = "SELECT " +
            "Program.uid, " +
            "Program.displayName, " +
            "ObjectStyle.color, " +
            "ObjectStyle.icon," +
            "Program.programType," +
            "Program.trackedEntityType," +
            "Program.description " +
            "FROM Program LEFT JOIN ObjectStyle ON ObjectStyle.uid = Program.uid " +
            "JOIN OrganisationUnitProgramLink ON OrganisationUnitProgramLink.program = Program.uid GROUP BY Program.uid";
    private static final String[] TABLE_NAMES = new String[]{ProgramModel.TABLE, ObjectStyleModel.TABLE,OrganisationUnitProgramLinkModel.TABLE};
    private static final Set<String> TABLE_SET = new HashSet<>(Arrays.asList(TABLE_NAMES));

    private final static String[] SELECT_TABLE_NAMES = new String[]{ProgramModel.TABLE, EventModel.TABLE, OrganisationUnitProgramLinkModel.TABLE};
    private final static String[] SELECT_TABLE_NAMES_2 = new String[]{ProgramModel.TABLE, EventModel.TABLE};
    private static final Set<String> SELECT_SET = new HashSet<>(Arrays.asList(SELECT_TABLE_NAMES));
    private static final Set<String> SELECT_SET_2 = new HashSet<>(Arrays.asList(SELECT_TABLE_NAMES_2));

    private final static String SELECT_ORG_UNITS =
            "SELECT * FROM " + OrganisationUnitModel.TABLE;

    private final BriteDatabase briteDatabase;

    HomeRepositoryImpl(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }

    @NonNull
    @Override
    public Observable<Pair<Integer, String>> numberOfRecords(ProgramModel program) {
        String queryFinal = null;

        String id = program == null || program.uid() == null ? "" : program.uid();

        return briteDatabase.createQuery(EventModel.TABLE, queryFinal, id).mapToList(EventModel::create)
                .flatMap(data -> {
                    if (program.programType() == ProgramType.WITH_REGISTRATION)
                        return briteDatabase.createQuery(TrackedEntityTypeModel.TABLE, TRACKED_ENTITY_TYPE_NAME, program.trackedEntityType())
                                .mapToOne(cursor -> Pair.create(data.size(), cursor.getString(0)));
                    else
                        return Observable.just(Pair.create(data.size(), "events"));
                });
    }

    @NonNull
    @Override
    public Flowable<List<ProgramViewModel>> programModels(List<Date> dates, Period period, String orgUnitsId) {
        return briteDatabase.createQuery(TABLE_SET, PROGRAM_MODELS)
                .mapToList(cursor -> {
                    String uid = cursor.getString(0);
                    String displayName = cursor.getString(1);
                    String color = cursor.getString(2);
                    String icon = cursor.getString(3);
                    String programType = cursor.getString(4);
                    String teiType = cursor.getString(5);
                    String description = cursor.getString(6);

                    //QUERYING Program EVENTS - dates filter
                    StringBuilder dateQuery = new StringBuilder("");
                    if (dates != null && !dates.isEmpty()) {
                        String queryFormat = "(%s BETWEEN '%s' AND '%s') ";
                        for (int i = 0; i < dates.size(); i++) {
                            Date[] datesToQuery = DateUtils.getInstance().getDateFromDateAndPeriod(dates.get(i), period);
                            dateQuery.append(String.format(queryFormat, "Event.eventDate", DateUtils.databaseDateFormat().format(datesToQuery[0]), DateUtils.databaseDateFormat().format(datesToQuery[1])));
                            if (i < dates.size() - 1)
                                dateQuery.append("OR ");
                        }
                    }

                    //QUERYING Program Events - orgUnit filter
                    String orgQuery = "";
                    if (orgUnitsId != null)
                        orgQuery = String.format("Event.organisationUnit IN (%s)", orgUnitsId);


                    String queryFinal;
                    String filter = "";
                    if (!dateQuery.toString().isEmpty() && !orgQuery.isEmpty())
                        filter = dateQuery.toString() + " AND " + orgQuery + " AND ";
                    else if (!dateQuery.toString().isEmpty() || !orgQuery.isEmpty())
                        filter = dateQuery.toString() + orgQuery + " AND ";

                    if (programType.equals(ProgramType.WITH_REGISTRATION.name())) {
                        queryFinal = String.format(SELECT_TEIS, filter);
                    } else {
                        queryFinal = String.format(SELECT_EVENTS, filter);
                    }

                    Cursor countCursor = briteDatabase.query(queryFinal, uid);
                    int count = 0;
                    if (countCursor != null && countCursor.moveToFirst()) {
                        count = countCursor.getCount();
                        countCursor.close();
                    }


                    //QUERYING Tracker name
                    String typeName = "Events";
                    if (programType.equals(ProgramType.WITH_REGISTRATION.name())) {
                        Cursor typeCursor = briteDatabase.query(TRACKED_ENTITY_TYPE_NAME, teiType);
                        if (typeCursor != null && typeCursor.moveToFirst()) {
                            typeName = typeCursor.getString(0);
                            typeCursor.close();
                        }
                    }

                    return ProgramViewModel.create(uid, displayName, color, icon, count, teiType, typeName, programType, description,true,true);
                }).map(list -> checkCount(list, period)).toFlowable(BackpressureStrategy.LATEST);
    }

    private List<ProgramViewModel> checkCount(List<ProgramViewModel> list, Period period) {
        if (period == null)
            return list;
        else {
            List<ProgramViewModel> models = new ArrayList<>();
            for (ProgramViewModel programViewModel : list)
                if (programViewModel.count() > 0)
                    models.add(programViewModel);
            return models;
        }
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnitModel>> orgUnits() {
        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, SELECT_ORG_UNITS)
                .mapToList(OrganisationUnitModel::create);
    }
}
