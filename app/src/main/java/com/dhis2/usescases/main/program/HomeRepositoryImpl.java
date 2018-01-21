package com.dhis2.usescases.main.program;

import android.support.annotation.NonNull;

import com.dhis2.utils.DateUtils;
import com.dhis2.utils.Period;
import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitProgramLinkModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramType;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import io.reactivex.Observable;

class HomeRepositoryImpl implements HomeRepository {

    /*private final static String SELECT_EVENTS = String.format(Locale.US,
            "SELECT * FROM %s WHERE %s.%s = 'programUid' ORDER BY %s.%s DESC",
            EventModel.TABLE, EventModel.TABLE, EventModel.Columns.PROGRAM, EventModel.TABLE, EventModel.Columns.LAST_UPDATED);*/

    private final static String SELECT_EVENTS = String.format(Locale.US,
            "SELECT * FROM %s WHERE %s.%s = 'programUid'",
            EventModel.TABLE, EventModel.TABLE, EventModel.Columns.PROGRAM);

    private final static String SELECT_PROGRAMS_VIEW_MODELS = String.format(Locale.US,
            "SELECT * FROM " +
                    "(SELECT %s,%s,%s,%s,%s,%s,%s,'%s' AS %s FROM %s) " +
                    "ORDER BY %s DESC",
            ProgramModel.Columns.CATEGORY_COMBO, ProgramModel.Columns.DISPLAY_FRONT_PAGE_LIST, ProgramModel.Columns.TRACKED_ENTITY, ProgramModel.Columns.PROGRAM_TYPE, ProgramModel.Columns.UID, ProgramModel.Columns.DISPLAY_NAME, ProgramModel.Columns.LAST_UPDATED,
            HomeViewModel.Type.PROGRAM.name(), HomeViewModel.Columns.HOME_VIEW_MODEL_TYPE, ProgramModel.TABLE,
            HomeViewModel.Columns.HOME_VIEW_MODEL_TYPE);

    private final static String PROGRAMS_EVENT_DATES = "" +
            "SELECT * FROM Program " +
            "INNER JOIN Event ON Event.program = Program.uid " +
            "WHERE Event.lastUpdated BETWEEN '%s' AND '%s' " +
            "GROUP BY Program.uid";

    private final static String PROGRAMS_EVENT_DATES_2 = "" +
            "SELECT * FROM Program " +
//            "INNER JOIN Event ON Event.program = Program.uid " +
            "WHERE (%s) " +
            "GROUP BY Program.uid";

    public final static String SELECT_PROGRAMS_VIEW_MODELS_ORG_UNIT =
            "SELECT trackedEntity, uid, displayName, lastUpdated, '" + HomeViewModel.Type.PROGRAM.name() + "' AS homeViewModelType FROM " +
                    "Program" +
                    " INNER JOIN OrganisationUnitProgramLink ON Program.uid = OrganisationUnitProgramLink.program" +
                    " WHERE OrganisationUnitProgramLink.organisationUnit IN (%s) GROUP BY Program.uid";


    private final static String SELECT_ORG_UNITS =
            "SELECT * FROM " + OrganisationUnitModel.TABLE;

    private final static String SELECT_TRACK_ENTITIES =
            "SELECT * FROM " + TrackedEntityInstanceModel.TABLE;

    private static final String[] TABLE_NAMES = new String[]{TrackedEntityModel.TABLE, ProgramModel.TABLE};
    private static final String[] HOME_TABLE = new String[]{EventModel.TABLE, ProgramModel.TABLE};
    private static final String[] TABLE_NAMES_2 = new String[]{ProgramModel.TABLE, OrganisationUnitProgramLinkModel.TABLE};
    private static final Set<String> TABLE_SET = new HashSet<>(Arrays.asList(TABLE_NAMES));
    private static final Set<String> TABLE_SET_2 = new HashSet<>(Arrays.asList(TABLE_NAMES_2));

    private final BriteDatabase briteDatabase;

    HomeRepositoryImpl(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }


    @NonNull
    @Override
    public Observable<List<ProgramModel>> programs(String fromDate, String toDate) {
        return briteDatabase.createQuery(ProgramModel.TABLE, String.format(PROGRAMS_EVENT_DATES, fromDate, toDate))
                .mapToList(ProgramModel::create);
    }

    @NonNull
    @Override
    public Observable<List<ProgramModel>> programs(List<Date> dates, Period period) {

        StringBuilder dateQuery = new StringBuilder();
        String queryFormat = "(%s BETWEEN '%s' AND '%s') ";
        for (int i = 0; i < dates.size(); i++) {
            Date[] datesToQuery = DateUtils.getInstance().getDateFromDateAndPeriod(dates.get(i), period);
            dateQuery.append(String.format(queryFormat, "Program.lastUpdated", DateUtils.getInstance().formatDate(datesToQuery[0]), DateUtils.getInstance().formatDate(datesToQuery[1])));
            if (i < dates.size() - 1)
                dateQuery.append("OR ");
        }

        return briteDatabase.createQuery(ProgramModel.TABLE, String.format(PROGRAMS_EVENT_DATES_2, dateQuery))
                .mapToList(ProgramModel::create);

    }

    @NonNull
    @Override
    public Observable<List<EventModel>> eventModels(String programUid) {
        return briteDatabase.createQuery(EventModel.TABLE, SELECT_EVENTS.replace("programUid",programUid))
                .mapToList(EventModel::create);
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnitModel>> orgUnits() {
        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, SELECT_ORG_UNITS)
                .mapToList(OrganisationUnitModel::create);
    }

}
