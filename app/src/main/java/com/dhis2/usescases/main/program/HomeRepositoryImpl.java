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

    /*
     SELECT * FROM
     (SELECT uid,displayName,'TRACKED_ENTITY' AS homeViewModelType FROM TrackedEntity
     UNION SELECT
     uid,displayName,'PROGRAM_NO_REG' AS homeViewModelType FROM Program WHERE Program.programType =
     'WITHOUT_REGISTRATION')
     ORDER BY homeViewModelType DESC
     */
    private final static String SELECT_EVENTS = String.format(Locale.US,
            "SELECT * FROM %s", EventModel.TABLE);
    private final static String SELECT_HOME_VIEW_MODELS = String.format(Locale.US,
            "SELECT * FROM " +
                    "(SELECT %s,%s,'%s' AS %s FROM %s " +
                    "UNION SELECT %s,%s,'%s' AS %s FROM %s WHERE %s.%s = '%s') " +
                    "ORDER BY %s DESC",
            TrackedEntityModel.Columns.UID, TrackedEntityModel.Columns.DISPLAY_NAME,
            HomeViewModel.Type.TRACKED_ENTITY.name(), HomeViewModel.Columns.HOME_VIEW_MODEL_TYPE,
            TrackedEntityModel.TABLE, ProgramModel.Columns.UID, ProgramModel.Columns.DISPLAY_NAME,
            HomeViewModel.Type.PROGRAM.name(), HomeViewModel.Columns.HOME_VIEW_MODEL_TYPE, ProgramModel.TABLE,
            ProgramModel.TABLE, ProgramModel.Columns.PROGRAM_TYPE, ProgramType.WITHOUT_REGISTRATION.name(),
            HomeViewModel.Columns.HOME_VIEW_MODEL_TYPE);

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
            "WHERE Program.lastUpdated BETWEEN '%s' AND '%s' " +
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
    public Observable<List<HomeViewModel>> homeViewModels() {
        return briteDatabase.createQuery(ProgramModel.TABLE, SELECT_PROGRAMS_VIEW_MODELS)
                .mapToList(HomeViewModel::fromCursor);
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
            dateQuery.append(String.format(queryFormat, "Program.lastUpdated",  DateUtils.getInstance().formatDate(datesToQuery[0]),  DateUtils.getInstance().formatDate(datesToQuery[1])));
            if (i < dates.size() - 1)
                dateQuery.append("OR ");
        }

        return briteDatabase.createQuery(ProgramModel.TABLE, String.format(PROGRAMS_EVENT_DATES_2, dateQuery))
                .mapToList(ProgramModel::create);

    }

    @NonNull
    @Override
    public Observable<List<EventModel>> eventModels(String programUid) {
        return briteDatabase.createQuery(EventModel.TABLE, SELECT_EVENTS)
                .mapToList(EventModel::create);
    }

    @NonNull
    @Override
    public Observable<List<HomeViewModel>> homeViewModels(ArrayList<String> orgUnitIds) {

        String queary = "";
        for (String id : orgUnitIds) {
            if (queary.isEmpty())
                queary = String.format(Locale.US, "'%s'", id);
            else
                queary = queary.concat(String.format(Locale.US, ", '%s'", id));
        }

        return briteDatabase.createQuery(TABLE_SET_2, String.format(Locale.US, SELECT_PROGRAMS_VIEW_MODELS_ORG_UNIT, queary))
                .mapToList(HomeViewModel::fromCursor);
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnitModel>> orgUnits() {
        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, SELECT_ORG_UNITS)
                .mapToList(OrganisationUnitModel::create);
    }

    @Override
    public Observable<List<TrackedEntityInstanceModel>> trackedEntities() {
        return briteDatabase.createQuery(TrackedEntityInstanceModel.TABLE, SELECT_TRACK_ENTITIES)
                .mapToList(TrackedEntityInstanceModel::create);
    }
}
